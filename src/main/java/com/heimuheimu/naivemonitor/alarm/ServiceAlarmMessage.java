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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 服务不可用或从不可用状态恢复的报警消息。
 *
 * <p><strong>说明：</strong>{@code ServiceAlarmMessage} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ServiceAlarmMessage {

    /**
     * 服务名称
     */
    private final String name;

    /**
     * 调用该服务的主机名称
     */
    private final String host;

    /**
     * 调用该服务的项目名称
     */
    private final String project;

    /**
     * 报警消息创建时间
     */
    private final Date createdTime = new Date();

    /**
     * 服务发生不可用的总次数
     */
    private int crashedTimes = 0;

    /**
     * 服务从不可用状态恢复的总次数
     */
    private int recoveredTimes = 0;

    /**
     * 服务曾处于不可用状态下的提供方主机名称 Set，如果该服务为本地服务，Set 为空
     */
    private final Set<String> crashedRemoteHostSet = new HashSet<>();

    /**
     * 当前服务仍处于不可用状态的提供方主机名称 Map，Key 为提供方主机名称，Value 固定为 1
     */
    private final Map<String, Integer> crashingRemoteHostMap = new HashMap<>();

    /**
     * 构造一个 {@code ServiceAlarmMessage} 实例。
     *
     * @param name 服务名称
     * @param host 调用该服务的主机名称
     * @param project 调用该服务的项目名称
     */
    public ServiceAlarmMessage(String name, String host, String project) {
        this.name = name;
        this.host = host;
        this.project = project;
    }

    /**
     * 当该服务变成不可用时，调用此方法，进行报警信息拼装。
     *
     * @param remoteHost 服务提供方主机名称，如果为本地服务，则为 {@code null} 或空字符串
     */
    public void onCrashed(String remoteHost) {
        this.crashedTimes ++;
        if (remoteHost != null && !remoteHost.isEmpty()) {
            crashedRemoteHostSet.add(remoteHost);
            crashingRemoteHostMap.put(remoteHost, 1);
        }
    }

    /**
     * 当该服务从不可用状态恢复时，调用此方法，进行报警信息拼装。
     *
     * @param remoteHost 服务提供方主机名称，如果为本地服务，则为 {@code null} 或空字符串
     */
    public void onRecovered(String remoteHost) {
        this.recoveredTimes ++;
        if (remoteHost != null && !remoteHost.isEmpty()) {
            crashingRemoteHostMap.remove(remoteHost);
        }
    }

    /**
     * 获得服务名称。
     *
     * @return 服务名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获得调用该服务的主机名称。
     *
     * @return 调用该服务的主机名称
     */
    public String getHost() {
        return host;
    }

    /**
     * 获得调用该服务的项目名称。
     *
     * @return 调用该服务的项目名称
     */
    public String getProject() {
        return project;
    }

    /**
     * 获得报警消息创建时间。
     *
     * @return 报警消息创建时间
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * 获得服务发生不可用的总次数。
     *
     * @return 服务发生不可用的总次数
     */
    public int getCrashedTimes() {
        return crashedTimes;
    }

    /**
     * 获得服务从不可用状态恢复的总次数。
     *
     * @return 服务从不可用状态恢复的总次数
     */
    public int getRecoveredTimes() {
        return recoveredTimes;
    }

    /**
     * 获得服务曾处于不可用状态下的提供方主机名称 Set，如果该服务为本地服务，Set 为空。
     *
     * @return 服务曾处于不可用状态下的提供方主机名称 Set
     */
    public Set<String> getCrashedRemoteHostSet() {
        return crashedRemoteHostSet;
    }

    /**
     * 获得服务当前仍处于不可用状态下的提供方主机名称 Set，如果该服务为本地服务，Set 为空。
     *
     * @return 服务当前仍处于不可用状态下的提供方主机名称 Set
     */
    public Set<String> getCrashingRemoteHostSet() {
        if (crashingRemoteHostMap.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(crashingRemoteHostMap.keySet());
        }
    }

    /**
     * 将当前报警消息拼装成文本输出。
     *
     * @return 当前报警消息拼装成的文本信息
     */
    public String toText() {
        String title = getName() + " 不可服务";
        StringBuilder buffer = new StringBuilder(title).append("\n");
        buffer.append("服务名称： ").append(getName()).append("\n");
        buffer.append("项目名称： ").append(getProject()).append("\n");
        buffer.append("主机名称： ").append(getHost()).append("\n");
        buffer.append("不可用次数： ").append(getCrashedTimes()).append("\n");
        buffer.append("恢复次数： ").append(getRecoveredTimes()).append("\n");
        if (getCrashingRemoteHostSet().isEmpty()) {
            buffer.append("当前不可用远程主机列表： [ 无 ] \n");
        } else {
            buffer.append("当前不可用远程主机列表： \n");
            List<String> crashingRemoteHostList = new ArrayList<>(getCrashingRemoteHostSet());
            Collections.sort(crashingRemoteHostList);
            for (String remoteHost : crashingRemoteHostList) {
                buffer.append("  - ").append(remoteHost).append("\n");
            }
        }
        if (!getCrashedRemoteHostSet().isEmpty()) {
            buffer.append("历史不可用远程主机列表： \n");
            List<String> crashedRemoteHostList = new ArrayList<>(getCrashedRemoteHostSet());
            Collections.sort(crashedRemoteHostList);
            for (String remoteHost : crashedRemoteHostList) {
                buffer.append("  - ").append(remoteHost).append("\n");
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        buffer.append("报警时间： ").append(dateFormat.format(getCreatedTime())).append("\n");
        return buffer.toString();
    }

    @Override
    public String toString() {
        return "ServiceAlarmMessage{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", project='" + project + '\'' +
                ", crashedTimes=" + crashedTimes +
                ", recoveredTimes=" + recoveredTimes +
                ", crashedRemoteHostSet=" + crashedRemoteHostSet +
                ", crashingRemoteHostMap=" + crashingRemoteHostMap +
                '}';
    }
}
