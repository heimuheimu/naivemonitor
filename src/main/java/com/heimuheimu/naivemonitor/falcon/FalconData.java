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

/**
 * Falcon 监控数据项，字段含义请参考文档：
 * <p>
 *     <a href="https://book.open-falcon.org/zh/usage/data-push.html">https://book.open-falcon.org/zh/usage/data-push.html</a>
 * </p>
 *
 * @author heimuheimu
 */
public class FalconData {

    /**
     * 标明 Metric 的主体(属主)，比如 metric 是 cpu_idle，那么 Endpoint 就表示这是哪台机器的 cpu_idle
     */
    private String endpoint;

    /**
     * 最核心的字段，代表这个采集项具体度量的是什么, 比如是 cpu_idle 呢，还是 memory_free, 还是 qps
     */
    private String metric;

    /**
     * 表示汇报该数据时的 unix 时间戳，注意是整数，代表的是秒
     */
    private long timestamp;

    /**
     * 表示该数据采集项的汇报周期，这对于后续的配置监控策略很重要，必须明确指定
     */
    private int step;

    /**
     * 代表该 metric 在当前时间点的值，float64
     */
    private double value;

    /**
     * 只能是 COUNTER 或者 GAUGE 二选一，前者表示该数据采集项为计时器类型，后者表示其为原值 (注意大小写)
     * <ul>
     *     <li>GAUGE：即用户上传什么样的值，就原封不动的存储</li>
     *     <li>COUNTER：指标在存储和展现的时候，会被计算为 speed，即（当前值 - 上次值）/ 时间间隔</li>
     * </ul>
     */
    private String counterType = "GAUGE";

    /**
     * 一组逗号分割的键值对, 对 metric 进一步描述和细化, 可以是空字符串. 比如 idc=lg，比如 service=xbox 等，多个 tag 之间用逗号分割
     */
    private String tags = "";

    /**
     * 获得 Metric 的主体(属主)，比如 metric 是 cpu_idle，那么 Endpoint 就表示这是哪台机器的 cpu_idle
     *
     * @return Metric 的主体(属主)
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * 设置 Metric 的主体(属主)，比如 metric 是 cpu_idle，那么 Endpoint 就表示这是哪台机器的 cpu_idle
     *
     * @param endpoint Metric 的主体(属主)
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 获得 Metric，最核心的字段，代表这个采集项具体度量的是什么, 比如是 cpu_idle 呢，还是 memory_free, 还是 qps
     *
     * @return Metric
     */
    public String getMetric() {
        return metric;
    }

    /**
     * 设置 Metric，最核心的字段，代表这个采集项具体度量的是什么, 比如是 cpu_idle 呢，还是 memory_free, 还是 qps
     *
     * @param metric Metric
     */
    public void setMetric(String metric) {
        this.metric = metric;
    }

    /**
     * 获得汇报该数据时的 unix 时间戳，注意是整数，代表的是秒
     *
     * @return 汇报该数据时的 unix 时间戳，注意是整数，代表的是秒
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置汇报该数据时的 unix 时间戳，注意是整数，代表的是秒
     *
     * @param timestamp 汇报该数据时的 unix 时间戳，注意是整数，代表的是秒
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获得该数据采集项的汇报周期，这对于后续的配置监控策略很重要，必须明确指定
     *
     * @return 数据采集项的汇报周期
     */
    public int getStep() {
        return step;
    }

    /**
     * 设置该数据采集项的汇报周期，这对于后续的配置监控策略很重要，必须明确指定
     *
     * @param step 数据采集项的汇报周期
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * 获得该 metric 在当前时间点的值，float64
     *
     * @return 该 metric 在当前时间点的值，float64
     */
    public double getValue() {
        return value;
    }

    /**
     * 设置该 metric 在当前时间点的值，float64
     *
     * @param value 该 metric 在当前时间点的值，float64
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * 获得 CounterType，只能是 COUNTER 或者 GAUGE 二选一，前者表示该数据采集项为计时器类型，后者表示其为原值 (注意大小写)
     * <ul>
     *     <li>GAUGE：即用户上传什么样的值，就原封不动的存储</li>
     *     <li>COUNTER：指标在存储和展现的时候，会被计算为 speed，即（当前值 - 上次值）/ 时间间隔</li>
     * </ul>
     *
     * @return CounterType
     */
    public String getCounterType() {
        return counterType;
    }

    /**
     * 设置 CounterType，只能是 COUNTER 或者 GAUGE 二选一，前者表示该数据采集项为计时器类型，后者表示其为原值 (注意大小写)
     * <ul>
     *     <li>GAUGE：即用户上传什么样的值，就原封不动的存储</li>
     *     <li>COUNTER：指标在存储和展现的时候，会被计算为 speed，即（当前值 - 上次值）/ 时间间隔</li>
     * </ul>
     *
     * @param counterType CounterType
     */
    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    /**
     * 获得 Tags，一组逗号分割的键值对, 对 metric 进一步描述和细化, 可以是空字符串. 比如 idc=lg，比如 service=xbox 等，多个 tag 之间用逗号分割
     *
     * @return Tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * 设置 Tags，一组逗号分割的键值对, 对 metric 进一步描述和细化, 可以是空字符串. 比如 idc=lg，比如 service=xbox 等，多个 tag 之间用逗号分割
     *
     * @param tags Tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 将当前 Falcon 监控数据项转换为 Json 格式字符串后返回
     *
     * @return 当前 Falcon 监控数据项对应的 Json 格式字符串
     */
    protected String toJson() {
        return "{\"endpoint\":\"" + endpoint +
                "\",\"metric\":\"" + metric +
                "\",\"timestamp\":" + timestamp +
                ",\"step\":" + step +
                ",\"value\":" + value +
                ",\"counterType\":\"" + counterType +
                "\",\"tags\":\"" + tags + "\"}";
    }

}
