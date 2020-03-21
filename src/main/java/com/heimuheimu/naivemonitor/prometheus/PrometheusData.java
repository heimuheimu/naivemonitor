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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Prometheus 监控指标，一个指标可对应多个样本数据(PrometheusSample)，更多信息请参考文档：
 * <a href="https://prometheus.io/docs/concepts/data_model/">DATA MODEL</a>
 *
 * <p><strong>说明：</strong>PrometheusData 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 * @since 1.1
 */
public class PrometheusData {

    /**
     * 监控指标名称正则表达式
     */
    static final Pattern METRIC_NAME_PATTERN = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:]*");

    /**
     * 监控指标标签名称正则表达式
     */
    static final Pattern LABEL_NAME_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    /**
     * 监控指标类型，不允许为 {@code null}
     */
    private final PrometheusType type;

    /**
     * 监控指标名称，例如：http_requests_total，且名称必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     */
    private String metricName = "";

    /**
     * 监控指标帮助文本，如果未设置，值为空
     */
    private String helpText = "";

    /**
     * 监控指标标签 Map，Key 为标签名称，Value 为标签值，标签名称必须符合以下正则匹配："[a-zA-Z_][a-zA-Z0-9_]*"，
     * 标签值可以为任意 Unicode 字符
     */
    private Map<String, String> labels = new LinkedHashMap<>();

    /**
     * 监控指标样本数据列表
     */
    private List<PrometheusSample> sampleList = new ArrayList<>();

    /**
     * 构造一个 PrometheusData 实例。
     *
     * @param type       监控指标类型，不允许为 {@code null}
     * @param metricName 监控指标名称，例如：http_requests_total，必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     * @throws IllegalArgumentException 如果 type 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public PrometheusData(PrometheusType type, String metricName) throws IllegalArgumentException {
        this(type, metricName, "");
    }

    /**
     * 构造一个 PrometheusData 实例。
     *
     * @param type       监控指标类型，不允许为 {@code null}
     * @param metricName 监控指标名称，例如：http_requests_total，必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     * @param helpText   监控指标帮助文本，允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 type 为 {@code null}，将会抛出此异常
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public PrometheusData(PrometheusType type, String metricName, String helpText) throws IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException("Fails to create `PrometheusData`: `type could not be null`.");
        }
        this.type = type;
        setMetricName(metricName);
        setHelpText(helpText);
    }

    /**
     * 获得监控指标类型，不会返回 {@code null}。
     *
     * @return 监控指标类型，不会为 {@code null}
     */
    public PrometheusType getType() {
        return type;
    }

    /**
     * 获得监控指标名称，例如：http_requests_total。
     *
     * @return 监控指标名称
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * 设置监控指标名称，名称必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"。
     *
     * @param metricName 监控指标名称，例如：http_requests_total
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public void setMetricName(String metricName) throws IllegalArgumentException {
        if (metricName == null || !METRIC_NAME_PATTERN.matcher(metricName).matches()) {
            throw new IllegalArgumentException("Invalid metric name: `" + metricName + "`.  It must match the regex \"[a-zA-Z_:][a-zA-Z0-9_:]*\".");
        }
        this.metricName = metricName;
    }

    /**
     * 获得监控指标帮助文本，如果未设置，值为空，不会返回 {@code null}。
     *
     * @return 监控指标帮助文本，可能为空，不会为 {@code null}
     */
    public String getHelpText() {
        return helpText;
    }

    /**
     * 设置监控指标帮助文本。
     *
     * @param helpText 监控指标帮助文本，允许为 {@code null} 或空
     */
    public void setHelpText(String helpText) {
        this.helpText = helpText != null ? helpText : "";
    }

    /**
     * 为监控指标添加一个标签，标签名称必须符合以下正则匹配："[a-zA-Z_][a-zA-Z0-9_]*"，标签值可以为任意 Unicode 字符，如果标签值为
     * {@code null} 或空，此方法不执行任何操作。
     *
     * @param labelName  标签名称
     * @param labelValue 标签值
     * @return 当前 PrometheusData 实例
     * @throws IllegalArgumentException 如果标签名称不符合正则规则，将会抛出此异常
     */
    public PrometheusData addLabel(String labelName, String labelValue) throws IllegalArgumentException {
        if (labelValue != null && !labelValue.isEmpty()) {
            if (labelName == null || !LABEL_NAME_PATTERN.matcher(labelName).matches()) {
                throw new IllegalArgumentException("Invalid label name: `" + labelName + "`. It must match the regex \"[a-zA-Z_][a-zA-Z0-9_]*\".");
            }
            labels.put(labelName, labelValue);
        }
        return this;
    }

    /**
     * 获得监控指标标签 Map，Key 为标签名称，Value 为标签值，该方法不会返回 {@code null}。
     *
     * @return 监控指标 Map，Key 为标签名称，Value 为标签值，不会为 {@code null}
     */
    public Map<String, String> getLabels() {
        return labels;
    }

    /**
     * 设置监控指标标签 Map，Key 为标签名称，Value 为标签值，标签名称必须符合以下正则匹配："[a-zA-Z_][a-zA-Z0-9_]*"，
     * 标签值可以为任意 Unicode 字符，如果标签值为 {@code null} 或空，该标签不会被保存，
     * 如果 labels 为 {@code null}，此方法不执行任何操作。
     *
     * @param labels 监控指标标签 Map，Key 为标签名称，Value 为标签值
     * @throws IllegalArgumentException 如果标签名称不符合正则规则，将会抛出此异常
     */
    public void setLabels(Map<String, String> labels) throws IllegalArgumentException {
        this.labels = new LinkedHashMap<>();
        if (labels != null) {
            for (String labelName : labels.keySet()) {
                String labelValue = labels.get(labelName);
                addLabel(labelName, labelValue);
            }
        }
    }

    /**
     * 为监控指标添加一个样本数据，如果 sample 为 {@code null}，此方法不执行任何操作。
     *
     * @param sample 样本数据
     * @return 当前 PrometheusData 实例
     */
    public PrometheusData addSample(PrometheusSample sample) {
        if (sample != null) {
            sampleList.add(sample);
        }
        return this;
    }

    /**
     * 获得监控指标样本数据列表，该方法不会返回 {@code null}。
     *
     * @return 样本数据列表
     */
    public List<PrometheusSample> getSampleList() {
        return sampleList;
    }

    /**
     * 设置监控指标样本数据列表，如果 sampleList 为 {@code null}，将会清空当前监控指标样本数据列表。
     *
     * @param sampleList 样本数据列表
     */
    public void setSampleList(List<PrometheusSample> sampleList) {
        this.sampleList = new ArrayList<>();
        if (sampleList != null) {
            for (PrometheusSample sample : sampleList) {
                if (sample != null) {
                    this.sampleList.add(sample);
                }
            }
        }
    }

    /**
     * 创建一个监控指标类型为 Counter 的 PrometheusData 实例。
     *
     * @param metricName 监控指标名称，例如：http_requests_total，必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     * @param helpText   监控指标帮助文本，允许为 {@code null} 或空
     * @return PrometheusData 实例
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public static PrometheusData buildCounter(String metricName, String helpText) throws IllegalArgumentException {
        return new PrometheusData(PrometheusType.Counter, metricName, helpText);
    }

    /**
     * 创建一个监控指标类型为 Gauge 的 PrometheusData 实例。
     *
     * @param metricName 监控指标名称，例如：http_requests_total，必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     * @param helpText   监控指标帮助文本，允许为 {@code null} 或空
     * @return PrometheusData 实例
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public static PrometheusData buildGauge(String metricName, String helpText) throws IllegalArgumentException {
        return new PrometheusData(PrometheusType.Gauge, metricName, helpText);
    }

    /**
     * 创建一个监控指标类型为 Histogram 的 PrometheusData 实例。
     *
     * @param metricName 监控指标名称，例如：http_requests_total，必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     * @param helpText   监控指标帮助文本，允许为 {@code null} 或空
     * @return PrometheusData 实例
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public static PrometheusData buildHistogram(String metricName, String helpText) throws IllegalArgumentException {
        return new PrometheusData(PrometheusType.Histogram, metricName, helpText);
    }

    /**
     * 创建一个监控指标类型为 Summary 的 PrometheusData 实例。
     *
     * @param metricName 监控指标名称，例如：http_requests_total，必须符合以下正则匹配："[a-zA-Z_:][a-zA-Z0-9_:]*"
     * @param helpText   监控指标帮助文本，允许为 {@code null} 或空
     * @return PrometheusData 实例
     * @throws IllegalArgumentException 如果监控指标名称不符合正则规则，将会抛出此异常
     */
    public static PrometheusData buildSummary(String metricName, String helpText) throws IllegalArgumentException {
        return new PrometheusData(PrometheusType.Summary, metricName, helpText);
    }

    @Override
    public String toString() {
        return "PrometheusData{" +
                "type=" + type +
                ", metricName='" + metricName + '\'' +
                ", helpText='" + helpText + '\'' +
                ", labels=" + labels +
                ", sampleList=" + sampleList +
                '}';
    }
}
