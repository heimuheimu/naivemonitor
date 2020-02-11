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

package com.heimuheimu.naivemonitor.prometheus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Prometheus 监控指标样本数据，更多信息请参考文档：<a href="https://prometheus.io/docs/concepts/data_model/">DATA MODEL</a>
 *
 * <p><strong>说明：</strong>PrometheusSample 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class PrometheusSample {

    /**
     * 监控指标为 Histogram 类型的 le 标签
     */
    public static final String HISTOGRAM_LABEL_LE = "le";

    /**
     * 当 Histogram 类型中的样本数据包含此标签（值可以为任意值）时，该样本数据将会以 "<basename>_sum" 的名称导出
     */
    public static final String HISTOGRAM_LABEL_SUM = "sum";

    /**
     * 当 Histogram 类型中的样本数据包含此标签（值可以为任意值）时，该样本数据将会以 "<basename>_count" 的名称导出
     */
    public static final String HISTOGRAM_LABEL_COUNT = "count";

    /**
     * 监控指标为 Summary 类型的 quantile 标签
     */
    public static final String SUMMARY_LABEL_QUANTILE = "quantile";

    /**
     * 当 Summary 类型中的样本数据包含此标签（值可以为任意值）时，该样本数据将会以 "<basename>_sum" 的名称导出
     */
    public static final String SUMMARY_LABEL_SUM = "sum";

    /**
     * 当 Summary 类型中的样本数据包含此标签（值可以为任意值）时，该样本数据将会以 "<basename>_count" 的名称导出
     */
    public static final String SUMMARY_LABEL_COUNT = "count";

    /**
     * 样本数据采集时间戳，如果未设置，值为 -1
     */
    private long timestamp = -1L;

    /**
     * 样本数据数值
     */
    private double value = 0d;

    /**
     * 样本数据标签 Map，Key 为标签名称，Value 为标签值，标签名称必须符合以下正则匹配："[a-zA-Z_][a-zA-Z0-9_]*"，
     * 标签值可以为任意 Unicode 字符
     */
    private Map<String, String> sampleLabels = new LinkedHashMap<>();

    /**
     * 获得样本数据采集时间戳，如果未设置，值为 -1。
     *
     * @return 样本数据采集时间戳，如果未设置，值为 -1
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置样本数据采集时间戳，如果值小于 0，将会清除时间戳的设置。
     *
     * @param timestamp 样本数据采集时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp >= 0 ? timestamp : -1;
    }

    /**
     * 获得样本数据数值。
     *
     * @return 样本数据数值
     */
    public double getValue() {
        return value;
    }

    /**
     * 设置样本数据数值。
     *
     * @param value 样本数据数值
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * 为样本数据添加一个标签，标签名称必须符合以下正则匹配："[a-zA-Z_][a-zA-Z0-9_]*"，标签值可以为任意 Unicode 字符，
     * 如果标签值为 {@code null} 或空，此方法不执行任何操作。
     *
     * @param sampleLabelName  标签名称
     * @param sampleLabelValue 标签值
     * @return 当前 PrometheusSample 实例
     * @throws IllegalArgumentException 如果标签名称不符合正则规则，将会抛出此异常
     */
    public PrometheusSample addSampleLabel(String sampleLabelName, String sampleLabelValue) throws IllegalArgumentException {
        if (sampleLabelValue != null && !sampleLabelValue.isEmpty()) {
            if (sampleLabelName == null || !PrometheusData.LABEL_NAME_PATTERN.matcher(sampleLabelName).matches()) {
                throw new IllegalArgumentException("Invalid sample label name: `" + sampleLabelName + "`. It must match the regex \"[a-zA-Z_][a-zA-Z0-9_]*\".");
            }
            sampleLabels.put(sampleLabelName, sampleLabelValue);
        }
        return this;
    }

    /**
     * 获得样本数据标签 Map，Key 为标签名称，Value 为标签值，该方法不会返回 {@code null}。
     *
     * @return 样本数据标签 Map，Key 为标签名称，Value 为标签值，不会为 {@code null}
     */
    public Map<String, String> getSampleLabels() {
        return sampleLabels;
    }

    /**
     * 设置样本数据标签 Map，Key 为标签名称，Value 为标签值，标签名称必须符合以下正则匹配："[a-zA-Z_][a-zA-Z0-9_]*"，
     * 标签值可以为任意 Unicode 字符，如果标签值为 {@code null} 或空，该标签不会被保存，如果 labels 为 {@code null}，此方法不执行任何操作。
     *
     * @param sampleLabels 监控指标标签 Map，Key 为标签名称，Value 为标签值
     * @throws IllegalArgumentException 如果标签名称不符合正则规则，将会抛出此异常
     */
    public void setSampleLabels(Map<String, String> sampleLabels) throws IllegalArgumentException {
        this.sampleLabels = new LinkedHashMap<>();
        if (sampleLabels != null) {
            for (String labelName : sampleLabels.keySet()) {
                String labelValue = sampleLabels.get(labelName);
                addSampleLabel(labelName, labelValue);
            }
        }
    }

    @Override
    public String toString() {
        return "PrometheusSample{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                ", sampleLabels=" + sampleLabels +
                '}';
    }
}
