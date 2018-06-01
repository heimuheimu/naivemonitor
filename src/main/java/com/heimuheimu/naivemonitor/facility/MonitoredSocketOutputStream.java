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
import java.io.OutputStream;
import java.net.Socket;

/**
 * 对 {@link OutputStream} 进行封装，通过 {@link SocketMonitor} 监控其所有的写操作，通常该输出流由 {@link Socket#getOutputStream()} 方法获得。
 *
 * <p><strong>说明：</strong>{@code MonitoredSocketOutputStream} 是否线程安全取决于被封装的 {@code OutputStream} 是否线程安全。</p>
 *
 * @author heimuheimu
 */
public class MonitoredSocketOutputStream extends OutputStream {

    /**
     * 输出流
     */
    private final OutputStream out;

    /**
     * Socket 读、写信息监控器
     */
    private final SocketMonitor socketMonitor;

    /**
     * 构造一个 {@code MonitoredSocketOutputStream} 实例。
     *
     * @param out 输出流，不允许为 {@code null}
     * @param socketMonitor Socket 读、写信息监控器，不允许为 {@code null}
     * @throws NullPointerException 如果 {@code out} 或者 {@code socketMonitor} 为 {@code null}，将会抛出此异常
     */
    public MonitoredSocketOutputStream(OutputStream out, SocketMonitor socketMonitor) throws NullPointerException {
        if (out == null) {
            throw new NullPointerException("Create `MonitoredSocketOutputStream` failed: `outputStream could not be null`.");
        }
        if (socketMonitor == null) {
            throw new NullPointerException("Create `MonitoredSocketOutputStream` failed: `socketMonitor could not be null`.");
        }
        this.out = out;
        this.socketMonitor = socketMonitor;
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        socketMonitor.onWritten(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        socketMonitor.onWritten(len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        socketMonitor.onWritten(1);
    }
}
