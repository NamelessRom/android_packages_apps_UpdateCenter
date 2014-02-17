/*
 * Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.interfaces.FragmentInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 05.01.14.
 */
public class CenterMainFragment extends Fragment implements View.OnClickListener,
        Animation.AnimationListener, FragmentInterface {

    //
    private static final int ANIMATION_FIRST_STEP = 0;
    private static final int ANIMATION_SECOND_STEP = 1;
    private static final int ANIMATION_DURATION_SHORT = 200;
    private static final int ANIMATION_DURATION_LONG = 500;

    //
    private int mCurrentId = 0;
    private Fragment mCurrentFragment;
    private View mCurrentView;
    // For flipping
    private boolean mShowingBack = false;
    // For zooming
    private int mInitialHeight, mInitialWidth;
    private int mAnimationStep = 0;

    //
    private View rootView;
    //private FrameLayout addonView, dummyView1, dummyView2;
    private static final List<View> viewList = new ArrayList<View>();

    //

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_center_main, container, false);

        final FrameLayout updateView = (FrameLayout) rootView.findViewById(R.id.frameLayout0);
        updateView.setOnClickListener(this);

        //addonView = (FrameLayout) rootView.findViewById(R.id.frameLayout1);
        //addonView.setOnClickListener(this);

        //dummyView1 = (FrameLayout) rootView.findViewById(R.id.frameLayout2);
        //dummyView1.setOnClickListener(this);

        //dummyView2 = (FrameLayout) rootView.findViewById(R.id.frameLayout3);
        //dummyView2.setOnClickListener(this);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout0,
                        DynamicEntryFragment.newInstance(0, R.drawable.ic_action_update))
                .commit();

        viewList.add(updateView);
        //viewList.add(addonView);
        //viewList.add(dummyView1);
        //viewList.add(dummyView2);

        return rootView;
    }

    @Override
    public void onClick(final View view) {
        mCurrentView = view;
        mCurrentId = view.getId();
        switch (mCurrentId) {

            case R.id.frameLayout0:
                mCurrentFragment = new UpdateFragment();
                break;

            default:
                return;

        }

        flipCard();
    }


    //

    void flipCard() {

        if (!mShowingBack) {
            mInitialWidth = mCurrentView.getWidth();
            mInitialHeight = mCurrentView.getHeight();
            for (View v : viewList) {
                if (!v.equals(mCurrentView)) {
                    v.setVisibility(View.GONE);
                }
            }
        }

        ScaleAnimation animation = new ScaleAnimation(1, 0, 1, 0);
        animation.setDuration(ANIMATION_DURATION_SHORT);
        animation.setAnimationListener(this);
        mCurrentView.startAnimation(animation);

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        switch (mAnimationStep) {
            case ANIMATION_FIRST_STEP:

                mAnimationStep = ANIMATION_SECOND_STEP;

                mCurrentView.getLayoutParams().height =
                        (mShowingBack ? mInitialHeight : rootView.getHeight());
                mCurrentView.getLayoutParams().width =
                        (mShowingBack ? mInitialWidth : rootView.getWidth());
                mCurrentView.requestLayout();

                animation = new ScaleAnimation(0, 1, 0, 1);
                animation.setDuration(ANIMATION_DURATION_LONG);
                animation.setAnimationListener(this);
                mCurrentView.startAnimation(animation);
                break;
            case ANIMATION_SECOND_STEP:

                mAnimationStep = ANIMATION_FIRST_STEP;

                if (mShowingBack) {
                    for (View v : viewList) {
                        if (!v.equals(mCurrentView)) {
                            v.setVisibility(View.VISIBLE);
                        }
                    }
                    mShowingBack = false;
                    getChildFragmentManager().popBackStack();
                    return;
                }

                getChildFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                                R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                        .replace(mCurrentId, mCurrentFragment)
                        .addToBackStack(null)
                        .commit();

                mShowingBack = true;

                break;
        }
    }

    @Override
    public void onAnimationRepeat(final Animation animation) {
        // Intentionally left blank
    }

    @Override
    public void onAnimationStart(final Animation animation) {
        // Intentionally left blank
    }

    @Override
    public boolean onFragmentBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            flipCard();
            return true;
        } else {
            return false;
        }
    }
}
