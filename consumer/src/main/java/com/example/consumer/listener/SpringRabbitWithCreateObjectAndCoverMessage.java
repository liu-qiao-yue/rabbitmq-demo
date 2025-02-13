package com.example.consumer.listener;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-04
 * @Description: 在AMQP中创建队列、交换机、绑定关系，要求放在消费者端，不能放在生产者端
 * <p>
 * 1. 创建队列：使用 QueueBuilder构建
 * 2. 创建交换机：使用 ExchangeBuilder构建
 * 3. 绑定关系：使用 BindingBuilder构建
 * </p>
 * 第二种方式：使用注解方式
 * @Version: 1.0
 */
@Component
public class SpringRabbitWithCreateObjectAndCoverMessage {
    /**
     * 创建 fanout 类型交换机
     *
     * @return
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        // 或者 ExchangeBuilder.fanoutExchange("hmall.fanout2");
        return new FanoutExchange("hmall.fanout2");
    }

    /**
     * 创建队列
     *
     * @return
     */
    @Bean
    public Queue fanoutQueue3() {
//         return QueueBuilder.durable("lazy.queue3").lazy().build(); //durable: 是否持久化
        return new Queue("fanout.queue3");
    }

    /**
     * 创建交换机和队列的绑定关系
     *
     * @param fanoutExchange
     * @param fanoutQueue3
     * @return
     */
    @Bean
    public Binding fanoutBinding3(FanoutExchange fanoutExchange, Queue fanoutQueue3) {
        // BindingBuilder.bind(fanoutQueue3).to(fanoutExchange);
        return BindingBuilder.bind(fanoutQueue3).to(fanoutExchange);
    }

    /* ----------------
     *
     * 创建annotation.direct 类型的交换机
     * 创建annotation.queue1和annotation.queue2队列
     * 设置绑定关系：
     * annotation.queue1: red & blue
     * annotation.queue2: red & green
     *
     * -----------------
     */


    /**
     * bindings是数组，以下是示例
     *
     * @param msg
     * @RabbitListener(bindings =
     * {
     * @QueueBinding( value = @org.springframework.amqp.rabbit.annotation.Queue(name = "annotation.queue1", durable = "true"),
     * exchange = @org.springframework.amqp.rabbit.annotation.Exchange(name = "annotation.direct", type = ExchangeTypes.DIRECT),
     * key = {"red", "blue"}),
     * @QueueBinding( value = @org.springframework.amqp.rabbit.annotation.Queue(name = "annotation.queue2", durable = "true"),
     * exchange = @org.springframework.amqp.rabbit.annotation.Exchange(name = "annotation.direct", type = ExchangeTypes.DIRECT),
     * key = {"red", "green"}
     * )
     * }
     * )
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @org.springframework.amqp.rabbit.annotation.Queue(name = "annotation.queue1", durable = "true"),
            exchange = @org.springframework.amqp.rabbit.annotation.Exchange(name = "annotation.direct", type = ExchangeTypes.DIRECT),
            key = {"red", "blue"}
    ))
    public void directQueue1(String msg) {
        System.out.println("annotation.queue1: " + msg);
    }


//    @Bean
//    public MessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }

    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        // 配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }

}
