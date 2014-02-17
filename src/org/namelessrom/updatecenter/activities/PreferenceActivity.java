package org.namelessrom.updatecenter.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.namelessrom.updatecenter.R;

import java.util.List;

/**
 * Created by alex on 17.02.14.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Some action");
            setListFooter(button);
        }*/
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class PrefsUpdates extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_update);
        }

    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PrefsUpdates.class.getName().equals(fragmentName)
                || super.isValidFragment(fragmentName);
    }
}
