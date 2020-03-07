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

package com.heimuheimu.naivemonitor.monitor.hotspot.gc;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import java.lang.management.MemoryUsage;
import java.util.Map;

/**
 * 提供将 GarbageCollectionNotificationInfo 转换为文本的方法，通常在日志打印中使用。
 *
 * @author heimuheimu
 * @since 1.1
 */
public class GarbageCollectionNotificationInfoFormatter {

    /**
     * 将 GarbageCollectionNotificationInfo 转换为文本内容。
     *
     * @param gcNotificationInfo GC 操作完成后的通知信息
     * @return GarbageCollectionNotificationInfo 对应的文本内容
     */
    public static String format(GarbageCollectionNotificationInfo gcNotificationInfo) {
        if (gcNotificationInfo == null) {
            return "null";
        } else {
            StringBuilder buffer = new StringBuilder(512);
            buffer.append("GC name: `").append(gcNotificationInfo.getGcName()).append("`. action: `")
                    .append(gcNotificationInfo.getGcAction()).append("`. cause: `")
                    .append(gcNotificationInfo.getGcCause()).append("`. \n\r");
            GcInfo gcInfo = gcNotificationInfo.getGcInfo();
            if (gcInfo != null) {
                buffer.append(" ---- GC duration: `").append(gcInfo.getDuration()).append("ms`. start time: `")
                        .append(gcInfo.getStartTime()).append("`. end time: `")
                        .append(gcInfo.getEndTime()).append("`. \n\r");
                Map<String, MemoryUsage> beforeGcMap = gcInfo.getMemoryUsageBeforeGc();
                Map<String, MemoryUsage> afterGcMap = gcInfo.getMemoryUsageAfterGc();
                for (String poolName : beforeGcMap.keySet()) {
                    buffer.append("   ---- [").append(poolName).append("] before gc: { ").append(beforeGcMap.get(poolName))
                            .append(" }. after gc: { ").append(afterGcMap.get(poolName)).append(" }\n\r");
                }
                for (String poolName : afterGcMap.keySet()) {
                    if (!beforeGcMap.containsKey(poolName)) {
                        buffer.append("   ---- [").append(poolName).append("] before gc: { null }. after gc: { ")
                                .append(afterGcMap.get(poolName)).append(" }\n\r");
                    }
                }
            }
            return buffer.toString();
        }
    }
}
