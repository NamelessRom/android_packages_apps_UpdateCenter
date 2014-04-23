package org.namelessrom.updatecenter.database;

public class DownloadItem {

    private int    _id;
    private String _downloadId;
    private String _fileName;
    private String _md5;
    private String _completed;

    public DownloadItem() { }

    public DownloadItem(final String fileName, final String downloadId, final String md5,
            final String completed) {
        this._id = -1;
        this._downloadId = downloadId;
        this._fileName = fileName;
        this._md5 = md5;
        this._completed = completed;
    }

    public DownloadItem(final int id, final String fileName, final String downloadId,
            final String md5, final String completed) {
        this._id = id;
        this._downloadId = downloadId;
        this._fileName = fileName;
        this._md5 = md5;
        this._completed = completed;
    }

    public int getId() {
        return this._id;
    }

    public void setId(final int id) {
        this._id = id;
    }

    public String getDownloadId() {
        return _downloadId;
    }

    public void setDownloadId(String _downloadId) {
        this._downloadId = _downloadId;
    }

    public String getFileName() {
        return this._fileName;
    }

    public void setFileName(final String fileName) {
        this._fileName = fileName;
    }

    public String getMd5() {
        return this._md5;
    }

    public void setMd5(final String md5) {
        this._md5 = md5;
    }

    public String getCompleted() {
        return this._completed;
    }

    public void setCompleted(final String completed) {
        this._completed = completed;
    }

}
