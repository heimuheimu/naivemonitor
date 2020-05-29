# NaiveMonitor: 快速实现数据监控和实时报警功能。

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/heimuheimu/naivemonitor.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/heimuheimu/naivemonitor/context:java)

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>naivemonitor</artifactId>
        <version>1.1-SNAPSHOT</version>
    </dependency>
```

## 执行信息监控 DEMO
  场景：对用户注册操作进行数据监控（注意：示例代码仅为说明如何使用 [ExecutionMonitor](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/monitor/ExecutionMonitor.java)。）
```java
@Controller
public class UserRegisterController { //用户注册使用的 Controller，数据监控可根据自身需求放在任意层级，例如 Service 层、Dao 层。

    public final int ERROR_CODE_DUPLICATE_USERNAME = -1; // 用户注册错误代码：用户名已存在

    public final int ERROR_CODE_UNEXPECTED_ERROR = -2; // 用户注册错误代码：预期外异常

    public final ExecutionMonitor USER_REGISTER_MONITOR = NaiveExecutionMonitorFactory.get("UserRegister"); //获取用户注册操作执行信息监控器
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/register")
    @ResponseBody
    public boolean register(@RequestParam("username") String username, @RequestParam("password") String password) {
        long startNanoTime = System.nanoTime();
        try {
            if (userService.isExist(username)) { //如果用户名已存在
                USER_REGISTER_MONITOR.onError(ERROR_CODE_DUPLICATE_USERNAME); //对用户名已存在的错误进行监控
                return false;
            }
            userService.add(username, password); //创建用户
            return true;
        } catch (Execution e) {
            USER_REGISTER_MONITOR.onError(ERROR_CODE_UNEXPECTED_ERROR); //对执行过程中出现预期外异常进行监控
            return false;
        } finally{
            USER_REGISTER_MONITOR.onExecuted(startNanoTime); //对每一个注册操作进行监控
        }
    }
}
```

## [推荐使用] Prometheus 监控系统数据采集 DEMO
#### 1. 实现用户注册操作 Prometheus 监控数据采集器：
```java
public class UserRegisterPrometheusCollector extends AbstractExecutionPrometheusCollector {
    
    @Override
    protected String getMetricPrefix() { // 获得监控指标前缀
        return "bookstore_user_register"; 
    }

    @Override
    protected Map<Integer, String> getErrorTypeMap() { // 获得执行错误代码映射表
        Map<Integer, String> errorTypeMap = new HashMap<>();
        errorTypeMap.put(UserRegisterController.ERROR_CODE_DUPLICATE_USERNAME, "DuplicateUsername");
        errorTypeMap.put(UserRegisterController.ERROR_CODE_UNEXPECTED_ERROR, "UnexpectedError");
        return errorTypeMap;
    }

    @Override
    protected List<ExecutionMonitor> getMonitorList() { // 获得需要采集的操作执行信息监控器列表
        return Collections.singletonList(UserRegisterController.USER_REGISTER_MONITOR);
    }
    
    @Override
    protected String getMonitorId(ExecutionMonitor executionMonitor, int index) { // 获得 ExecutionMonitor 对应的 ID，每个 ExecutionMonitor 对应的 ID 应保证唯一
        return String.valueOf(index);
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) { // 当添加完成一个样本数据后，将会调用此方法进行回调，通常用于给样本数据添加 Label
        // no-op
    }
}
```
#### 2. 实现 Prometheus 监控指标导出 Controller（注意：请勿将此 Controller 暴露给公网访问，需通过策略仅允许 Prometheus 服务器或者内网访问）
```java
@Controller
@RequestMapping("/internal/")
public class PrometheusMetricsController {
    
    private final PrometheusExporter exporter;
    
    @Autowired
    public PrometheusMetricsController(PrometheusExporter exporter) {
        this.exporter = exporter;
    }
    
    @RequestMapping("/metrics")
    public void metrics(HttpServletResponse response) throws IOException {
        PrometheusHttpWriter.write(exporter.export(), response);
    }
}
```
#### 3. 在 Spring 中配置 PrometheusExporter 实例
```xml
    <bean name="prometheusExporter" class="com.heimuheimu.naivemonitor.prometheus.PrometheusExporter">
        <constructor-arg index="0" >
            <list>
                <!-- JVM 监控信息采集器-->
                <bean class="com.heimuheimu.naivemonitor.prometheus.support.hotspot.HotspotCompositePrometheusCollector" />
                <!-- 用户注册执行监控信息采集器 -->
                <bean class="com.heimuheimu.bookstore.monitor.prometheus.UserRegisterPrometheusCollector" />
            </list>
        </constructor-arg>
    </bean>
```
#### 4. 在 Prometheus 服务中配置对应的 Job
  完成以上工作后，在 Prometheus 系统中即可找到以下监控指标：
* 用户注册执行信息指标：
  * bookstore_user_register_exec_count    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内用户注册执行次数
  * bookstore_user_register_exec_peak_tps_count    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内每秒最大用户注册执行次数
  * bookstore_user_register_avg_exec_time_millisecond    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内用户注册平均执行时间，单位：毫秒
  * bookstore_user_register_max_exec_time_millisecond    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单次用户注册最大执行时间，单位：毫秒
  * bookstore_user_register_exec_error_count{errorCode="-1",errorType="DuplicateUsername"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内用户注册操作出现用户名重复的错误次数
  * bookstore_user_register_exec_error_count{errorCode="-2",errorType="UnexpectedError"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内用户注册操作出现预期外异常的错误次数
* JVM 类加载信息指标：
  * hotspot_loaded_class_count    &nbsp;&nbsp;&nbsp;&nbsp; 当前已加载的类数量
  * hotspot_total_loaded_class_count    &nbsp;&nbsp;&nbsp;&nbsp; 累计加载过的类数量
  * hotspot_total_unloaded_class_count    &nbsp;&nbsp;&nbsp;&nbsp; 累计卸载过的类数量
* JVM GC（垃圾回收）信息指标：
  * hotspot_gc_count{name="$collectorName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内执行的 GC 操作次数
  * hotspot_gc_time_milliseconds{name="$collectorName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内执行 GC 操作消耗的总时间，单位：毫秒
  * hotspot_gc_max_duration_millisecond{name="$collectorName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内单次 GC 执行最大时间，单位：毫秒
* JVM 内存使用信息指标：
  * hotspot_heap_memory_init_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域初始化内存大小，单位：字节
  * hotspot_heap_memory_used_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域正在使用的内存大小，单位：字节
  * hotspot_heap_memory_committed_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域保证可使用的内存大小，单位：字节
  * hotspot_heap_memory_max_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域最大可使用的内存大小，单位：字节
  * hotspot_nonheap_memory_init_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域初始化内存大小，单位：字节
  * hotspot_nonheap_memory_used_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域正在使用的内存大小，单位：字节
  * hotspot_nonheap_memory_committed_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域保证可使用的内存大小，单位：字节
  * hotspot_nonheap_memory_max_bytes    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域最大可使用的内存大小，单位：字节
  * hotspot_memory_pool_init_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域初始化内存大小，单位：字节
  * hotspot_memory_pool_used_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域正在使用的内存大小，单位：字节
  * hotspot_memory_pool_committed_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域保证可使用的内存大小，单位：字节
  * hotspot_memory_pool_max_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域最大可使用的内存大小，单位：字节
  * hotspot_memory_pool_peak_init_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内该内存池区域达到使用峰值时的初始化内存大小，单位：字节
  * hotspot_memory_pool_peak_used_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内该内存池区域达到使用峰值时的使用的内存大小，单位：字节
  * hotspot_memory_pool_peak_committed_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内该内存池区域达到使用峰值时的保证可使用的内存大小，单位：字节
  * hotspot_memory_pool_peak_max_bytes{name="$poolName"}    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内该内存池区域达到使用峰值时的最大可使用的内存大小，单位：字节
* JVM 线程信息采集器
  * hotspot_thread_count    &nbsp;&nbsp;&nbsp;&nbsp; 当前存活线程总数，包括 daemon 和 non-daemon 线程
  * hotspot_daemon_thread_count    &nbsp;&nbsp;&nbsp;&nbsp; 当前存活的 daemon 线程总数  
  * hotspot_total_started_thread_count    &nbsp;&nbsp;&nbsp;&nbsp; 累计启动过的线程总数
  * hotspot_peak_thread_count    &nbsp;&nbsp;&nbsp;&nbsp; 相邻两次采集周期内峰值存活线程总数
  
  通过 util-grafana 项目可以快速生成 Grafana 监控图表，项目地址：[https://github.com/heimuheimu/util-grafana](https://github.com/heimuheimu/util-grafana)
  
  更多 Prometheus 监控数据采集器的写法可参考 naiveredis 等项目，[点击查看源码](https://github.com/heimuheimu/naiveredis/tree/master/src/main/java/com/heimuheimu/naiveredis/monitor/prometheus)
  
## Falcon 监控系统数据采集 DEMO
#### 1. 实现用户注册操作 Falcon 监控数据采集器：
```java
public class UserRegisterFalconDataCollector extends AbstractExecutionDataCollector {
    
    @Override
    protected Map<Integer, String> getErrorMetricSuffixMap() {
        Map<Integer, String> errorMetricSuffixMap = new HashMap<>();
        errorMetricSuffixMap.put(-1, "_duplicate_username"); //对用户名已存在的错误次数进行统计
        errorMetricSuffixMap.put(-2, "_exec_error"); //对执行过程中出现预期外的异常次数进行统计
        return errorMetricSuffixMap;
    }

    @Override
    protected String getCollectorName() {
        return "register"; //采集器名称，表明为用户注册操作
    }

    @Override
    protected List<ExecutionMonitor> getExecutionMonitorList() {
        return Collections.singletonList(UserRegisterController.);
    }

    @Override
    protected String getModuleName() {
        return "bookstore"; //用户注册操作所在的项目名称
    } 

    @Override
    public int getPeriod() {
        return 30; //监控数据推送周期为 30 秒
    }
} 
```
#### 2. 在 Spring 中配置 Falcon 数据推送：
```xml
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址-->
        <constructor-arg index="1">
            <list>
                <!-- JVM 监控信息采集器-->
                <bean class="com.heimuheimu.naivemonitor.prometheus.support.hotspot.HotspotCompositePrometheusCollector" />
                <!-- 用户注册执行监控信息采集器 -->
                <bean class="com.heimuheimu.bookstore.monitor.falcon.UserRegisterFalconDataCollector" />
            </lit>
        </constructor-arg>
    </bean>
```
  完成以上工作后，在 Falcon 系统中可以找到以下数据项：
* 用户注册执行信息指标：
  * bookstore_register_tps/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均用户注册次数
  * bookstore_register_peak_tps/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大用户注册次数
  * bookstore_register_avg_exec_time/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次注册操作平均执行时间
  * bookstore_register_max_exec_time/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次注册操作最大执行时间
  * bookstore_register_duplicate_username/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内出现用户名已存在的错误次数
  * bookstore_register_exec_error/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内出现预期外的异常次数
* JVM 类加载信息指标：
  * hotspot_loaded_class_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前已加载的类数量
  * hotspot_total_loaded_class_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 累计加载过的类数量
  * hotspot_total_unloaded_class_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 累计卸载过的类数量
* JVM GC（垃圾回收）信息指标：
  * hotspot_gc_count/module=hotspot,name={collectorName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行的 GC 操作次数
  * hotspot_gc_time_milliseconds/module=hotspot,name={collectorName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内执行 GC 操作消耗的总时间，单位：毫秒
  * hotspot_gc_max_duration_millisecond/module=hotspot,name={collectorName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次 GC 执行最大时间，单位：毫秒
* JVM 内存使用信息指标：
  * hotspot_heap_memory_init_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域初始化内存大小，单位：字节
  * hotspot_heap_memory_used_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域正在使用的内存大小，单位：字节
  * hotspot_heap_memory_committed_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域保证可使用的内存大小，单位：字节
  * hotspot_heap_memory_max_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 heap 内存区域最大可使用的内存大小，单位：字节
  * hotspot_nonheap_memory_init_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域初始化内存大小，单位：字节
  * hotspot_nonheap_memory_used_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域正在使用的内存大小，单位：字节
  * hotspot_nonheap_memory_committed_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域保证可使用的内存大小，单位：字节
  * hotspot_nonheap_memory_max_bytes/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前 non-heap 内存区域最大可使用的内存大小，单位：字节
  * hotspot_memory_pool_init_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域初始化内存大小，单位：字节
  * hotspot_memory_pool_used_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域正在使用的内存大小，单位：字节
  * hotspot_memory_pool_committed_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域保证可使用的内存大小，单位：字节
  * hotspot_memory_pool_max_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 该内存池区域最大可使用的内存大小，单位：字节
  * hotspot_memory_pool_peak_init_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内该内存池区域达到使用峰值时的初始化内存大小，单位：字节
  * hotspot_memory_pool_peak_used_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内该内存池区域达到使用峰值时的使用的内存大小，单位：字节
  * hotspot_memory_pool_peak_committed_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内该内存池区域达到使用峰值时的保证可使用的内存大小，单位：字节
  * hotspot_memory_pool_peak_max_bytes/module=hotspot,name={poolName}    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内该内存池区域达到使用峰值时的最大可使用的内存大小，单位：字节
* JVM 线程信息采集器
  * hotspot_thread_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前存活线程总数，包括 daemon 和 non-daemon 线程
  * hotspot_daemon_thread_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 当前存活的 daemon 线程总数  
  * hotspot_total_started_thread_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 累计启动过的线程总数
  * hotspot_peak_thread_count/module=hotspot    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内峰值存活线程总数
  
  更多 Falcon 监控数据采集器的写法可参考 naiveredis 等项目，[点击查看源码](https://github.com/heimuheimu/naiveredis/tree/master/src/main/java/com/heimuheimu/naiveredis/monitor/falcon)

## 实时报警 DEMO
  在 Spring 中配置 [NaiveServiceAlarm](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/alarm/NaiveServiceAlarm.java)：

```xml
    <!-- 报警消息通知器列表配置 -->
    <util:list id="notifierList">
        <bean class="com.heimuheimu.naivemonitor.alarm.support.DingTalkServiceAlarmMessageNotifier">
            <constructor-arg index="0" value="https://oapi.dingtalk.com/robot/send?access_token={your_access_token}" /> <!-- 钉钉消息推送 URL 地址 -->
            <constructor-arg index="1" value="" /> <!-- Http proxy 地址，允许为空 -->
            <constructor-arg index="2" value="5000" /> <!-- 发送超时时间，毫秒 -->
            <constructor-arg index="3" value="" /> <!-- 服务不可用时显示的图片地址，允许为空 -->
            <constructor-arg index="4" value="" /> <!-- 服务恢复时显示的图片地址，允许为空 -->
        </bean>
    </util:list>
    
    <bean id="naiveServiceAlarm" class="com.heimuheimu.naivemonitor.alarm.NaiveServiceAlarm">
        <constructor-arg index="0" ref="notifierList"/>
    </bean>
```  

  实时报警示例代码：

```java
public class NaiveServiceAlarmTester {
    
    @Autowired
    private NaiveServiceAlarm naiveServiceAlarm;
    
    //服务不可用报警信息发送 Demo
    public void testSendCrashMessage() { 
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setHost(MonitorUtil.getLocalHostName()); //设置本机地址信息
        serviceContext.setProject("bookstore"); //设置当前项目名称
        serviceContext.setName("Memcached"); //设置服务名称
        serviceContext.setRemoteHost("127.0.0.1:11211"); //提供服务的远程主机地址信息，如果为本地服务，不需要进行此设置
        
        naiveServiceAlarm.onCrashed(serviceContext); //进行服务不可用报警通知
    }
    
    //服务恢复信息发送 Demo
    public void testSendRecoverMessage() { 
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setHost(MonitorUtil.getLocalHostName()); //设置本机地址信息
        serviceContext.setProject("bookstore"); //设置当前项目名称
        serviceContext.setName("Memcached"); //设置服务名称
        serviceContext.setRemoteHost("127.0.0.1:11211"); //提供服务的远程主机地址信息，如果为本地服务，不需要进行此设置
        
        naiveServiceAlarm.onRecovered(serviceContext); //进行服务不可用报警通知
    
    }
}
```

## 定制报警通知器
[NaiveServiceAlarm](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/alarm/NaiveServiceAlarm.java) 允许设置多个报警通知器，在一个报警通知器失败时，会继续选择其它可用的报警通知器进行通知。
由于钉钉机器人的发送频率有限制，为保证报警消息到达，建议增加一个短信报警通知器（不建议使用邮件等非实时通知器）。

使用 NaiveMonitor 框架提供的 [NaiveHttpPost](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/http/NaiveHttpPost.java) 类，可以方便的实现一个基于 Http Post 请求的短信报警通知器：

```java
/**
 * 短信报警通知器。
 */
public class SmsServiceAlarmMessageNotifier implements ServiceAlarmMessageNotifier {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceAlarmMessageNotifier.class);
    
    /**
     * 短信服务 URL 地址。
     */
    private final String url;
    
    public SmsServiceAlarmMessageNotifier(String url) {
        this.url = url;
    }
    
    @Override
    public boolean send(ServiceAlarmMessage serviceAlarmMessage) {
        if (url != null && !url.isEmpty()) {
            long startTime = System.currentTimeMillis();
            HashMap<String, Object> params = new HashMap<>(); //组装短信服务需要的参数，应根据短信服务接口定义进行设置
            params.put("phoneNums", "13700000000,13811111111"); //设置短信接收人手机号码
            params.put("message", serviceAlarmMessage.toText()); //设置短信内容
            try {
                NaiveHttpPost post = new NaiveHttpPost(url, 5000); //Post 请求的超时时间设置为 5 秒
                String responseText = post.doPost(NaiveHttpPost.getPostBody(params)); //发起 Post 请求
                if ("true".equals(responseText)) { //根据返回内容判断请求是否成功，应根据短信服务接口返回值定义进行判断
                    LOGGER.info("Send ServiceAlarmMessage to sms success. Cost: `{} ms`. ServiceAlarmMessage: `{}`. Url: `{}`.",
                            System.currentTimeMillis() - startTime, serviceAlarmMessage, url);
                } else {
                    LOGGER.error("Send ServiceAlarmMessage to sms failed. Cost: `{} ms`. ResponseText: `{}`. ServiceAlarmMessage: `{}`. Url: `{}`.",
                            System.currentTimeMillis() - startTime, responseText, serviceAlarmMessage, url);
                }
                return isSuccess;
            } catch (Exception e) {
                LOGGER.error("Send ServiceAlarmMessage to sms failed. Cost: `" + (System.currentTimeMillis() - startTime)
                        + " ms`. ServiceAlarmMessage: `" + serviceAlarmMessage + "`. Url: `" + url + "`.", e);
                return false;
            }
        } else {
            LOGGER.warn("SmsServiceAlarmMessageNotifier is inactive. ServiceAlarmMessage: `{}`.", serviceAlarmMessage);
            return false;
        }
    }
}
```

## SQL 执行监控

DB 跪，网站跪。NaiveMonitor 框架提供了 [SqlExecutionMonitor](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/monitor/SqlExecutionMonitor.java) 来监控项目中的 SQL 执行。

如果你的项目仍在使用古老的 Spring + iBatis，NaiveMonitor 框架提供了直接的解决方案，仅需要通过 Spring 配置即可进行监控：

```xml
    <!-- 用于替换 Spring 的 iBatis 辅助类 SqlMapClientTemplate 配置 -->
    <bean id="bookstoreSqlMapClientTemplate" class="com.heimuheimu.naivemonitor.ibatis.SmartSqlMapClientTemplate">
        <constructor-arg index="0" value="bookstore" /> <!-- 数据库名称 -->
        <constructor-arg index="1" ref="bookstoreSqlMapClient" /> <!-- iBatis SqlMapClient 实例 -->
        <constructor-arg index="2" value="50" /> <!-- 大于该执行时间的 SQL 语句执行将会被定义为慢查，单位：毫秒 -->
    </bean>
    
    <!-- 在监控数据采集器列表中加入 SqlExecutionDataCollector-->
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址-->
        <constructor-arg index="1">
            <list>
                <!-- SQL 执行监控数据采集器-->
                <bean class="com.heimuheimu.naivemonitor.falcon.support.SqlExecutionDataCollector">
                    <constructor-arg index="0" value="bookstore" /> <!-- 当前项目名称 -->
                    <constructor-arg index="1" value="bookstore" /> <!-- 数据库名称 -->
                </bean>>
            </lit>
        </constructor-arg>
    </bean>
```

代码层无需修改，仍使用 Spring 提供的 SqlMapClientTemplate 进行访问：

```java
@Repository
public class UserDaoImpl implements UserDao {
    
    @Resource(name = "bookstoreSqlMapClientTemplate")
    private SqlMapClientTemplate sqlMapClientTemplate;
    
    public void add(User user) {
        sqlMapClientTemplate.insert("UserDao.add", user);
    }
}
```

SQL 执行日志配置：
```
#SQL 错误日志配置
log4j.logger.NAIVESQL_ERROR_EXECUTION_LOGGER=ERROR, NAIVESQL_ERROR_EXECUTION_LOGGER
log4j.additivity.NAIVESQL_ERROR_EXECUTION_LOGGER=false
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.file=${log.output.directory}/naivemonitor/sql_error.log
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.encoding=UTF-8
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

#SQL 慢查日志配置
log4j.logger.NAIVESQL_SLOW_EXECUTION_LOGGER=ERROR, NAIVESQL_SLOW_EXECUTION_LOGGER
log4j.additivity.NAIVESQL_SLOW_EXECUTION_LOGGER=false
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.file=${log.output.directory}/naivemonitor/sql_slow_execution.log
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.encoding=UTF-8
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.layout.ConversionPattern=%d{ISO8601} : %m%n
```

  完成以上工作后，在 Falcon 系统中可以找到以下数据项：
* SQL 执行错误数据项：
  * bookstore_bookstore_sql_error/module=bookstore 30 秒内 SQL 执行错误次数
  * bookstore_bookstore_sql_slow_execution/module=bookstore 30 秒内 SQL 慢查次数
* SQL 影响的行数数据项：
  * bookstore_bookstore_sql_max_result_size/module=bookstore 30 秒内单条 Select 语句返回的最大记录行数
  * bookstore_bookstore_sql_max_updated_rows/module=bookstore 30 秒内单条 Update 语句更新的最大行数
  * bookstore_bookstore_sql_max_deleted_rows/module=bookstore 30 秒内单条 Delete 语句删除的最大行数
* SQL 执行信息数据项：
  * bookstore_bookstore_sql_tps/module=bookstore &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 每秒平均执行次数
  * bookstore_bookstore_sql_peak_tps/module=bookstore &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 SQL 每秒最大执行次数
  * bookstore_bookstore_sql_avg_exec_time/module=bookstore &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次 SQL 操作平均执行时间
  * bookstore_bookstore_sql_max_exec_time/module=bookstore &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次 SQL 操作最大执行时间

如果你的项目在使用其它 ORM 框架，请参考 [SmartSqlMapClientTemplate](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/ibatis/SmartSqlMapClientTemplate.java)
类来实现 SQL 执行监控功能。

## 版本发布记录
### V1.1-SNAPSHOT
### 新增特性：
 * 新增 JVM 相关信息监控器：
   * 类加载信息
   * 垃圾回收信息
   * 内存使用信息
   * 线程信息
 * 支持将监控数据推送至 Prometheus 监控系统

***

### V1.0
### 特性：
 * 提供以下多种类型监控器：
   * 执行信息
   * 压缩操作信息
   * Socket 读、写信息
   * 线程池信息
   * SQL 执行信息
 * 钉钉机器人实时报警
 * 支持将监控数据推送至 Falcon 监控系统

## 更多信息
* [钉钉机器人开发官方文档](https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.a5dkCS&treeId=257&articleId=105735&docType=1)
* [[推荐使用] Prometheus 监控系统](https://prometheus.io/docs/introduction/overview/)
* [Falcon 监控系统](https://book.open-falcon.org/zh/)
* [NaiveMonitor v1.0 API Doc](https://heimuheimu.github.io/naivemonitor/api/v1.0/)
* [NaiveMonitor v1.0 源码下载](https://heimuheimu.github.io/naivemonitor/download/naivemonitor-1.0-sources.jar)
* [NaiveMonitor v1.0 Jar包下载](https://heimuheimu.github.io/naivemonitor/download/naivemonitor-1.0.jar)
