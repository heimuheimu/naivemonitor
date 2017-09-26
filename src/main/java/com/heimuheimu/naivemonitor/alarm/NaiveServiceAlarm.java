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

package com.heimuheimu.naivemonitor.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 当服务不可用或从不可用状态恢复时，可通过此报警器进行实时通知
 * <p>注意：当前实现是线程安全的</p>
 *
 * @author heimuheimu
 */
public class NaiveServiceAlarm {

    private final static Logger LOGGER = LoggerFactory.getLogger(NaiveServiceAlarm.class);

    /**
     * 报警消息通知器列表
     */
    private final List<ServiceAlarmMessageNotifier> notifierList;

    /**
     * 报警消息合并最大条数
     */
    private final int maxMessageBatchSize;

    /**
     * 当两条报警消息创建时间小于当前间隔，将进行合并，单位：秒
     */
    private final int mergeMessageIntervalSeconds;

    /**
     * 报警消息通知线程使用的私有锁
     */
    private final Object notificationThreadLock = new Object();

    /**
     * 需要发送的报警消息队列
     */
    private final LinkedBlockingQueue<ServiceContextMessage> messageQueue = new LinkedBlockingQueue<>();

    /**
     * 报警消息通知线程是否正在运行中
     */
    private boolean isNotificationThreadRunning = false;

    /**
     * 构造一个服务报警器，该报警器将会对两条创建时间间隔小于 3 秒的报警消息进行合并，合并最大条数为 50 条
     *
     * @param notifierList 报警消息通知器列表，不允许为 {@code null} 或空
     * @throws IllegalArgumentException 如果消息通知器列表为 {@code null} 或空时，抛出此异常
     */
    public NaiveServiceAlarm(List<ServiceAlarmMessageNotifier> notifierList) throws IllegalArgumentException {
        this(notifierList, 50, 3);
    }

    /**
     * 构造一个服务报警器
     *
     * @param notifierList 报警消息通知器列表，不允许为 {@code null} 或空
     * @param maxMessageBatchSize 报警消息合并最大条数，不允许小于等于 0
     * @param mergeMessageIntervalSeconds 当两条报警消息创建时间小于当前间隔，将进行合并，单位：秒，不允许小于等于 0
     */
    public NaiveServiceAlarm(List<ServiceAlarmMessageNotifier> notifierList, int maxMessageBatchSize, int mergeMessageIntervalSeconds) {
        if (notifierList == null || notifierList.isEmpty()) {
            LOGGER.error("Created NaiveServiceAlarm failed: `notifier list could not be null or empty`.");
            throw new IllegalArgumentException("Created NaiveServiceAlarm failed: `notifier list could not be null or empty`.");
        }
        this.maxMessageBatchSize = maxMessageBatchSize;
        this.mergeMessageIntervalSeconds = mergeMessageIntervalSeconds;
        this.notifierList = notifierList;
    }

    /**
     * 对不可用的服务进行报警通知
     *
     * @param serviceContext 当前不可用的服务信息
     */
    public void onCrashed(ServiceContext serviceContext) {
        synchronized (notificationThreadLock) {
            messageQueue.add(new ServiceContextMessage(serviceContext, ServiceContextMessage.STATE_CRASHED));
            startNotificationThread();
        }
    }

    /**
     * 对服务从不可用状态恢复进行通知
     *
     * @param serviceContext 从不可用状态恢复的服务
     */
    public void onRecovered(ServiceContext serviceContext) {
        synchronized (notificationThreadLock) {
            messageQueue.add(new ServiceContextMessage(serviceContext, ServiceContextMessage.STATE_RECOVERED));
            startNotificationThread();
        }
    }

    /**
     * 启动报警消息通知线程，对当前队列中的报警消息进行通知
     * <br>注意：该方法必须在获取 {@link #notificationThreadLock} 锁后进行执行
     */
    private void startNotificationThread() {
        if (!isNotificationThreadRunning) {
            isNotificationThreadRunning = true;
            Thread notificationThread = new NotificationThread();
            notificationThread.setName("naivemonitor-alarm-message-notification");
            notificationThread.setDaemon(true);
            notificationThread.start();
        }
    }

    private void send(ServiceAlarmMessage serviceAlarmMessage) {
        boolean isSentSuccess = false;
        ServiceAlarmMessageNotifier sentNotifier = null;
        for (ServiceAlarmMessageNotifier notifier : notifierList) {
            try {
                isSentSuccess = notifier.send(serviceAlarmMessage);
                if (isSentSuccess) {
                    sentNotifier = notifier;
                    break;
                }
            } catch (Exception e) {
                LOGGER.error("Send ServiceAlarmMessage failed: `encounter unexpected error`. ServiceAlarmMessage: `" +
                        serviceAlarmMessage + "`. Notifier: `" + notifier + "`.", e);
            }
        }
        if (isSentSuccess) {
            LOGGER.info("Send ServiceAlarmMessage success. ServiceAlarmMessage: `" + serviceAlarmMessage + "`. Notifier: `" + sentNotifier + "`.");
        } else {
            LOGGER.error("Send ServiceAlarmMessage completely failed. ServiceAlarmMessage:`" + serviceAlarmMessage + "`. Notifiers: " + notifierList + "`.");
        }
    }

    private static class ServiceContextMessage {

        /**
         * 服务状态：不可用
         */
        private final static int STATE_CRASHED = -1;

        /**
         * 服务状态：服务从不可用状态恢复
         */
        private final static int STATE_RECOVERED = 0;

        /**
         * 服务及服务所在的运行环境信息
         */
        private final ServiceContext serviceContext;

        /**
         * 服务状态：
         * <ul>
         *     <li>{@link #STATE_CRASHED}: 服务不可用</li>
         *     <li>{@link #STATE_RECOVERED}: 服务从不可用状态恢复</li>
         * </ul>
         */
        private final int state;

        public ServiceContextMessage(ServiceContext serviceContext, int state) {
            this.serviceContext = serviceContext;
            this.state = state;
        }
    }

    private class NotificationThread extends Thread {

        @Override
        public void run() {
            LOGGER.debug("NotificationThread has benn started.");
            boolean stopFlag = false;
            while (!stopFlag) {
                try {
                    Map<String, ServiceAlarmMessage> alarmMessageMap = new HashMap<>();

                    int mergedSize = 0;
                    ServiceContextMessage message = messageQueue.poll();
                    if (message != null) {
                        addToAlarmMessageMap(alarmMessageMap, message);
                        mergedSize++;

                        while (mergedSize < maxMessageBatchSize &&
                                (message = messageQueue.poll(mergeMessageIntervalSeconds, TimeUnit.SECONDS)) != null) {
                            addToAlarmMessageMap(alarmMessageMap, message);
                            mergedSize++;
                        }
                    }

                    if (!alarmMessageMap.isEmpty()) {
                        for (ServiceAlarmMessage serviceAlarmMessage : alarmMessageMap.values()) {
                            send(serviceAlarmMessage);
                        }
                    } else {
                        synchronized (notificationThreadLock) {
                            if (messageQueue.isEmpty()) {
                                stopFlag = true;
                                isNotificationThreadRunning = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    //should not happen
                    LOGGER.error("NotificationThread run failed: `encounter unexpected error`. ServiceAlarmMessage maybe lost.", e);
                    stopFlag = true;
                    synchronized (notificationThreadLock) {
                        isNotificationThreadRunning = false;
                    }
                }
            }
            LOGGER.debug("NotificationThread has benn stopped.");
        }

        private void addToAlarmMessageMap(Map<String, ServiceAlarmMessage> alarmMessageMap, ServiceContextMessage message) {
            ServiceContext serviceContext = message.serviceContext;
            String serviceKey = getServiceKey(serviceContext);
            ServiceAlarmMessage alarmMessage = alarmMessageMap.get(serviceKey);
            if (alarmMessage == null) {
                alarmMessage = new ServiceAlarmMessage(serviceContext.getName(), serviceContext.getHost(), serviceContext.getProject());
                alarmMessageMap.put(serviceKey, alarmMessage);
            }
            if (message.state == ServiceContextMessage.STATE_CRASHED) {
                alarmMessage.onCrashed(serviceContext.getRemoteHost());
                LOGGER.debug("Add crashed `{}` to `{}`.", serviceContext.getRemoteHost(), alarmMessage);
            } else {
                alarmMessage.onRecovered(serviceContext.getRemoteHost());
                LOGGER.debug("Add recovered `{}` to `{}`.", serviceContext.getRemoteHost(), alarmMessage);
            }
        }

        private String getServiceKey(ServiceContext context) {
            return context.getHost() + "_" + context.getProject() + "_" + context.getName();
        }
    }
}
