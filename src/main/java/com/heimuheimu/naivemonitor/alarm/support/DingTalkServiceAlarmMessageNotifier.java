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
package com.heimuheimu.naivemonitor.alarm.support;

import com.heimuheimu.naivemonitor.alarm.ServiceAlarmMessage;
import com.heimuheimu.naivemonitor.alarm.ServiceAlarmMessageNotifier;
import com.heimuheimu.naivemonitor.http.NaiveHttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 使用钉钉实现的报警消息通知器。
 *
 * <p>
 * 钉钉机器人开发文档请查阅：
 * <a href="https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.a5dkCS&treeId=257&articleId=105735&docType=1">
 *     https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.a5dkCS&treeId=257&articleId=105735&docType=1
 * </a>
 * </p>
 *
 * <p><strong>说明：</strong>{@code NaiveServiceAlarm} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class DingTalkServiceAlarmMessageNotifier implements ServiceAlarmMessageNotifier {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DingTalkServiceAlarmMessageNotifier.class);

    /**
     * 钉钉消息推送 URL 地址
     */
    private final String url;

    /**
     * Http 代理地址，由主机名和端口号组成，用 ":" 进行分割，例如："192.168.1.1:9900"，允许为 {@code null} 或空字符串
     */
    private final String proxyHost;

    /**
     * 钉钉消息推送超时时间，单位：毫秒
     */
    private final int timeout;

    /**
     * 在报警消息发送时，服务仍存在不可用时使用的图片地址
     */
    private final String crashingImageUrl;

    /**
     * 在报警消息发送时，服务已恢复使用的图片地址
     */
    private final String recoveredImageUrl;

    /**
     * 构造一个使用钉钉实现的报警消息通知器，发送超时设置为 5 秒钟。
     * <p>
     *     如果 url 设置为 {@code null} 或空，{@link #send(ServiceAlarmMessage)} 只进行 WARN 级别日志打印，不进行实际发送，并永远返回 {@code false}。
     * </p>
     *
     * @param url url 钉钉消息发送 URL 地址
     */
    public DingTalkServiceAlarmMessageNotifier(String url) {
        this(url, null, 5000, null, null);
    }

    /**
     * 构造一个使用钉钉实现的报警消息通知器，可指定 Http 代理地址，发送超时设置为 5 秒钟。
     *
     * <p>
     *     如果本机无法访问公网，可通过 Http(Https) 代理的方式来实现钉钉报警，例如使用 Tinyproxy，更多资料请查阅：
     *     <a href="https://tinyproxy.github.io">https://tinyproxy.github.io</a>
     * </p>
     *
     * <p>
     *     如果 url 设置为 {@code null} 或空，{@link #send(ServiceAlarmMessage)} 只进行 WARN 级别日志打印，不进行实际发送，并永远返回 {@code false}。
     * </p>
     *
     * @param url 钉钉消息发送 URL 地址
     * @param proxyHost Http 代理地址，由主机名和端口号组成，用 ":" 进行分割，例如："192.168.1.1:9900"，允许为 {@code null} 或空字符串
     */
    public DingTalkServiceAlarmMessageNotifier(String url, String proxyHost) {
        this(url, proxyHost, 5000, null, null);
    }

    /**
     * 构造一个使用钉钉实现的报警消息通知器，可指定 Http 代理地址和发送超时时间。
     *
     * <p>
     *     如果本机无法访问公网，可通过 Http(Https) 代理的方式来实现钉钉报警，例如使用 Tinyproxy，更多资料请查阅：
     *     <a href="https://tinyproxy.github.io">https://tinyproxy.github.io</a>
     * </p>
     *
     * <p>
     *     如果 url 设置为 {@code null} 或空，{@link #send(ServiceAlarmMessage)} 只进行 WARN 级别日志打印，不进行实际发送，并永远返回 {@code false}。
     * </p>
     *
     * @param url 钉钉消息发送 URL 地址，允许为 {@code null} 或空
     * @param proxyHost Http 代理地址，由主机名和端口号组成，用 ":" 进行分割，例如："192.168.1.1:9900"，允许为 {@code null} 或空字符串
     * @param timeout 钉钉消息发送超时时间，单位：毫秒，不允许小于等于 0
     * @param crashingImageUrl 在报警消息发送时，服务仍存在不可用时使用的图片地址，如果不需要，则设置 {@code null} 或空字符串
     * @param recoveredImageUrl 在报警消息发送时，服务已恢复使用的图片地址，如果不需要，则设置 {@code null} 或空字符串
     */
    public DingTalkServiceAlarmMessageNotifier(String url, String proxyHost, int timeout, String crashingImageUrl, String recoveredImageUrl) {
        this.url = url;
        this.proxyHost = proxyHost;
        this.timeout = timeout;
        this.crashingImageUrl = crashingImageUrl != null ? crashingImageUrl : "";
        this.recoveredImageUrl = recoveredImageUrl != null ? recoveredImageUrl : "";
    }

    @Override
    public boolean send(ServiceAlarmMessage serviceAlarmMessage) {
        if (url != null && !url.isEmpty()) {
            long startTime = System.currentTimeMillis();
            try {
                NaiveHttpPost post = new NaiveHttpPost(url, timeout, proxyHost);
                post.getUrlConnection().setRequestProperty("Content-Type", "application/json; charset=utf-8");
                String responseText = post.doPost(getPostBody(serviceAlarmMessage));
                boolean isSuccess = isSuccess(responseText);
                if (isSuccess) {
                    LOGGER.info("Send ServiceAlarmMessage to DingTalk success. Cost: `{} ms`. ServiceAlarmMessage: `{}`. Url: `{}`. Proxy host: `{}`. Timeout: `{}`. CrashingImageUrl: `{}`. RecoveredImageUrl: `{}`.",
                            System.currentTimeMillis() - startTime, serviceAlarmMessage, url, proxyHost, timeout, crashingImageUrl, recoveredImageUrl);
                } else {
                    LOGGER.error("Send ServiceAlarmMessage to DingTalk failed. Cost: `{} ms`. ResponseText: `{}`. ServiceAlarmMessage: `{}`. Url: `{}`. Proxy host: `{}`. Timeout: `{}`. CrashingImageUrl: `{}`. RecoveredImageUrl: `{}`.",
                            System.currentTimeMillis() - startTime, responseText, serviceAlarmMessage, url, proxyHost, timeout, crashingImageUrl, recoveredImageUrl);
                }
                return isSuccess;
            } catch (Exception e) {
                LOGGER.error("Send ServiceAlarmMessage to DingTalk failed. Cost: `" + (System.currentTimeMillis() - startTime)
                        + " ms`. ServiceAlarmMessage: `" + serviceAlarmMessage + "`. Url: `" + url + "`. Proxy host: `" + proxyHost + "`. Timeout: `" + timeout
                        + "`. CrashingImageUrl: `" + crashingImageUrl + "`. RecoveredImageUrl: `" + recoveredImageUrl + "`.", e);
                return false;
            }
        } else {
            LOGGER.warn("DingTalkServiceAlarmMessageNotifier is inactive. ServiceAlarmMessage: `" + serviceAlarmMessage + "`.");
            return false;
        }
    }

    private String getPostBody(ServiceAlarmMessage serviceAlarmMessage) {
        String title = serviceAlarmMessage.getName() + " 不可服务";
        StringBuilder buffer = new StringBuilder();
        buffer.append("{\"msgtype\":\"markdown\",\"markdown\":{\"title\":\"");
        buffer.append(title).append("\",");
        buffer.append("\"text\":\"");
        buffer.append("# **").append(title).append("** \n");
        buffer.append("#### 服务名称： **").append(serviceAlarmMessage.getName()).append("** \n");
        buffer.append("#### 项目名称： **").append(serviceAlarmMessage.getProject()).append("** \n");
        buffer.append("#### 主机名称： **").append(serviceAlarmMessage.getHost()).append("** \n");
        buffer.append("#### 不可用次数： ").append(serviceAlarmMessage.getCrashedTimes()).append("\n");
        buffer.append("#### 恢复次数： ").append(serviceAlarmMessage.getRecoveredTimes()).append("\n");
        if (serviceAlarmMessage.getCrashingRemoteHostSet().isEmpty()) {
            buffer.append("#### 当前不可用远程主机列表： [ 无 ] \n");
        } else {
            buffer.append("#### 当前不可用远程主机列表： \n");
            List<String> crashingRemoteHostList = new ArrayList<>(serviceAlarmMessage.getCrashingRemoteHostSet());
            Collections.sort(crashingRemoteHostList);
            for (String remoteHost : crashingRemoteHostList) {
                buffer.append("- **").append(remoteHost).append("** \n");
            }
        }
        if (!serviceAlarmMessage.getCrashedRemoteHostSet().isEmpty()) {
            buffer.append("#### 历史不可用远程主机列表： \n");
            List<String> crashedRemoteHostList = new ArrayList<>(serviceAlarmMessage.getCrashedRemoteHostSet());
            Collections.sort(crashedRemoteHostList);
            for (String remoteHost : crashedRemoteHostList) {
                buffer.append("- **").append(remoteHost).append("** \n");
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        buffer.append("#### 报警时间： **").append(dateFormat.format(serviceAlarmMessage.getCreatedTime())).append("** \n");

        if (!crashingImageUrl.isEmpty()) {
            if (!serviceAlarmMessage.getCrashingRemoteHostSet().isEmpty() ||
                    serviceAlarmMessage.getCrashedTimes() > serviceAlarmMessage.getRecoveredTimes()) {
                buffer.append("![screenshot](").append(crashingImageUrl).append(")\n");
            }
        }

        if (!recoveredImageUrl.isEmpty()) {
            if (serviceAlarmMessage.getCrashingRemoteHostSet().isEmpty() &&
                    serviceAlarmMessage.getCrashedTimes() <= serviceAlarmMessage.getRecoveredTimes()) {
                buffer.append("![screenshot](").append(recoveredImageUrl).append(")\n");
            }
        }

        buffer.append("\"}, \"at\": {\"atMobiles\":[],\"isAtAll\":").append(!serviceAlarmMessage.getCrashingRemoteHostSet().isEmpty())
                .append("}}");
        return buffer.toString();
    }

    private boolean isSuccess(String responseText) {
        if (responseText != null) {
            responseText = responseText.replace(" ","").replace("\t", "")
                    .replace("\n", "").replace("\r", "");
            return responseText.contains("\"errcode\":0");
        }
        return false;
    }

}
