package com.lechucksoftware.proxy.proxysettings.ui.base;

import android.os.Bundle;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import timber.log.Timber;

/**
 * Created by marco on 24/05/13.
 */
public class BasePreferenceFragment extends PreferenceFragment
{
    /**
     *  Fragment life-cycle
     *
     *  onAttach()	The fragment instance is associated with an activity instance.The activity is not yet fully initialized
     *
     *  onCreate()	Fragment is created
     *
     *  onCreateView()	The fragment instance creates its view hierarchy. The inflated views become part of the view hierarchy of its containing activity.
     *
     *  onActivityCreated()	 Activity and fragment instance have been created as well as thier view hierarchy.
     *
     *  onResume()	 Fragment becomes visible and active.
     *
     *  onPause()	 Fragment is visibile but becomes not active anymore, e.g., if another activity is animating on top of the activity which contains the fragment.
     *
     *  onStop()	 Fragment becomes not visible.
     */

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    protected static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Timber.d("onCreate " + this.getClass().getSimpleName());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Timber.d("onResume " + this.getClass().getSimpleName());
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Timber.d("onPause " + this.getClass().getSimpleName());
    }
}
