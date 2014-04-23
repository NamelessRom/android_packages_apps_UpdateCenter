package org.namelessrom.updatecenter.fragments.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.items.AppData;

/**
 * Created by alex on 28.04.14.
 */
public class AppDetailsFragment extends Fragment {

    public static final String ARG_APP_DATA = "arg_app_data";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        final AppData appData = (AppData) arguments.getSerializable(ARG_APP_DATA);

        final View v = inflater.inflate(R.layout.fragment_app_details, container, false);

        final TextView tv = (TextView) v.findViewById(R.id.DUMMY_DETAILS);
        tv.setText(appData.getAppId() + "\n" +
                appData.getTitle() + "\n" +
                appData.getDescription() + "\n" +
                appData.getVersionName() + "\n" +
                appData.getVersionCode() + "\n" +
                appData.getTimestamp() + "\n");

        return v;
    }
}
