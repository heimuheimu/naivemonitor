/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.naivemonitor.facility;

import com.heimuheimu.naivemonitor.monitor.SocketMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 对 {@link InputStream} 进行封装，通过 {@link SocketMonitor} 监控其所有的读取操作，通常该输入流由 {@link Socket#getInputStream()} 方法获得。
 *
 * <p><strong>说明：</strong>{@code MonitoredSocketInputStream} 是否线程安全取决于被封装的 {@code InputStream} 是否线程安全。</p>
 *
 * @author heimuheimu
 */
public class MonitoredSocketInputStream extends InputStream {

    /**
     * 输入流
     */
    private final InputStream in;

    /**
     * Socket 读、写信息监控器
     */
    private final SocketMonitor socketMonitor;

    /**
     * 构造一个 {@code MonitoredSocketInputStream} 实例。
     *
     * @param in 输入流，不允许为 {@code null}
     * @param socketMonitor Socket 读、写信息监控器，不允许为 {@code null}
     * @throws NullPointerException 如果 {@code in} 或者 {@code socketMonitor} 为 {@code null}，将会抛出此异常
     */
    public MonitoredSocketInputStream(InputStream in, SocketMonitor socketMonitor) throws NullPointerException {
        if (in == null) {
            throw new NullPointerException("Create `MonitoredSocketInputStream` failed: `inputStream could not be null`.");
        }
        if (socketMonitor == null) {
            throw new NullPointerException("Create `MonitoredSocketInputStream` failed: `socketMonitor could not be null`.");
        }
        this.in = in;
        this.socketMonitor = socketMonitor;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int readBytes = in.read(b);
        if (readBytes >= 0) {
            socketMonitor.onRead(readBytes);
        }
        return readBytes;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readBytes = in.read(b, off, len);
        if (readBytes >= 0) {
            socketMonitor.onRead(readBytes);
        }
        return readBytes;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipBytes = in.skip(n);
        if (skipBytes >= 0) {
            socketMonitor.onRead(skipBytes);
        }
        return skipBytes;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readLimit) {
        in.mark(readLimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public int read() throws IOException {
        int value = in.read();
        if (value >= 0) {
            socketMonitor.onRead(1);
        }
        return value;
    }
}
