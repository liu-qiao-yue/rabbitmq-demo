package com.example.consumer.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-01
 * @Description: exchange 交换机分为三类：
 * <li> Fanout广播模式：会将收到的信息广播到每一个跟其绑定的queue。解决的是消息要被多个微服务接受，而不是消费者</li>
 * <li> 
 *     Direct定向模式：会将收到的消息根据规则路由到制动的queue。
 *     <p>- 每一个queue都与exchange设置一个bindingKey</p>
 *     <p>- 发布者发送消息时，指定消息的RoutingKey</p>
 *     <p>- exchange将消息路由到BindingKey与消息RoutingKey一致的队列</p>
 * </li>
 * <li>
 *     Topic话题模式：与Direct类似，区别于routingKey可以是多个单词的列表，并且以“.”分割
 *     <p>- # 指代0或多个单词</p>
 *     <p>- * 指代一个单词</p>
 * </li>
 * @Version: 1.0
 */
@Slf4j
@Component
public class SpringRabbitWithExchange {


    /*--------------------------  fanout  -------------------------------------*/

    @RabbitListener(queues = {"fanout.queue1"})
    public void fanoutQueue1(String msg) {
        log.info("fanoutQueue1 接收到消息：{}", msg);
    }

    @RabbitListener(queues = {"fanout.queue2"})
    public void fanoutQueue2(String msg) {
        log.info("fanoutQueue2 接收到消息：{}", msg);
    }

    /*--------------------------  direct  -------------------------------------*/
    @RabbitListener(queues = {"direct.queue1"})
    public void directQueue1(String msg) {
        log.info("directQueue1 接收到消息：{}", msg);
    }

    @RabbitListener(queues = {"direct.queue2"})
    public void directQueue2(String msg) {
        log.info("directQueue2 接收到消息：{}", msg);
    }

    /*--------------------------  topic  -------------------------------------*/
    @RabbitListener(queues = {"topic.queue1"})
    public void topicQueue1(String msg) {
        log.info("topicQueue1 接收到消息：{}", msg);
    }

    @RabbitListener(queues = {"topic.queue2"})
    public void topicQueue2(String msg) {
        log.info("topicQueue2 接收到消息：{}", msg);
    }


}
