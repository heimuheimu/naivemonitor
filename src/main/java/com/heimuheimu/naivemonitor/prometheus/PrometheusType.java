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

/**
 * Prometheus 监控指标类型枚举类，更多信息请参考文档：<a href="https://prometheus.io/docs/concepts/metric_types/">METRIC TYPES</a>
 *
 * @author heimuheimu
 * @since 1.1
 */
public enum PrometheusType {

    /**
     * Counter 类型，适用于监控指标数值仅允许递增或重置为 0 后重新递增的场景，请不要在监控指标样本数据可能下降的场景中使用此类型。
     * 更多信息请参考以下文档：<a href="https://prometheus.io/docs/concepts/metric_types/#counter">Counter</a>
     */
    Counter("counter"),

    /**
     * Gauge 类型，适用于监控指标数值增加或减少都存在的场景。
     * 更多信息请参考以下文档：<a href="https://prometheus.io/docs/concepts/metric_types/#gauge">Counter</a>
     */
    Gauge("gauge"),

    /**
     * Histogram 类型，可用于记录样本数据在不同区间出现的次数，以及样本数据数值总和。
     * 更多信息请参考以下文档：<a href="https://prometheus.io/docs/concepts/metric_types/#histogram">Histogram</a>，
     * <a href="https://prometheus.io/docs/practices/histograms/#histograms-and-summaries">HISTOGRAMS AND SUMMARIES</a>
     *
     * <p><strong>注意：</strong>Histogram 区间样本数据必须包含 {@link PrometheusSample#HISTOGRAM_LABEL_LE} 标签，
     * SUM 样本数据必须包含 {@link PrometheusSample#HISTOGRAM_LABEL_SUM} 标签，COUNT 样本数据必须包含
     * {@link PrometheusSample#HISTOGRAM_LABEL_COUNT} 标签。</p>
     */
    Histogram("histogram"),

    /**
     * Summary 类型，可用于记录样本数据在不同百分比下的最小样本数据，以及样本数据数值总和。
     * 更多信息请参考以下文档：<a href="https://prometheus.io/docs/concepts/metric_types/#summary">Summary</a>，
     * <a href="https://prometheus.io/docs/practices/histograms/#histograms-and-summaries">HISTOGRAMS AND SUMMARIES</a>
     *
     * <p><strong>注意：</strong>Summary 的百分比最小样本数据必须包含 {@link PrometheusSample#SUMMARY_LABEL_QUANTILE} 标签，
     * SUM 样本数据必须包含 {@link PrometheusSample#SUMMARY_LABEL_SUM} 标签，COUNT 样本数据必须包含
     * {@link PrometheusSample#SUMMARY_LABEL_COUNT} 标签。</p>
     */
    Summary("summary");

    /**
     * Prometheus 监控指标类型对应的文本，在文本格式导出时使用。
     */
    private final String type;

    /**
     * 构造一个 PrometheusType 枚举类型。
     *
     * @param type Prometheus 监控指标类型对应的文本
     */
    PrometheusType(String type) {
        this.type = type;
    }

    /**
     * 获得 Prometheus 监控指标类型对应的文本，在文本格式导出时使用。
     *
     * @return Prometheus 监控指标类型对应的文本
     */
    public String getType() {
        return type;
    }
}
