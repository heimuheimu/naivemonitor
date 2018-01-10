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

import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.Objects;

/**
 * 服务及服务所在的运行环境信息。
 *
 * <p><strong>说明：</strong>{@code ServiceContext} 类是非线程安全的，不允许多个线程使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ServiceContext {

    /**
     * 服务名称
     */
    private String name = "";

    /**
     * 调用该服务的主机名称，默认当前 JVM 运行的机器名
     */
    private String host = MonitorUtil.getLocalHostName();

    /**
     * 调用该服务的项目名称
     */
    private String project = "";

    /**
     * 提供该服务的主机名称，如果为本地服务，值为空
     */
    private String remoteHost = "";

    /**
     * 获得服务名称。
     *
     * @return 服务名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置服务名称。
     *
     * @param name 服务名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获得调用该服务的主机名称，如果未进行过设置，将返回当前 JVM 运行的机器名。
     *
     * @return 调用该服务的主机名称
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置调用该服务的主机名称，如果不进行设置，默认为当前 JVM 运行的机器名。
     *
     * @param host 调用该服务的主机名称
     */
    public void setHost(String host) {
        this.host = host;
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
     * 设置调用该服务的项目名称。
     *
     * @param project 调用该服务的项目名称
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * 获得提供该服务的主机名称，如果为本地服务，值为空。
     *
     * @return 提供该服务的主机名称，如果为本地服务，值为空
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * 设置提供该服务的主机名称，如果为本地服务，不需要进行设置。
     *
     * @param remoteHost 提供该服务的主机名称，如果为本地服务，不需要进行设置
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceContext that = (ServiceContext) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(host, that.host) &&
                Objects.equals(project, that.project) &&
                Objects.equals(remoteHost, that.remoteHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, project, remoteHost);
    }

    @Override
    public String toString() {
        return "ServiceContext{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", project='" + project + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                '}';
    }
}
