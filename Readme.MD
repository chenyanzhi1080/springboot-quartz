###### 自定义定时任务注解MyScheduleJob
```
@MyScheduleJob(cron = "0 0/1 * * * ?")
```
>  @MyScheduleJob 目前只支持cron方法，时间表达式参考Schedule 的cron
>  @MyScheduleJob不影响@ScheduleJob的使用

###### 温馨提示
> 如果对spingboot的配置不清楚，可以阅读 spring-boot-autoconfigure-*.*.*.RELEASE.jar 包里面的spring-configuration-metadata.json

> 本demo还在完善中,将补充详细对说明

> 本demo用flyway 管理数据库版本，本地运行本demo，只需创建一个空的数据库，直接运行，就会自动初始化数据库