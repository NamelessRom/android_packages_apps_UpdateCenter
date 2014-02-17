/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.updatecenter.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.updatecenter.R;

/**
 * Created by alex on 05.01.14.
 */
class DynamicEntryFragment extends Fragment {

    public static final String ARG_FRAGMENT = "arg_fragment";
    public static final String ARG_IMG = "arg_img";

    private String mText;
    private int mImgId = R.mipmap.ic_launcher;

    public static DynamicEntryFragment newInstance(int fragmentId, int imgId) {
        DynamicEntryFragment f = new DynamicEntryFragment();

        Bundle b = new Bundle();
        b.putInt(DynamicEntryFragment.ARG_FRAGMENT, fragmentId);
        b.putInt(DynamicEntryFragment.ARG_IMG, imgId);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        final Bundle b = getArguments();
        final int fragmentId = (b != null ? b.getInt(ARG_FRAGMENT) : -1);
        mImgId = (b != null ? b.getInt(ARG_IMG) : mImgId);

        switch (fragmentId) {
            case 0:
                mText = getString(R.string.action_updates);
                break;
            default:
                mText = "Could not identify fragment to load";
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dynamic_entry, container, false);

        TextView tvHelp = (TextView) view.findViewById(R.id.dynamic_textView);
        tvHelp.setText(mText);

        ImageView ivHelp = (ImageView) view.findViewById(R.id.dynamic_imageView);
        ivHelp.setImageResource(mImgId);

        return view;
    }
}
