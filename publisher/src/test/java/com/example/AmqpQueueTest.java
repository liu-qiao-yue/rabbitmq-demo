package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-01
 * @Description:
 * @Version: 1.0
 */
@SpringBootTest
public class AmqpQueueTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 将消息发送到已有的队列中，忽略交换机
     * 队列类型：classic
     */
    @Test
    void testSentMessage() {
        String queueName = "simple.queue";
        String msg = "hi, simple.queue!";
        rabbitTemplate.convertAndSend(queueName, msg);
    }

    /**
     * work模型是指多个消费者消费该队列内容
     * 队列类型：classic
     *
     * Q: 如何解决消费堆积问题 -> 绑定多个xiao
     */
    @Test
    void testSentMessageByWorkModel() {
        String queueName = "work.queue";
        try {
            for (int i = 0; i < 50; i++) {
                String msg = "this is work msg for 【" + i + "】";
                rabbitTemplate.convertAndSend(queueName, msg);
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSentMessageWithObject() {
        Map<String, Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age", 18);
        map.put("sex", "男");
        map.put("address", "北京");

        rabbitTemplate.convertAndSend("object.queue", map);
    }

    @Test
    void testLazyQueue() {
        // 发送一百万次消息
        for (int i = 0; i < 1000000; i++) {
            rabbitTemplate.convertAndSend("simple.queue","hi, simple.queue!");
        }
    }
}
