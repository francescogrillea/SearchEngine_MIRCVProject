package org.common;

public class DocInfo {

    private final int pid;
    private final long length;

    public DocInfo(int pid, long length) {
        this.pid = pid;
        this.length = length;
    }

    public int getPid() {
        return pid;
    }

    public long getLength() {
        return length;
    }

}
