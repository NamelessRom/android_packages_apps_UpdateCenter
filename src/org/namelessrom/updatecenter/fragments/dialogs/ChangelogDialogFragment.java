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
package org.namelessrom.updatecenter.fragments.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangelogDialogFragment extends DialogFragment implements Constants {

    public static final String BUNDLE_FILENAME = "bundle_filename";
    public static final String BUNDLE_URL = "bundle_url";

    private TextView mTvChangelog;

    private String filePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_changelog, container, false);

        final String fileName = getArguments().getString(BUNDLE_FILENAME);
        final String url = getArguments().getString(BUNDLE_URL);

        mTvChangelog = (TextView) v.findViewById(R.id.changelogTextView);
        mTvChangelog.setTypeface(Typeface.SANS_SERIF);
        mTvChangelog.setText(fileName + "\n\n");

        filePath = UPDATE_FOLDER_FULL + File.separator + fileName;

        if (new File(filePath).exists()) {
            mTvChangelog.setText(readFile(filePath));
        } else {
            final DownloadTask downloadTask = new DownloadTask(getActivity());
            downloadTask.execute(url, filePath);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private String readFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        sb.append(mTvChangelog.getText());

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str).append("\n");
            }
        } catch (Exception exc) {
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }

        sb.append("\n\n").append(getString(R.string.changelog_loaded));

        return sb.toString();
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(final String... pParams) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            wl.acquire();

            try {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(pParams[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                    }

                    input = connection.getInputStream();
                    output = new FileOutputStream(pParams[1]);

                    byte data[] = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()) {
                            return null;
                        }
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null) {
                            output.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException ignored) {
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            } finally {
                wl.release();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (isAdded()) {
                mTvChangelog.setText(readFile(filePath));
            }
        }
    }
}
