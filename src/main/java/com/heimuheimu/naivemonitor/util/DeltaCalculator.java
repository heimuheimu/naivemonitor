/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 heimuheimu
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

package com.heimuheimu.naivemonitor.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 差值计算器。
 *
 * <p><strong>说明：</strong>DeltaCalculator 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class DeltaCalculator {

    private final ConcurrentHashMap<String, Double> valueMap = new ConcurrentHashMap<>();

    /**
     * 计算该类型的值与上一次值的差值，如果为第一次计算，则直接返回当前值。
     *
     * @param key 每个类型的值需拥有一个唯一的 Key，不允许为 {@code null} 或空
     * @param value 当前值
     * @return 差值
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     */
    public double delta(String key, double value) throws IllegalArgumentException {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Fails to calculate delta: `key could not be null or empty`");
        }
        Double lastValue = valueMap.put(key, value);
        return lastValue != null ? value - lastValue : value;
    }
}
