package com.example.common;

import org.springframework.amqp.rabbit.connection.CorrelationData;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-05
 * @Description:
 * @Version: 1.0
 */
public class EnhancedCorrelationData extends CorrelationData {
    private final String body;

    public EnhancedCorrelationData(String id, String body) {
        super(id);
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}


