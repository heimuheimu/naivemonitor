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

package com.heimuheimu.naivemonitor.falcon;

import com.heimuheimu.naivemonitor.MonitorUtil;
import com.heimuheimu.naivemonitor.http.NaiveHttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code FalconReporter} 会周期性的采集各 {@link FalconDataCollector} 中的监控数据，将其推送至 Falcon 系统，周期由 {@link FalconDataCollector} 自行定义。
 * <p>
 *     关于 Falcon 系统的更多信息请参考文档：
 *     <a href="https://book.open-falcon.org/zh/usage/data-push.html">https://book.open-falcon.org/zh/usage/data-push.html</a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code FalconReporter} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @see FalconDataCollector
 * @author heimuheimu
 */
public class FalconReporter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FalconReporter.class);

    /**
     * 当前机器名
     */
    private final String endpoint;

    /**
     * 用于接收监控数据的 Falcon 接口 URL 地址
     */
    private final String pushUrl;

    /**
     * 监控数据收集器列表
     */
    private final List<FalconDataCollector> falconDataCollectorList;

    /**
     * 监控数据定时上报任务执行器
     */
    private ScheduledExecutorService executorService = null;

    /**
     * 构造一个 {@code FalconReporter} 实例，Endpoint 默认为机器名。
     *
     * @param pushUrl 用于接收监控数据的 Falcon 接口 URL 地址，不允许为 {@code null}
     * @param falconDataCollectorList 监控数据收集器列表，不允许为 {@code null}
     */
    public FalconReporter(String pushUrl, List<FalconDataCollector> falconDataCollectorList) {
        this (pushUrl, falconDataCollectorList, null);
    }

    /**
     * 构造一个 {@code FalconReporter} 实例，Endpoint 默认为机器名，并会根据该机器名尝试从别名 Map 中获取对应的别名。
     *
     * @param pushUrl 用于接收监控数据的 Falcon 接口 URL 地址，不允许为 {@code null}
     * @param falconDataCollectorList 监控数据收集器列表，不允许为 {@code null}
     * @param endpointAliasMap Endpoint 别名 Map，Key 为机器名， Value 为别名，允许为 {@code null}
     * @throws IllegalArgumentException 如果 falconDataCollectorList 为空或者为 {@code null}
     */
    public FalconReporter(String pushUrl, List<FalconDataCollector> falconDataCollectorList, Map<String, String> endpointAliasMap) throws IllegalArgumentException {
        if (falconDataCollectorList == null || falconDataCollectorList.isEmpty()) {
            LOGGER.error("Construct FalconReporter failed: `falconDataCollectorList is null`. Url: `" + pushUrl + "`.");
            throw new IllegalArgumentException("Construct FalconReporter failed: `falconDataCollectorList is null`. Url: `" + pushUrl + "`.");
        }
        this.pushUrl = pushUrl;
        this.falconDataCollectorList = falconDataCollectorList;
        String endpoint = MonitorUtil.getLocalHostName();
        if (endpointAliasMap != null && endpointAliasMap.containsKey(endpoint)) {
            endpoint = endpointAliasMap.get(endpoint);
        }
        this.endpoint = endpoint;
    }

    /**
     * 执行 {@code FalconReporter} 初始化操作。
     */
    public synchronized void init() {
        if (executorService == null) {
            long startTime = System.currentTimeMillis();
            Map<Integer, List<FalconDataCollector>> collectorMap = new HashMap<>();
            for (FalconDataCollector collector : falconDataCollectorList) {
                int period = collector.getPeriod();
                List<FalconDataCollector> samePeriodCollectors = collectorMap.get(period);
                if (samePeriodCollectors == null) {
                    samePeriodCollectors = new ArrayList<>();
                    collectorMap.put(period, samePeriodCollectors);
                }
                samePeriodCollectors.add(collector);
            }

            int poolSize = Math.min(3, collectorMap.size()); //执行上报数据任务的线程数最多不超过 3 个

            executorService = Executors.newScheduledThreadPool(poolSize, new ThreadFactory() {

                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("naivemonitor-falcon-reporter-" + threadNumber.getAndIncrement());
                    t.setDaemon(true);
                    if (t.getPriority() != Thread.NORM_PRIORITY)
                        t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }

            });

            for (int period : collectorMap.keySet()) {
                executorService.scheduleAtFixedRate(new ReportTask(endpoint, pushUrl, collectorMap.get(period)), period, period, TimeUnit.SECONDS);
            }
            LOGGER.info("FalconReporter has been initialized. Cost: `{} ms`. Endpoint: `{}`. Push url: `{}`. Pool size: `{}`. Collector map: `{}`.",
                    System.currentTimeMillis() - startTime, endpoint, pushUrl, poolSize, collectorMap);
        }
    }

    /**
     * 执行 {@code FalconReporter} 关闭操作，将会关闭 {@code FalconReporter} 使用的线程池，释放资源。
     */
    @Override
    public synchronized void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 监控数据上报任务。
     */
    private static class ReportTask implements Runnable {

        /**
         * 当前机器名
         */
        private final String endpoint;

        /**
         * 用于接收监控数据的 Falcon 接口 URL 地址
         */
        private final String pushUrl;

        /**
         * 监控数据收集器列表
         */
        private final List<FalconDataCollector> falconDataCollectorList;

        public ReportTask(String endpoint, String pushUrl, List<FalconDataCollector> falconDataCollectorList) {
            this.endpoint = endpoint;
            this.pushUrl = pushUrl;
            this.falconDataCollectorList = falconDataCollectorList;
        }

        @Override
        public void run() {
            List<FalconData> pushDataList = new ArrayList<>();
            for (FalconDataCollector falconDataCollector : falconDataCollectorList) {
                try {
                    pushDataList.addAll(falconDataCollector.getList());
                } catch (Exception e) {
                    LOGGER.error("Get falcon data list failed: `" + e.getMessage() + "`. Collector: `" +
                            falconDataCollector + "`.", e);
                }
            }

            if (!pushDataList.isEmpty()) {
                try {
                    for (FalconData pushData : pushDataList) { //所有采集到的监控数据重新设置 endpoint
                        pushData.setEndpoint(endpoint);
                    }
                    String pushJsonData = toJson(pushDataList);

                    NaiveHttpPost httpPost = new NaiveHttpPost(pushUrl, 5000);
                    httpPost.doPost(pushJsonData);

                    int responseCode = httpPost.getUrlConnection().getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        LOGGER.error("Push falcon data list failed: `error response code [{}]`. Url: `{}`.",
                                responseCode, pushUrl);
                    }
                } catch (Exception e) {
                    LOGGER.error("Push falcon data list failed: `" + e.getMessage() + "`. Url: `" + pushUrl + "`.", e);
                }
            }
        }

        /**
         * 将监控数据列表转换为 Json 格式字符串后返回
         *
         * @param pushDataList 监控数据列表
         * @return 监控数据列表对应的 Json 格式字符串
         */
        private String toJson(List<FalconData> pushDataList) {
            StringBuilder buffer = new StringBuilder("[");
            if (!pushDataList.isEmpty()) {
                for (FalconData pushData : pushDataList) {
                    buffer.append(pushData.toJson()).append(",");
                }
                buffer.deleteCharAt(buffer.length() - 1);
            }
            return buffer.append("]").toString();
        }

    }
}
