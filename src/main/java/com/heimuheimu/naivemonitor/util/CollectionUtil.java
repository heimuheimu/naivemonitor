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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NaiveMonitor 项目使用的集合工具类。
 *
 * @author heimuheimu
 * @since  1.1
 */
public class CollectionUtil {

    /**
     * 根据 Key 前缀从目标 Map 中获得对应的 Value 列表，如果 prefix 为 {@code null} 或空，将会返回所有值列表，该方法不会返回 {@code null}。
     *
     * @param source 目标 Map，如果为 {@code null}，将会返回空列表
     * @param prefix Key 前缀，如果为 {@code null} 或空，将会返回所有值列表
     * @param <T> 值类型
     * @return 值列表
     */
    public static <T> List<T> getListByPrefix(Map<String, T> source, String prefix) {
        if (source == null) {
            return new ArrayList<>();
        }
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>(source.values());
        } else {
            List<T> result = new ArrayList<>();
            for (String name : source.keySet()) {
                if (name.startsWith(prefix)) {
                    result.add(source.get(name));
                }
            }
            return result;
        }
    }
}
