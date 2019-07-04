package com.example.demo.config;

import com.example.demo.core.JobSchedulerFactory;
import com.example.demo.core.annotation.MyScheduleJob;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Set;

/**
 * @program: springboot-quartz
 * @description: 配置任务调度中心 [QRTZ_JOB_DETAILS], [QRTZ_TRIGGERS] and [QRTZ_CRON_TRIGGERS]
 * @author: cyz
 * @create: 2018-07-27 17:43
 **/
@Configuration
//@EnableScheduling
public class QuartzConfig implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(QuartzConfig.class);
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.username}")
    private String jdbcUsername;
    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    @Autowired
    JobSchedulerFactory jobSchedulerFactory;

    @Bean(name ="mySchedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        // 延时启动
        factory.setStartupDelay(20);
        // 加载quartz数据源配置
        factory.setQuartzProperties(quartzProperties());
        // 自定义Job Factory，用于Spring注入
        factory.setJobFactory(jobSchedulerFactory);

        return factory;
    }

    /**
     * 设置quartz属性
     *
     * @throws IOException 2016年10月8日下午2:39:05
     */
    public Properties quartzProperties() throws IOException {
        Properties prop = new Properties();
        prop.put("quartz.scheduler.instanceName", "ServerScheduler");
        prop.put("org.quartz.scheduler.instanceId", "AUTO");
        prop.put("org.quartz.scheduler.skipUpdateCheck", "true");
        prop.put("org.quartz.scheduler.instanceId", "NON_CLUSTERED");
        prop.put("org.quartz.scheduler.jobFactory.class", "org.quartz.simpl.SimpleJobFactory");
        prop.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        prop.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        prop.put("org.quartz.jobStore.dataSource", "quartzDataSource");
        prop.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        prop.put("org.quartz.jobStore.isClustered", "true");
        prop.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        prop.put("org.quartz.threadPool.threadCount", "5");

        prop.put("org.quartz.dataSource.quartzDataSource.driver", "com.mysql.jdbc.Driver");
        prop.put("org.quartz.dataSource.quartzDataSource.URL", jdbcUrl);
        prop.put("org.quartz.dataSource.quartzDataSource.user", jdbcUsername);
        prop.put("org.quartz.dataSource.quartzDataSource.password", jdbcPassword);
        prop.put("org.quartz.dataSource.quartzDataSource.maxConnections", "10");
        prop.put("org.quartz.dataSource.quartzDataSource.connectionProvider.class", "com.example.demo.core.DruidConnectionProvider");
        return prop;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext annotationContext = MyApplicationContextAware.getApplicationContext();
        StdScheduler stdScheduler = (StdScheduler) annotationContext.getBean("mySchedulerFactoryBean");//获得上面创建的bean
        Scheduler myScheduler =stdScheduler;

         logger.info("=====初始化定时任务列表======");
         //通过Reflections反射工具，找到含MyScheduleJob的方法和类
         ConfigurationBuilder builder = new ConfigurationBuilder()
         .filterInputsBy(new FilterBuilder().includePackage("com.example.demo"))
         .addUrls(ClasspathHelper.forPackage("com.example.demo"))
         .setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner(), new TypeElementsScanner(), new SubTypesScanner());
         Reflections reflections = new Reflections(builder);
         Set<Method> methods = reflections.getMethodsAnnotatedWith(MyScheduleJob.class);
         try {
             myScheduler.clear();
             for (Method method : methods){
                 MyScheduleJob myScheduleJob = AnnotationUtils.findAnnotation(method, MyScheduleJob.class);
                     if (null != myScheduleJob) {
                         Class clazz = method.getDeclaringClass();
                         logger.info("===beanName==="+clazz.getName());
                         logger.info("===method.name==="+method.getName());
                         logger.info("===myScheduleJob.cron==="+myScheduleJob.cron());
                         JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) clazz).withIdentity(clazz.getSimpleName(), "jobGroup").build();
                         CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(myScheduleJob.cron());
                         CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(clazz.getSimpleName()+"trigger", "triggerGroup")
                         .withSchedule(cronScheduleBuilder).build();
                         myScheduler.scheduleJob(jobDetail, trigger);
                     }
             }
             //            myScheduler.start();
             myScheduler.startDelayed(30);
         } catch (Exception e) {
            logger.warn("初始化定时任warn "+e);
         }
    }
}
