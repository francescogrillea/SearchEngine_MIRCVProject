package org.common;

import java.nio.ByteBuffer;

public class DocInfo {
    private final int pid;
    private final int length;
    static final int BYTES = Integer.BYTES + Integer.BYTES;

    public DocInfo(int pid, int length) {
        this.pid = pid;
        this.length = length;
    }

    public DocInfo(ByteBuffer buffer){
        this.pid = buffer.getInt();
        this.length = buffer.getInt();
    }

    public int getPid() {
        return pid;
    }

    public int getLength() {
        return length;
    }

    public ByteBuffer serialize(){
        ByteBuffer buffer = ByteBuffer.allocate(BYTES);
        buffer.putInt(this.pid);
        buffer.putInt(this.length);
        buffer.flip();
        return buffer;
    }

    @Override
    public String toString() {
        return "DocInfo{" +
                "pid=" + pid +
                ", length=" + length +
                '}';
    }
}
