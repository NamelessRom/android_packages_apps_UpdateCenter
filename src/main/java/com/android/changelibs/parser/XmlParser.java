/*******************************************************************************
 * Copyright (c) 2013 Gabriele Mariotti.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.android.changelibs.parser;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.android.changelibs.Util;
import com.android.changelibs.internal.ChangeLog;
import com.android.changelibs.internal.ChangeLogException;
import com.android.changelibs.internal.ChangeLogRow;
import com.android.changelibs.internal.ChangeLogRowHeader;

import org.namelessrom.updatecenter.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Read and parse res/raw/changelog.xml.
 * Example:
 * <p/>
 * <pre>
 *    XmlParser parse = new XmlParser(this);
 *    ChangeLog log=parse.readChangeLogFile();
 * </pre>
 * <p/>
 * If you want to use a custom xml file, you can use:
 * <pre>
 *    XmlParser parse = new XmlParser(this,R.raw.mycustomfile);
 *    ChangeLog log=parse.readChangeLogFile();
 * </pre>
 * <p/>
 * It is a example for changelog.xml
 * <pre>
 *  <?xml version="1.0" encoding="utf-8"?>
 *       <changelog bulletedList=false>
 *            <changelogversion versionName="1.2" changeDate="20/01/2013">
 *                 <changelogtext>new feature to share data</changelogtext>
 *                 <changelogtext>performance improvement</changelogtext>
 *            </changelogversion>
 *            <changelogversion versionName="1.1" changeDate="13/01/2013">
 *                 <changelogtext>issue on wifi connection</changelogtext>*
 *            </changelogversion>*
 *       </changelog>
 * </pre>
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class XmlParser extends BaseParser {

    private static final String TAG = "XmlParser";

    private String  mChangeLogFileResourceUrl = null;
    private String  mChangeLogFileCachePath   = null;
    private boolean mIsSourceForge            = false;

    private boolean mDebug = false;

    //--------------------------------------------------------------------------------
    //TAGs and ATTRIBUTEs in xml file
    //--------------------------------------------------------------------------------

    private static final String TAG_CHANGELOG            = "changelog";
    private static final String TAG_CHANGELOGVERSION     = "changelogversion";
    private static final String TAG_CHANGELOGTEXT        = "changelogtext";
    private static final String TAG_CHANGELOGBUG         = "changelogbug";
    private static final String TAG_CHANGELOGIMPROVEMENT = "changelogimprovement";

    private static final String ATTRIBUTE_BULLETEDLIST    = "bulletedList";
    private static final String ATTRIBUTE_VERSIONNAME     = "versionName";
    private static final String ATTRIBUTE_VERSIONCODE     = "versionCode";
    private static final String ATTRIBUTE_CHANGEDATE      = "changeDate";
    private static final String ATTRIBUTE_CHANGETEXT      = "changeText";
    private static final String ATTRIBUTE_CHANGETEXTTITLE = "changeTextTitle";

    private static List<String> mChangeLogTags = new ArrayList<String>() {{
        add(TAG_CHANGELOGBUG);
        add(TAG_CHANGELOGIMPROVEMENT);
        add(TAG_CHANGELOGTEXT);
    }};

    //--------------------------------------------------------------------------------
    //Constructors
    //--------------------------------------------------------------------------------

    /**
     * Create a new instance for a context.
     *
     * @param context current Context
     */
    public XmlParser(Context context) {
        super(context);
        mDebug = mContext.getResources().getBoolean(R.bool.changelog_enable_logging);
    }

    /**
     * Create a new instance for a context and with a custom url .
     *
     * @param context                  current Context
     * @param changeLogFileResourceUrl url with xml files
     */
    public XmlParser(Context context, String changeLogFileResourceUrl) {
        super(context);
        mDebug = mContext.getResources().getBoolean(R.bool.changelog_enable_logging);
        this.mChangeLogFileResourceUrl = changeLogFileResourceUrl;

        final Resources res = context.getResources();
        final String path = res.getString(R.string.changelog_cache_path);
        logDebug("Cache path: " + path);

        if (!path.equals("null")) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                this.mChangeLogFileCachePath = Environment.getExternalStorageDirectory()
                        + File.separator + path;
                this.mIsSourceForge = res.getBoolean(R.bool.changelog_is_sourceforge);
                logDebug("mChangeLogFileCachePath: " + this.mChangeLogFileCachePath);
            } else {
                logDebug("No sdcard mounted!");
            }
        }
    }

    //--------------------------------------------------------------------------------

    private void logDebug(String msg) {
        if (mDebug) {
            Log.d(TAG, msg);
        }
    }

    /**
     * Read and parse res/raw/changelog.xml or custom file
     *
     * @return {@link com.android.changelibs.internal.ChangeLog} obj with all data
     * @throws Exception if changelog.xml or custom file is not found or if there are errors on
     *                   parsing
     */
    @Override
    public ChangeLog readChangeLogFile() throws Exception {

        ChangeLog chg;

        try {
            InputStream is = null;

            if (mChangeLogFileResourceUrl != null) {
                if (Util.isConnected(super.mContext)) {
                    File file = null;
                    if (mChangeLogFileCachePath != null) {
                        final String[] tmp = mChangeLogFileResourceUrl.split("/");
                        String filename;
                        if (mIsSourceForge) {
                            // length - 2 as we fetch from sourceforge and it appends /download
                            filename = tmp[tmp.length - 2];
                        } else {
                            filename = tmp[tmp.length - 1];
                        }
                        logDebug("Filename: " + filename);
                        file = new File(mChangeLogFileCachePath + File.separator + filename);
                        logDebug("File: " + file.getAbsolutePath());
                    }
                    final URL url = new URL(mChangeLogFileResourceUrl);
                    if (file != null) {
                        if (file.exists()) {
                            is = new FileInputStream(file);
                            logDebug("Loading changelog from cache!");
                        } else {
                            logDebug("Loading changelog to cache!");
                            is = url.openStream();

                            final OutputStream output = new FileOutputStream(file);
                            final byte[] buffer = new byte[256];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                            output.flush();
                            output.close();
                            is.close();
                            logDebug("Loaded changelog to cache!");

                            is = new FileInputStream(file);
                            logDebug("Loading changelog from cache!");
                        }
                    } else {
                        is = url.openStream();
                        logDebug("Loading changelog from url!");
                    }
                }
            }

            if (is != null) {

                // Create a new XML Pull Parser.
                final XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                parser.nextTag();

                // Create changelog obj that will contain all data
                chg = new ChangeLog();
                // Parse file
                readChangeLogNode(parser, chg);

                // Close inputstream
                is.close();
            } else {
                throw new ChangeLogException("Changelog.xml not found");
            }
        } catch (XmlPullParserException xpe) {
            throw xpe;
        } catch (IOException ioe) {
            throw ioe;
        }

        return chg;
    }


    /**
     * Parse changelog node
     *
     * @param parser
     * @param changeLog
     */
    protected void readChangeLogNode(XmlPullParser parser, ChangeLog changeLog) throws Exception {

        if (parser == null || changeLog == null) return;

        // Parse changelog node
        parser.require(XmlPullParser.START_TAG, null, TAG_CHANGELOG);

        // Read attributes
        final String bulletedList = parser.getAttributeValue(null, ATTRIBUTE_BULLETEDLIST);
        if (bulletedList == null || bulletedList.equals("true")) {
            changeLog.setBulletedList(true);
            super.bulletedList = true;
        } else {
            changeLog.setBulletedList(false);
            super.bulletedList = false;
        }

        //Parse nested nodes
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            final String tag = parser.getName();

            if (tag.equals(TAG_CHANGELOGVERSION)) {
                readChangeLogVersionNode(parser, changeLog);
            }
        }
    }

    /**
     * Parse changeLogVersion node
     *
     * @param parser
     * @param changeLog
     * @throws Exception
     */
    protected void readChangeLogVersionNode(XmlPullParser parser, ChangeLog changeLog)
            throws Exception {

        if (parser == null) return;

        parser.require(XmlPullParser.START_TAG, null, TAG_CHANGELOGVERSION);

        // Read attributes
        final String versionName = parser.getAttributeValue(null, ATTRIBUTE_VERSIONNAME);
        final String versionCode = parser.getAttributeValue(null, ATTRIBUTE_VERSIONCODE);
        final String changeDate = parser.getAttributeValue(null, ATTRIBUTE_CHANGEDATE);
        if (versionName == null) {
            throw new ChangeLogException("VersionName required in changeLogVersion node");
        }

        final ChangeLogRowHeader row = new ChangeLogRowHeader();
        row.setVersionName(versionName);
        row.setVersionCode(versionCode);
        row.setChangeDate(changeDate);
        changeLog.addRow(row);

        // Parse nested nodes
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            final String tag = parser.getName();

            if (mChangeLogTags.contains(tag)) {
                readChangeLogRowNode(parser, changeLog, versionName, versionCode);
            }
        }
    }

    /**
     * Parse changeLogText node
     *
     * @param parser
     * @param changeLog
     * @throws Exception
     */
    private void readChangeLogRowNode(XmlPullParser parser, ChangeLog changeLog, String versionName,
            String versionCode) throws Exception {

        if (parser == null) return;


        final String tag = parser.getName();

        final ChangeLogRow row = new ChangeLogRow();
        row.setVersionName(versionName);
        row.setVersionCode(versionCode);

        // Read attributes
        final String changeLogTextTitle = parser.getAttributeValue(null, ATTRIBUTE_CHANGETEXTTITLE);
        if (changeLogTextTitle != null) { row.setChangeTextTitle(changeLogTextTitle); }

        // It is possible to force bulleted List
        final String bulletedList = parser.getAttributeValue(null, ATTRIBUTE_BULLETEDLIST);
        if (bulletedList != null) {
            if (bulletedList.equals("true")) {
                row.setBulletedList(true);
            } else {
                row.setBulletedList(false);
            }
        } else {
            row.setBulletedList(super.bulletedList);
        }

        // Read text
        if (parser.next() == XmlPullParser.TEXT) {
            final String changeLogText = parser.getText();
            if (changeLogText == null) {
                throw new ChangeLogException("ChangeLogText required in changeLogText node");
            }
            row.parseChangeText(changeLogText);
            row.setType(tag.equalsIgnoreCase(TAG_CHANGELOGBUG) ? ChangeLogRow.BUGFIX
                    : tag.equalsIgnoreCase(TAG_CHANGELOGIMPROVEMENT) ? ChangeLogRow.IMPROVEMENT
                            : ChangeLogRow.DEFAULT);
            parser.nextTag();
        }
        changeLog.addRow(row);

    }

}
