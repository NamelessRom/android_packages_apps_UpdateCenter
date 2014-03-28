package org.namelessrom.updatecenter.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.widgets.AttachFragment;

public class WelcomeFragment extends AttachFragment {

    public static final int ID = 100;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }
}
