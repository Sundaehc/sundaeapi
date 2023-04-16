package com.sundae.project.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置
 * @author sundae
 */
public class RabbitMqConfig {

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue smsQueue(){
        Map<String, Object> arguments = new HashMap<>();
        //声明死信队列和交换机
        arguments.put("x-dead-letter-exchange", "sms-exchange");
        arguments.put("x-dead-letter-routing-key", "sms.release");
        arguments.put("x-message-ttl", 60000); // 消息过期时间：1分钟
        return new Queue("api-sms-queue",true,false,false ,arguments);
    }

    /**
     * 死信队列：消息重试三次后放入死信队列
     * @return
     */
    @Bean
    public Queue deadLetter(){
        return new Queue("sms.delay.queue", true, false, false);
    }

    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange smsExchange() {
        return new TopicExchange("sms-exchange", true, false);
    }


    /**
     * 交换机和普通队列绑定
     * @return
     */
    @Bean
    public Binding smsBinding(){
        return new Binding("api-sms-queue", Binding.DestinationType.QUEUE,"sms-exchange","sms-send",null);
    }

    /**
     * 交换机和死信队列绑定
     * @return
     */
    @Bean
    public Binding smsDelayBinding(){
        return new Binding("sms.delay.queue", Binding.DestinationType.QUEUE,"sms-exchange","sms.release",null);
    }

}
