package com.example.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-05
 * @Description: 只关心消息无法正确路由到任何队列的情况
 * @Version: 1.0
 */
@Slf4j
@Component
public class RabbitMQConfig implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        rabbitTemplate.setReturnsCallback(returnedMessage ->
                log.info("消息发送失败，应答码：{}，原因：{}，交换机：{}，路由器：{}，消息：{}",
                        returnedMessage.getReplyCode(),
                        returnedMessage.getReplyText(),
                        returnedMessage.getExchange(),
                        returnedMessage.getRoutingKey(),
                        new String(returnedMessage.getMessage().getBody()))
        );

    }
}
