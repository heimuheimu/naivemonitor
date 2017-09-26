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

/**
 * 服务不可用或从不可用状态恢复的报警消息通知器。由于报警信息实时性要求较高，建议采用短信、钉钉、微信等方式进行发送。
 * <p>注意：实现类必须是线程安全的</p>
 *
 * @author heimuheimu
 */
public interface ServiceAlarmMessageNotifier {

    /**
     * 发送一条服务不可用或从不可用状态恢复的报警消息，如果成功，返回 {@code true}，否则返回 {@code false}
     *
     * @param serviceAlarmMessage 服务不可用或从不可用状态恢复的报警消息
     * @return 是否发送成功
     */
    boolean send(ServiceAlarmMessage serviceAlarmMessage);

}
