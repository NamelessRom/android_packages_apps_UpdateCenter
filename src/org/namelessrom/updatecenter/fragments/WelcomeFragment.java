package org.namelessrom.updatecenter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachFragment;

public class WelcomeFragment extends AttachFragment {

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, Constants.ID_UC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_welcome, container, false);

        return v;
    }
}
