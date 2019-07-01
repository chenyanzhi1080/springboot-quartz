package com.example.demo.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @program: springboot-quartz
 * @description: 自定义定时任务注释
 * @author: cyz
 * @create: 2018-07-30 09:48
 **/
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyScheduleJob {
    String cron() default "";
}
