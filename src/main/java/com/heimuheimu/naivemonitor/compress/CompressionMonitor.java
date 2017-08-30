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

package com.heimuheimu.naivemonitor.compress;

import com.heimuheimu.naivemonitor.ExecutionTimeInfo;
import com.heimuheimu.naivemonitor.SizeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 压缩信息统计
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class CompressionMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(CompressionMonitor.class);

    /**
     * 压缩（解压）字节数统计信息
     */
    private final CompressionInfo compressionInfo = new CompressionInfo();

    /**
     * 被压缩内容字节长度统计信息
     */
    private final SizeInfo sizeInfo = new SizeInfo();

    /**
     * 压缩执行时间统计信息
     */
    private final ExecutionTimeInfo compressExecutionTimeInfo = new ExecutionTimeInfo();

    /**
     * 解压执行时间统计信息
     */
    private final ExecutionTimeInfo decompressExecutionTimeInfo = new ExecutionTimeInfo();

    /**
     * 增加一个被压缩内容字节长度统计信息
     * <p>注意：该方法不会抛出任何异常</p>
     *
     * @param size 被压缩内容字节长度
     */
    public void addSize(long size) {
        try {
            sizeInfo.add(size);
        } catch (Exception e) {
            //should not happen
            LOG.error("Unexpected error. Size: `" + size + "`.", e);
        }
    }

    /**
     * 增加一个压缩操作统计
     * <p>注意：该方法不会抛出任何异常</p>
     *
     * @param preCompressed 压缩前内容字节长度
     * @param compressed 压缩后内容字节长度
     * @param startTime 压缩操作开始时间
     */
    public void addCompress(long preCompressed, long compressed, long startTime) {
        try {
            compressionInfo.addCompress(preCompressed, compressed);
            compressExecutionTimeInfo.add(startTime);
        } catch (Exception e) {
            //should not happen
            LOG.error("Unexpected error. PreCompressed: `" + preCompressed + "`. Compressed: `"
                    + compressed + "`. Start time: `" + startTime + "`.", e);
        }
    }

    /**
     * 增加一个解压操作统计
     * <p>注意：该方法不会抛出任何异常</p>
     *
     * @param preDecompressed 解压前内容字节长度
     * @param decompressed 解压后内容字节长度
     * @param startTime 解压操作开始时间
     */
    public void addDecompress(long preDecompressed, long decompressed, long startTime) {
        try {
            compressionInfo.addDecompress(preDecompressed, decompressed);
            decompressExecutionTimeInfo.add(startTime);
        } catch (Exception e) {
            //should not happen
            LOG.error("Unexpected error. PreDecompressed: `" + preDecompressed + "`. Decompressed: `"
                    + decompressed + "`. Start time: `" + startTime + "`.", e);
        }
    }

    /**
     * 获得压缩（解压）字节数统计信息
     *
     * @return 压缩（解压）字节数统计信息
     */
    public CompressionInfo getCompressionInfo() {
        return compressionInfo;
    }

    /**
     * 获得被压缩内容字节长度统计信息
     *
     * @return 被压缩内容字节长度统计信息
     */
    public SizeInfo getSizeInfo() {
        return sizeInfo;
    }

    /**
     * 获得压缩执行时间统计信息
     *
     * @return 压缩执行时间统计信息
     */
    public ExecutionTimeInfo getCompressExecutionTimeInfo() {
        return compressExecutionTimeInfo;
    }

    /**
     * 获得解压执行时间统计信息
     *
     * @return 解压执行时间统计信息
     */
    public ExecutionTimeInfo getDecompressExecutionTimeInfo() {
        return decompressExecutionTimeInfo;
    }

}
