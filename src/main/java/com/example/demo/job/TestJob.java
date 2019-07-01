package com.example.demo.job;

import com.example.demo.core.annotation.MyScheduleJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * @program: springboot-quartz
 * @description: 测试job
 * @author: cyz
 * @create: 2018-07-30 09:56
 **/
@Component
public class TestJob implements Job {
    /**
     * 测试自定义定时任务注释
     * @param context
     * @throws JobExecutionException
     */
    @MyScheduleJob(cron = "0 0/2 * * * ?")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("=======TestJob 测试自定义定时任务注释  ========="+LocalTime.now());
    }
    @Scheduled(cron = "0 0/5 * * * ?")
    public void testCron(){
        System.out.println("=======TestJob 测试Spring定时任务   ========="+LocalTime.now());
    }
}
