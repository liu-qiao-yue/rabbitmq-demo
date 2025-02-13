package com.example.consumer.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-01
 * @Description:
 * @Version: 1.0
 */
@Slf4j
@Component
public class SpringRabbitWithQueues {
    /**
     * 消费队列中的消息，队列是已经创建的
     *
     * @param msg 发送的消息，类型要与发送者保持一致
     * @RabbitListener queues 可以是多个String组成的数组
     */
    @RabbitListener(queues = "simple.queue")
    public void consumerMessage(String msg) throws InterruptedException {
        log.info("spring AMQP 接收到消息 {}", msg);
//        throw new RuntimeException("故意抛出的异常");
    }



    /*------------------------  work 模型 -----------------------------*/

    /**
     * 默认写法下无论消费者速度如何都是轮流分配
     * 解决办法是在 application.yml 中增加 spring.rabbitmq.listener.simple.prefetch: 1
     * 即每次只能获取一条信息，处理完成后才能获取下一个信息
     *
     * @RabbitListener(queues = "work.queue")
     * public void consumerMessageWIthWorkModel1(String msg){
     * log.info("spring AMQP work 模型 1 接收到消息 {}", msg);
     * Thread.sleep(20);
     * }
     **/

    @RabbitListener(queues = "work.queue")
    public void consumerMessageWIthWorkModel1(String msg) throws InterruptedException {
        log.info("spring AMQP work 模型 1 接收到消息 {}", msg);
        Thread.sleep(20);
    }

    @RabbitListener(queues = "work.queue")
    public void consumerMessageWIthWorkModel2(String msg) throws InterruptedException {
        log.warn("spring AMQP work 模型 2 接收到消息 {}", msg);
        Thread.sleep(200);
    }

    @RabbitListener(queuesToDeclare = @Queue(
            name = "lazy.queue3",
            durable = "true",
            arguments = @Argument(name = "x-queue-mode", value = "lazy")
    ))
    public void consumerLazyMessage(String msg) {
        log.info("spring AMQP lazy queue 接收到消息 {}", msg);
    }


    @RabbitListener(queues = "dead.queue")
    public void consumerMessageWIthTTL(Object msg) throws InterruptedException {
        log.warn("死信队列消息 {}", msg);
    }
}
