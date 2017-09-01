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

import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控器常用方法工具类
 *
 * @author heimuheimu
 * @ThreadSafe
 */
public class MonitorUtil {

    private MonitorUtil() {
        //prevent construct this class
    }

    /**
     * 对目标数值执行 {@link AtomicLong#addAndGet(long)} 操作，如果目标数值执行 add 操作后小于 0，则将其重置为 0
     *
     * @param target 目标数值
     * @param delta add 操作需要增加的数量
     * @return 执行 add 操作后的目标数值大小，不会小于 0
     */
    public static long safeAdd(AtomicLong target, long delta) {
        long value = target.addAndGet(delta);
        if (value >= 0) {
            return value;
        } else {
            target.set(0);
            return 0;
        }
    }

}