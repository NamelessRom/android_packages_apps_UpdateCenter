package org.namelessrom.updatecenter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHandler.class.getName();

    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_NAME    = "UpdateCenter.db";

    private static final String KEY_ID         = "id";
    private static final String KEY_DOWNLOADID = "downloadid";
    private static final String KEY_FILENAME   = "filename";
    private static final String KEY_MD5        = "md5";
    private static final String KEY_COMPLETED  = "completed";

    public static final String TABLE_DOWNLOADS = "downloads";

    private static final String CREATE_DOWNLOADS_TABLE = "CREATE TABLE " + TABLE_DOWNLOADS + '('
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DOWNLOADID + " TEXT," + KEY_FILENAME + " TEXT,"
            + KEY_MD5 + " TEXT," + KEY_COMPLETED + " TEXT)";
    private static final String DROP_DOWNLOADS_TABLE   = "DROP TABLE IF EXISTS " + TABLE_DOWNLOADS;

    private static DatabaseHandler sDatabaseHandler = null;

    private DatabaseHandler(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHandler getInstance(final Context context) {
        if (sDatabaseHandler == null) {
            sDatabaseHandler = new DatabaseHandler(context);
        }
        return sDatabaseHandler;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(CREATE_DOWNLOADS_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.e(TAG, "onUpgrade"
                + " | oldVersion: " + String.valueOf(oldVersion)
                + " | newVersion: " + String.valueOf(newVersion));
        int currentVersion = oldVersion;

        if (currentVersion < 1) {
            db.execSQL(DROP_DOWNLOADS_TABLE);
            db.execSQL(CREATE_DOWNLOADS_TABLE);
            currentVersion = 1;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "onDowngrade"
                + " | oldVersion: " + String.valueOf(oldVersion)
                + " | newVersion: " + String.valueOf(newVersion));
        // TODO: a more grateful way?
        db.execSQL(DROP_DOWNLOADS_TABLE);
        onCreate(db);
        /*try {
            new File(Application.applicationContext.getFilesDir() + DC_DOWNGRADE)
                    .createNewFile();
        } catch (Exception ignored) { }*/
    }

    //==============================================================================================
    // All CRUD(Create, Read, Update, Delete) Operations
    //==============================================================================================

    public boolean addItem(final DownloadItem item, final String tableName) {
        final SQLiteDatabase db = getWritableDatabase();

        if (db == null) return false;

        final ContentValues values = new ContentValues();
        values.put(KEY_DOWNLOADID, item.getDownloadId());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_MD5, item.getMd5());
        values.put(KEY_COMPLETED, item.getCompleted());

        db.insert(tableName, null, values);
        db.close();
        return true;
    }

    public DownloadItem getItem(final int id, final String tableName) {
        final SQLiteDatabase db = getReadableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.query(tableName, new String[]{
                        KEY_ID, KEY_DOWNLOADID, KEY_FILENAME, KEY_MD5, KEY_COMPLETED},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        cursor.close();
        db.close();
        return new DownloadItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4));
    }

    public DownloadItem getDownloadItem(final String downloadId) {
        final SQLiteDatabase db = getReadableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.query(TABLE_DOWNLOADS, new String[]{
                        KEY_ID, KEY_DOWNLOADID, KEY_FILENAME, KEY_MD5, KEY_COMPLETED},
                KEY_DOWNLOADID + "=?",
                new String[]{downloadId}, null, null, null, null
        );
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        cursor.close();
        db.close();
        return new DownloadItem(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4));
    }

    public List<DownloadItem> getAllItems(final String tableName) {
        final List<DownloadItem> itemList = new ArrayList<DownloadItem>();
        final String selectQuery = "SELECT * FROM " + tableName;

        final SQLiteDatabase db = getWritableDatabase();

        if (db == null) return null;

        final Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            DownloadItem item;
            do {
                item = new DownloadItem();
                item.setId(Integer.parseInt(cursor.getString(0)));
                item.setDownloadId(cursor.getString(1));
                item.setFileName(cursor.getString(2));
                item.setMd5(cursor.getString(3));
                item.setCompleted(cursor.getString(4));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return itemList;
    }

    public void insertOrUpdate(final DownloadItem item, final String tableName) {
        deleteItem(item, tableName);
        addItem(item, tableName);
    }

    public int updateItem(final DownloadItem item, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return -1;

        final ContentValues values = new ContentValues();
        values.put(KEY_DOWNLOADID, item.getDownloadId());
        values.put(KEY_FILENAME, item.getFileName());
        values.put(KEY_MD5, item.getMd5());
        values.put(KEY_COMPLETED, item.getCompleted());

        final int id = db.update(tableName, values, KEY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        db.close();
        return id;
    }

    public boolean deleteItem(final DownloadItem item, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, KEY_DOWNLOADID + " = ?", new String[]{item.getDownloadId()});
        db.close();
        return true;
    }

    public boolean deleteItemById(final int ID, final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, KEY_ID + " = ?", new String[]{String.valueOf(ID)});
        db.close();
        return true;
    }

    public int getTableCount(final String tableName) {
        final String countQuery = "SELECT  * FROM " + tableName;
        final SQLiteDatabase db = this.getReadableDatabase();

        if (db == null) return -1;

        final Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();

        cursor.close();
        db.close();
        return count;
    }

    public boolean deleteAllItems(final String tableName) {
        final SQLiteDatabase db = this.getWritableDatabase();

        if (db == null) return false;

        db.delete(tableName, null, null);
        db.close();
        return true;
    }
}
