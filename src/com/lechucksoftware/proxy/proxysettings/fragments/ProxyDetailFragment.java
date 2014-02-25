package com.lechucksoftware.proxy.proxysettings.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.lechucksoftware.proxy.proxysettings.ApplicationGlobals;
import com.lechucksoftware.proxy.proxysettings.R;
import com.lechucksoftware.proxy.proxysettings.activities.ProxyDetailActivity;
import com.lechucksoftware.proxy.proxysettings.components.InputExclusionList;
import com.lechucksoftware.proxy.proxysettings.components.InputField;
import com.lechucksoftware.proxy.proxysettings.components.InputTags;
import com.lechucksoftware.proxy.proxysettings.db.ProxyEntity;
import com.lechucksoftware.proxy.proxysettings.fragments.base.BaseDialogFragment;
import com.lechucksoftware.proxy.proxysettings.fragments.base.IBaseFragment;
import com.lechucksoftware.proxy.proxysettings.preferences.ValidationPreference;
import com.lechucksoftware.proxy.proxysettings.utils.EventReportingUtils;
import com.lechucksoftware.proxy.proxysettings.utils.UIUtils;
import com.shouldit.proxy.lib.ProxyStatusItem;
import com.shouldit.proxy.lib.enums.ProxyStatusProperties;
import com.shouldit.proxy.lib.log.LogWrapper;
import com.shouldit.proxy.lib.utils.ProxyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ProxyDetailFragment extends BaseDialogFragment
{
    public static ProxyDetailFragment instance;
    public static final String TAG = ProxyDetailFragment.class.getSimpleName();

    // Arguments
    private static final String SELECTED_PROXY_ARG = "SELECTED_PROXY_ARG";

    private InputField proxyHost;
    private InputField proxyPort;
    private InputExclusionList proxyBypass;
//    private InputTags proxyTags;
    private ProxyEntity selectedProxy;
    private UUID cachedObjId;
    private UIHandler uiHandler;
    private RelativeLayout proxyInUseBanner;
    private ScrollView proxyScrollView;
    private Map<ProxyStatusProperties,CharSequence> validationErrors;

    public static ProxyDetailFragment newInstance(UUID cachedObjId)
    {
        ProxyDetailFragment instance = new ProxyDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(SELECTED_PROXY_ARG, cachedObjId);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        uiHandler = new UIHandler();
        validationErrors = new HashMap<ProxyStatusProperties, CharSequence>();

        if (args != null && args.containsKey(SELECTED_PROXY_ARG))
        {
            cachedObjId = (UUID) getArguments().getSerializable(SELECTED_PROXY_ARG);
            selectedProxy = (ProxyEntity) ApplicationGlobals.getCacheManager().get(cachedObjId);
        }
        else
        {
            // TODO: Add handling here
            EventReportingUtils.sendException(new Exception("NO PROXY RECEIVED"));
        }

        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.proxy_preferences, container, false);

        getUIComponents(v);
        uiHandler.callRefreshUI();

        return v;
    }

    private void getUIComponents(View v)
    {
        proxyScrollView = (ScrollView) v.findViewById(R.id.proxy_scrollview);
        proxyInUseBanner = (RelativeLayout) v.findViewById(R.id.proxy_in_use_banner);

        proxyHost = (InputField) v.findViewById(R.id.proxy_host);
        proxyHost.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {  }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String value = editable.toString();
                selectedProxy.host = value;

                proxyHost.setError(null);
                ProxyStatusItem item = ProxyUtils.isProxyValidHostname(value);
                validationErrors.remove(item.statusCode);

                if (!item.result)
                {
                    proxyHost.setError(item.message);
                    validationErrors.put(item.statusCode, item.message);
                }

                checkValidation();
            }
        });

        proxyPort = (InputField) v.findViewById(R.id.proxy_port);
        proxyPort.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                Integer value = null;
                try
                {
                    value = Integer.parseInt(editable.toString());
                }
                catch (NumberFormatException e)
                {
                    value = Integer.MAX_VALUE;
                }

                ProxyStatusItem item = ProxyUtils.isProxyValidPort(value);
                validationErrors.remove(item.statusCode);

                proxyPort.setError(null);
                if (!item.result)
                {
                    proxyPort.setError(item.message);
                    validationErrors.put(item.statusCode, item.message);
                }

                selectedProxy.port = value;
                checkValidation();
            }
        });

        proxyBypass = (InputExclusionList) v.findViewById(R.id.proxy_bypass);
        proxyBypass.addValueChangedListener(new InputExclusionList.ValueChangedListener()
        {
            @Override
            public void onExclusionListChanged(String result)
            {
                LogWrapper.d(TAG,"Exclusion list updated: " + result);
                selectedProxy.exclusion = result;
                proxyScrollView.scrollTo(0,proxyScrollView.getBottom());

                ProxyStatusItem item = ProxyUtils.isProxyValidExclusionList(selectedProxy.exclusion);
                validationErrors.remove(item.statusCode);
                if (!item.result)
                {
                    validationErrors.put(item.statusCode,item.message);
                }

                checkValidation();
            }
        });

//        proxyTags = (InputTags) v.findViewById(R.id.proxy_tags);
//        proxyTags.setTagsViewOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                TagsListFragment tagsListSelectorFragment = TagsListFragment.newInstance(cachedObjId);
//                tagsListSelectorFragment.show(getFragmentManager(), TAG);
//            }
//        });
    }

    private void checkValidation()
    {
        if(validationErrors.isEmpty())
        {
            ((ProxyDetailActivity)getActivity()).enableSave();
        }
        else
        {
            ((ProxyDetailActivity)getActivity()).disableSave();
        }
    }

    private void refreshUI()
    {
        if (selectedProxy != null)
        {
            proxyInUseBanner.setVisibility(UIUtils.booleanToVisibility(selectedProxy.getInUse()));

            proxyHost.setValue(selectedProxy.host);
            if (selectedProxy.port != null && selectedProxy.port != 0)
            {
                proxyPort.setValue(selectedProxy.port);
            }

            proxyBypass.setExclusionString(selectedProxy.exclusion);
//                proxyTags.setTags(selectedProxy.getTags());
        }
    }

    private class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message message)
        {
            Bundle b = message.getData();

            LogWrapper.w(TAG, "handleMessage: " + b.toString());

            refreshUI();
        }

        public void callRefreshUI()
        {
            sendEmptyMessage(0);
        }
    }
}