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

package com.heimuheimu.naivemonitor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 长度统计信息，通常用于统计字节长度
 * <p>大小将以 2 的次方为区间进行统计</p>
 * <p>当前实现是线程安全的</p>
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class SizeInfo {

    /**
     * 累计长度，例如总字节长度
     */
    private final AtomicLong size = new AtomicLong();

    /**
     * 累计统计次数
     */
    private final AtomicLong count = new AtomicLong();

    /**
     * 长度大小区间，以 2 的次方为区间，该数组长度为 32
     */
    private final long[] sizeLimits;

    /**
     * 对应不同长度区间的统计次数，该数组长度为 32
     */
    private final AtomicLong[] sizeCounts;

    /**
     * 构造一个长度统计信息，通常用于统计字节长度
     */
    public SizeInfo() {
        sizeLimits = new long[32];
        long limit = 1;
        for (int i = 0; i < 32; i++) {
            limit *= 2;
            sizeLimits[i] = limit;
        }
        sizeCounts = new AtomicLong[33];
        for (int i = 0; i < 33; i++) {
            sizeCounts[i] = new AtomicLong();
        }
    }

    /**
     * 新增一个长度统计
     *
     * @param size 长度
     */
    public void add(long size) {
        this.size.addAndGet(size);
        this.count.incrementAndGet();
        int i = 0;
        for (long sizeLimit : sizeLimits) {
            if (size < sizeLimit) {
                break;
            } else {
                i++;
            }
        }
        sizeCounts[i].incrementAndGet();
    }

    /**
     * 获得累计长度，例如总字节长度
     *
     * @return 累计长度，例如总字节长度
     */
    public long getSize() {
        return size.get();
    }

    /**
     * 获得累计统计次数
     *
     * @return 累计统计次数
     */
    public long getCount() {
        return count.get();
    }

    /**
     * 获得长度大小区间，以 2 的次方为区间，该数组长度为 32
     *
     * @return 长度大小区间，以 2 的次方为区间，该数组长度为 32
     */
    public long[] getSizeLimits() {
        return Arrays.copyOf(sizeLimits, sizeLimits.length);
    }

    /**
     * 获得对应不同长度区间的统计次数，该数组长度为 32
     *
     * @return 对应不同长度区间的统计次数，该数组长度为 32
     */
    public AtomicLong[] getSizeCounts() {
        return Arrays.copyOf(sizeCounts, sizeCounts.length);
    }

    @Override
    public String toString() {
        return "SizeInfo{" +
                "size=" + size +
                ", count=" + count +
                ", sizeLimits=" + Arrays.toString(sizeLimits) +
                ", sizeCounts=" + Arrays.toString(sizeCounts) +
                '}';
    }

}
