package org.namelessrom.updatecenter.events;

public class VolleyResponseEvent {

    private final String  mOutput;
    private final boolean mIsError;

    public VolleyResponseEvent(final String output) { this(output, false); }

    public VolleyResponseEvent(final String output, final boolean isError) {
        mOutput = output;
        mIsError = isError;
    }

    public String getOutput() { return mOutput; }

    public boolean isError() { return mIsError; }
}
