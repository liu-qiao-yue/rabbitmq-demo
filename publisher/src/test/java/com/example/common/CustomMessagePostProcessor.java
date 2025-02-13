package com.example.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-13
 * @Description:
 * @Version: 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
public class CustomMessagePostProcessor implements MessagePostProcessor {

    public Long delay = 1000L;

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setDelayLong(delay);
        return message;
    }
}
