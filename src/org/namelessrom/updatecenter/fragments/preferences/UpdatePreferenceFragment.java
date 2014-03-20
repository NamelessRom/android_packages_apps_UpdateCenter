package org.namelessrom.updatecenter.fragments.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.namelessrom.updatecenter.R;

/**
 * Created by alex on 20.03.14.
 */
public class UpdatePreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_update);
    }

}
