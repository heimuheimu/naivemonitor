/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 heimuheimu
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

package com.heimuheimu.naivemonitor.socket;

import com.heimuheimu.naivemonitor.SizeInfo;

/**
 * Socket 统计信息
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class SocketInfo {

    /**
     * Socket 连接目标地址，由主机名和端口组成，":"符号分割，例如：localhost:4141
     */
    private final String host;

    /**
     * Socket 读字节长度统计
     */
    private final SizeInfo readSize = new SizeInfo();

    /**
     * Socket 写字节长度统计
     */
    private final SizeInfo writeSize = new SizeInfo();

    /**
     * 构造一个 Socket 统计信息
     *
     * @param host Socket 连接目标地址，由主机名和端口组成，":"符号分割，例如：localhost:4141
     */
    public SocketInfo(String host) {
        this.host = host;
    }

    /**
     * 添加 Socket 单次读操作读取的字节长度统计
     *
     * @param size Socket 读字节长度
     */
    public void addRead(long size) {
        readSize.add(size);
    }

    /**
     * 添加 Socket 单次写操作写入的字节长度统计
     *
     * @param size Socket 写字节长度
     */
    public void addWrite(long size) {
        writeSize.add(size);
    }

    /**
     * 获得 Socket 连接目标地址
     *
     * @return Socket 连接目标地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 获得 Socket 读字节长度统计
     *
     * @return Socket 读字节长度统计
     */
    public SizeInfo getReadSize() {
        return readSize;
    }

    /**
     * 获得 Socket 写字节长度统计
     *
     * @return Socket 写字节长度统计
     */
    public SizeInfo getWriteSize() {
        return writeSize;
    }

    @Override
    public String toString() {
        return "SocketInfo{" +
                "host='" + host + '\'' +
                ", readSize=" + readSize +
                ", writeSize=" + writeSize +
                '}';
    }

}
