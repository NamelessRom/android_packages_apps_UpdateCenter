package org.namelessrom.updatecenter.database;

public class DownloadItem {

    private int     _id;
    private String  _downloadId;
    private String  _fileName;
    private String  _md5;
    private String  _completed;
    private boolean _is_paused;

    public DownloadItem() { }

    public DownloadItem(final String downloadId, final String fileName, final String md5,
            final String completed) {
        this(-1, downloadId, fileName, md5, completed, false);
    }

    public DownloadItem(final int id, final String downloadId, final String fileName,
            final String md5, final String completed) {
        this(id, downloadId, fileName, md5, completed, false);
    }

    public DownloadItem(final int id, final String downloadId, final String fileName,
            final String md5, final String completed, final boolean isPaused) {
        this._id = id;
        this._downloadId = downloadId;
        this._fileName = fileName;
        this._md5 = md5;
        this._completed = completed;
        this._is_paused = isPaused;
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

    public boolean isPaused() {
        return _is_paused;
    }

    public void setPaused(final boolean _is_paused) {
        this._is_paused = _is_paused;
    }
}
