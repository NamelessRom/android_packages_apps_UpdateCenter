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

package org.namelessrom.updatecenter.net;

import com.android.okhttp.OkHttpClient;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.events.VolleyResponseEvent;
import org.namelessrom.updatecenter.utils.BusProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Handler for HTTP Requests
 */
public class HttpHandler {

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    public static final int TYPE_GENERAL = 1;

    public static String get(String url) throws IOException {
        String responseString;

        final HttpURLConnection connection = mOkHttpClient.open(new URL(url));
        connection.setConnectTimeout(5000);
        InputStream in = null;
        try {
            in = connection.getInputStream();
            byte[] response = readFully(in);
            responseString = new String(response, "UTF-8");
        } finally {
            if (in != null) in.close();
        }

        return responseString;
    }

    public static String post(URL url, byte[] body) throws IOException {
        String responseString;

        final HttpURLConnection connection = mOkHttpClient.open(url);
        OutputStream out = null;
        InputStream in = null;
        try {
            // Write the request.
            connection.setRequestMethod("POST");
            out = connection.getOutputStream();
            out.write(body);
            out.close();

            // Read the response.
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP response: "
                        + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
            in = connection.getInputStream();
            byte[] response = readFully(in);
            responseString = new String(response, "UTF-8");
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }

        return responseString;
    }

    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    public static void getVolley(final String url, final int type) {
        final JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject res) {
                        try {
                            if (res != null) {
                                switch (type) {
                                    default:
                                    case TYPE_GENERAL:
                                        BusProvider.getBus().post(
                                                new VolleyResponseEvent(res.toString()));
                                        break;
                                }
                            }
                        } catch (Exception exc) {
                            BusProvider.getBus().post(
                                    new VolleyResponseEvent(exc.getMessage(), true));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        BusProvider.getBus().post(
                                new VolleyResponseEvent(error.getMessage(), true));
                    }
                }
        );
        Application.addToRequestQueue(req);
    }

    public static void postVolley(final String url, final Map<String, String> map, final int type) {
        final JsonObjectRequest req = new JsonObjectRequest(url, new JSONObject(map),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject res) {
                        try {
                            if (res != null) {
                                switch (type) {
                                    default:
                                    case TYPE_GENERAL:
                                        BusProvider.getBus().post(
                                                new VolleyResponseEvent(res.toString()));
                                        break;
                                }
                            }
                        } catch (Exception exc) {
                            BusProvider.getBus().post(
                                    new VolleyResponseEvent(exc.getMessage(), true));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        BusProvider.getBus().post(
                                new VolleyResponseEvent(error.getMessage(), true));
                    }
                }
        );
        Application.addToRequestQueue(req);
    }

}
