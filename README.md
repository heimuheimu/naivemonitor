# NaiveMonitor: 快速实现数据监控和实时报警功能。

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>naivemonitor</artifactId>
        <version>1.0</version>
    </dependency>
```

## 数据监控 DEMO
  场景：对用户注册操作进行数据监控（注意：示例代码仅为说明如何使用 [ExecutionMonitor](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/monitor/ExecutionMonitor.java)。）
```java
@Controller
public class UserRegisterController { //用户注册使用的 Controller，数据监控可根据自身需求放在任意层级，例如 Service 层、Dao 层。
    
    private final ExecutionMonitor monitor = NaiveExecutionMonitorFactory.get("UserRegister"); //获取用户注册操作执行信息监控器
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/register")
    @ResponseBody
    public boolean register(@RequestParam("username") String username, @RequestParam("password") String password) {
        long startTime = System.nanoTime();
        try {
            if (userService.isExist(username)) { //如果用户名已存在
                monitor.onError(-1); //对用户名已存在的错误进行监控
                return false;
            }
            userService.add(username, password); //创建用户
            return true;
        } catch (Execution e) {
            monitor.onError(-2); //对执行过程中出现预期外异常进行监控
            return false;
        } finally{
            monitor.onExecuted(startTime); //对每一个注册操作进行监控
        }
    }
}
```
  实现用户注册操作 Falcon 监控数据采集器：
```java
public class UserRegisterDataCollector extends AbstractExecutionDataCollector {
    
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
        return Collections.singletonList(NaiveExecutionMonitorFactory.get("UserRegister"));
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
  在 Spring 中配置 Falcon 数据推送：
```xml
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址-->
        <constructor-arg index="1">
            <list>
                <bean class="com.heimuheimu.naivemonitor.demo.falcon.UserRegisterDataCollector" />
            </lit>
        </constructor-arg>
    </bean>
```
   完成以上工作后，在 Falcon 系统中可以找到以下数据项：
* bookstore_register_tps/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均用户注册次数
* bookstore_register_peak_tps/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大用户注册次数
* bookstore_register_avg_exec_time/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次注册操作平均执行时间
* bookstore_register_max_exec_time/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次注册操作最大执行时间
* bookstore_register_duplicate_username/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内出现用户名已存在的错误次数
* bookstore_register_exec_error/module=bookstore    &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内出现预期外的异常次数

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
#SQL 慢查日志配置
log4j.logger.NAIVESQL_ERROR_EXECUTION_LOGGER=ERROR, NAIVESQL_ERROR_EXECUTION_LOGGER
log4j.additivity.NAIVESQL_ERROR_EXECUTION_LOGGER=false
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.file=${log.output.directory}/naivemonitor/sql_error.log
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.encoding=UTF-8
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVESQL_ERROR_EXECUTION_LOGGER.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

#SQL 错误日志配置
log4j.logger.NAIVESQL_SLOW_EXECUTION_LOGGER=ERROR, NAIVESQL_SLOW_EXECUTION_LOGGER
log4j.additivity.NAIVESQL_SLOW_EXECUTION_LOGGER=false
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.file=${log.output.directory}/naivemonitor/sql_slow_execution.log
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.encoding=UTF-8
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVESQL_SLOW_EXECUTION_LOGGER.layout.ConversionPattern=%d{ISO8601} : %m%n
```

如果你的项目在使用其它 ORM 框架，请参考 [SmartSqlMapClientTemplate](https://github.com/heimuheimu/naivemonitor/blob/master/src/main/java/com/heimuheimu/naivemonitor/ibatis/SmartSqlMapClientTemplate.java)
类来实现 SQL 执行监控功能。

## 更多信息
* [Falcon 监控数据推送官方文档](https://book.open-falcon.org/zh/usage/data-push.html)
* [钉钉机器人开发官方文档](https://open-doc.dingtalk.com/docs/doc.htm?spm=a219a.7629140.0.0.a5dkCS&treeId=257&articleId=105735&docType=1)
* [NaiveMonitor v1.0 API Doc](https://heimuheimu.github.io/naivemonitor/api/v1.0/)
* [NaiveMonitor v1.0 源码下载](https://heimuheimu.github.io/naivemonitor/download/naivemonitor-1.0-sources.jar)
* [NaiveMonitor v1.0 Jar包下载](https://heimuheimu.github.io/naivemonitor/download/naivemonitor-1.0.jar)
