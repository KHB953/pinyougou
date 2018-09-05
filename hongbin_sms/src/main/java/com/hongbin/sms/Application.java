package com.hongbin.sms;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.jms.Queue;

/**
 * spring boot 引导类
 * 1、自动装配
 * 2、组件扫描
 * 3、默认扫描当前引导类所在的包以及子包下所有的注解
 *
 * @author hongbin
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }



    @Bean
    public Queue createQueue(){
        return  new ActiveMQQueue("sms");

    }
}
