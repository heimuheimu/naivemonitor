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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Socket 信息统计
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class SocketMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketMonitor.class);

    private final SocketInfo GLOBAL_INFO = new SocketInfo("");

    private final ConcurrentHashMap<String, SocketInfo> HOST_SOCKET_INFO_MAP = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    private SocketMonitor() {
        //private constructor
    }

    /**
     * 添加 Socket 单次读操作读取的字节长度统计
     * <p>注意：该方法不会抛出任何异常</p>
     *
     * @param host Socket 连接目标地址
     * @param size Socket 读字节长度
     */
    public void addRead(String host, long size) {
        try {
            GLOBAL_INFO.addRead(size);
            SocketInfo socketInfo = get(host);
            socketInfo.addRead(size);
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Host: `" + host + "`, Size: `"
                    + size + "`.", e);
        }
    }

    /**
     * 添加 Socket 单次写操作写入的字节长度统计
     * <p>注意：该方法不会抛出任何异常</p>
     *
     * @param host Socket 连接目标地址
     * @param size Socket 写字节长度
     */
    public void addWrite(String host, long size) {
        try {
            GLOBAL_INFO.addWrite(size);
            SocketInfo socketInfo = get(host);
            socketInfo.addWrite(size);
        } catch (Exception e) {
            //should not happen
            LOGGER.error("Unexpected error. Host: `" + host + "`, Size: `"
                    + size + "`.", e);
        }
    }

    /**
     * 获得全局 Socket 统计信息
     *
     * @return 全局 Socket 统计信息
     */
    public SocketInfo getGlobalInfo() {
        return GLOBAL_INFO;
    }

    /**
     * 获得 Socket 统计信息 Map，Key 为 Socket 连接目标地址，Value 为该地址对应的 Socket 统计信息
     * <p>注意：全局 Socket 统计信息的 Key 为空字符串</p>
     *
     * @return Socket 统计信息 Map
     */
    public Map<String, SocketInfo> get() {
        HashMap<String, SocketInfo> socketInfoHashMap = new HashMap<>(HOST_SOCKET_INFO_MAP);
        socketInfoHashMap.put("", GLOBAL_INFO);
        return socketInfoHashMap;
    }

    private SocketInfo get(String host) {
        SocketInfo socketInfo = HOST_SOCKET_INFO_MAP.get(host);
        if (socketInfo == null) {
            synchronized (lock) {
                socketInfo = HOST_SOCKET_INFO_MAP.get(host);
                if (socketInfo == null) {
                    socketInfo = new SocketInfo(host);
                    HOST_SOCKET_INFO_MAP.put(host, socketInfo);
                }
            }
        }
        return socketInfo;
    }

}
