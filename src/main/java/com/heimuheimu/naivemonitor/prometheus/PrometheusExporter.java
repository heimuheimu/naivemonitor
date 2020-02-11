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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prometheus 监控指标导出器，当调用 {@link #export()} 方法时，将会依次采集各的 Prometheus 监控指标采集器中的数据，
 * 并将其转换为文本格式后导出，文本格式定义请参考文档：
 * <a href="https://prometheus.io/docs/instrumenting/exposition_formats/">EXPOSITION FORMATS</a>
 *
 * <p><strong>说明：</strong>PrometheusExporter 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see PrometheusHttpWriter
 */
public class PrometheusExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusExporter.class);

    /**
     * Prometheus 文本格式使用的换行符
     */
    private static final char LINE_FEED_CHARACTER = '\n';

    /**
     * 监控指标帮助文本特殊字符转义 Map，Key 为需要转义的字符，Value 为转义后的字符串
     */
    private static final Map<Integer, String> HELP_TEXT_ESCAPE_MAP;

    static {
        HELP_TEXT_ESCAPE_MAP = new HashMap<>();
        HELP_TEXT_ESCAPE_MAP.put((int) '\\', "\\\\");
        HELP_TEXT_ESCAPE_MAP.put((int) '\n', "\\n");
    }

    /**
     * 标签值文本特殊字符转义 Map，Key 为需要转义的字符，Value 为转义后的字符串
     */
    private static final Map<Integer, String> LABEL_VALUE_ESCAPE_MAP;

    static {
        LABEL_VALUE_ESCAPE_MAP = new HashMap<>();
        LABEL_VALUE_ESCAPE_MAP.put((int) '\\', "\\\\");
        LABEL_VALUE_ESCAPE_MAP.put((int) '\n', "\\n");
        LABEL_VALUE_ESCAPE_MAP.put((int) '"', "\\\"");
    }

    /**
     * Prometheus 监控指标采集器列表
     */
    private final List<PrometheusCollector> prometheusCollectorList;

    /**
     * 构造一个 PrometheusExporter 实例。
     *
     * @param prometheusCollectorList Prometheus 监控指标采集器列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果 prometheusCollectorList 为 {@code null} 或空，将会抛出此异常
     */
    public PrometheusExporter(List<PrometheusCollector> prometheusCollectorList) throws IllegalArgumentException {
        if (prometheusCollectorList == null || prometheusCollectorList.isEmpty()) {
            String errorMessage = "Fails to create `PrometheusExporter`: `prometheusCollectorList could not be empty`.";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        this.prometheusCollectorList = prometheusCollectorList;
    }

    /**
     * 依次采集各的 Prometheus 监控数据采集器中的数据，并将其转换为文本格式后导出，文本格式定义请参考文档：
     * <a href="https://prometheus.io/docs/instrumenting/exposition_formats/">EXPOSITION FORMATS</a>
     *
     * @return 所有 Prometheus 监控指标对应的文本
     * @throws IllegalStateException 如果采集或转换过程中出现错误，将会抛出此异常
     */
    public String export() throws IllegalStateException {
        StringBuilder text = new StringBuilder(16 * 1024);
        for (PrometheusCollector collector : prometheusCollectorList) {
            List<PrometheusData> dataList;
            try {
                dataList = collector.getList();
            } catch (Exception e) {
                String errorMessage = "Fails to export prometheus data: `collect failed`. Collector: `" + collector + "`.";
                LOGGER.error(errorMessage, e);
                throw new IllegalStateException(errorMessage, e);
            }
            if (dataList == null || dataList.isEmpty()) {
                String errorMessage = "Fails to export prometheus data: `empty data list`. Collector: `" + collector + "`.";
                LOGGER.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            } else {
                for (PrometheusData data : dataList) {
                    try {
                        text.append(formatData(data)).append(LINE_FEED_CHARACTER);
                    } catch (Exception e) {
                        String errorMessage = "Fails to export prometheus data: `format failed`. Invalid data: `"
                                + data + "`. Collector: `" + collector + "`.";
                        LOGGER.error(errorMessage, e);
                        throw new IllegalStateException(errorMessage, e);
                    }
                }
            }
        }
        return text.toString();
    }

    /**
     * 将 Prometheus 监控指标转换为文本。
     *
     * @param data Prometheus 监控指标
     * @return Prometheus 监控指标对应的文本
     * @throws IllegalStateException 如果转换过程中出现错误，将会抛出此异常
     */
    private StringBuilder formatData(PrometheusData data) throws IllegalStateException {
        StringBuilder dataText = new StringBuilder(256);
        String metricName = data.getMetricName();
        if (!data.getHelpText().isEmpty()) {
            dataText.append("# HELP ").append(metricName).append(" ")
                    .append(escapeHelpText(data.getHelpText())).append(LINE_FEED_CHARACTER);
        }
        dataText.append("# TYPE ").append(metricName).append(" ")
                .append(data.getType()).append(LINE_FEED_CHARACTER);
        List<PrometheusSample> sampleList = data.getSampleList();
        if (sampleList.isEmpty()) {
            throw new IllegalStateException("Fails to format prometheus data: `empty sample list`. Invalid data: `" + data + "`.");
        }
        for (PrometheusSample sample : sampleList) {
            dataText.append(formatSample(data, sample)).append(LINE_FEED_CHARACTER);
        }
        return dataText;
    }

    /**
     * 将 Prometheus 监控指标中的单条样本数据转换为文本。
     *
     * @param data   Prometheus 监控指标
     * @param sample Prometheus 样本数据
     * @return Prometheus 样本数据对应的文本
     */
    private StringBuilder formatSample(PrometheusData data, PrometheusSample sample) {
        StringBuilder sampleText = new StringBuilder(128);
        //append sample name
        sampleText.append(getSampleName(data, sample));
        // append label
        int lengthBeforeAppendLabels = sampleText.length();
        sampleText.append("{");
        appendLabels(sampleText, data.getLabels());
        appendLabels(sampleText, sample.getSampleLabels());
        sampleText.deleteCharAt(sampleText.length() - 1);
        if (sampleText.length() > lengthBeforeAppendLabels) {
            sampleText.append("}");
        }
        // append value
        sampleText.append(" ").append(sample.getValue());
        // append timestamp
        if (sample.getTimestamp() != -1) {
            sampleText.append(" ").append(sample.getTimestamp());
        }
        return sampleText;
    }

    /**
     * 获得样本数据名称。
     *
     * @param data   Prometheus 监控指标
     * @param sample Prometheus 样本数据
     * @return 样本数据名称
     */
    private String getSampleName(PrometheusData data, PrometheusSample sample) {
        Map<String, String> sampleLabels = sample.getSampleLabels();
        String sampleName = data.getMetricName();
        if (data.getType() == PrometheusType.Histogram) {
            if (sampleLabels.containsKey(PrometheusSample.HISTOGRAM_LABEL_LE)) {
                sampleName += "_bucket";
            } else if (sampleLabels.containsKey(PrometheusSample.HISTOGRAM_LABEL_SUM)) {
                sampleName += "_sum";
                sampleLabels.remove(PrometheusSample.HISTOGRAM_LABEL_SUM);
            } else if (sampleLabels.containsKey(PrometheusSample.HISTOGRAM_LABEL_COUNT)) {
                sampleName += "_count";
                sampleLabels.remove(PrometheusSample.HISTOGRAM_LABEL_COUNT);
            }
        } else if (data.getType() == PrometheusType.Summary) {
            if (sampleLabels.containsKey(PrometheusSample.SUMMARY_LABEL_SUM)) {
                sampleName += "_sum";
                sampleLabels.remove(PrometheusSample.SUMMARY_LABEL_SUM);
            } else if (sampleLabels.containsKey(PrometheusSample.SUMMARY_LABEL_COUNT)) {
                sampleName += "_count";
                sampleLabels.remove(PrometheusSample.SUMMARY_LABEL_COUNT);
            }
        }
        return sampleName;
    }

    /**
     * 将标签以文本格式添加到目标 StringBuilder 中，值为 {@code null} 或空的标签不会被添加，如果存在至少一个可添加的标签，
     * 目标 StringBuilder 的最后一个字符为标签分隔符 ","。
     * <p><strong>注意：</strong>该方法不会添加开始字符 "{" 以及结束字符 "}"。</p>
     *
     * @param dest   目标 StringBuilder
     * @param labels 标签 Map，Key 为标签名称，Value 为标签值
     */
    private void appendLabels(StringBuilder dest, Map<String, String> labels) {
        for (String labelName : labels.keySet()) {
            String labelValue = labels.get(labelName);
            if (labelValue != null && !labelValue.isEmpty()) {
                dest.append(labelName).append("=\"").append(escapeLabelValue(labelValue)).append("\",");
            }
        }
    }

    /**
     * 对监控指标帮助文本中的特殊字符进行转义，返回转义后的文本。
     *
     * @param helpText 监控指标帮助文本
     * @return 转义后的监控指标帮助文本
     */
    private String escapeHelpText(String helpText) {
        return escape(helpText, HELP_TEXT_ESCAPE_MAP);
    }

    /**
     * 对标签值文本中的特殊字符进行转义，返回转义后的文本。
     *
     * @param labelValue 标签值文本
     * @return 转义后的标签值文本
     */
    private String escapeLabelValue(String labelValue) {
        return escape(labelValue, LABEL_VALUE_ESCAPE_MAP);
    }

    /**
     * 对原始文本中的特殊字符进行转义，返回转义后的文本。
     *
     * @param source    原始文本，不允许为 {@code null}
     * @param escapeMap 特殊字符转义 Map，Key 为需要转义的字符，Value 为转义后的字符串
     * @return 转义后的文本
     */
    private String escape(String source, Map<Integer, String> escapeMap) {
        StringBuilder buffer = null;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            String escapedString = escapeMap.get((int) c);
            if (escapedString != null) {
                if (buffer == null) {
                    buffer = new StringBuilder(source.length() + 8);
                    buffer.append(source, 0, i);
                }
                buffer.append(escapedString);
            } else {
                if (buffer != null) {
                    buffer.append(c);
                }
            }
        }
        return buffer == null ? source : buffer.toString();
    }
}
