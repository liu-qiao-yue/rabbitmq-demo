package com.example;

import com.example.common.CustomMessagePostProcessor;
import com.example.common.EnhancedCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.example.common.Utils.loadJson;

/**
 * @Author: ellie
 * @CreateTime: 2025-02-01
 * @Description:
 * @Version: 1.0
 */
@Slf4j
@SpringBootTest
public class AmqpExchangeTest {

    private List<Map<String, String>> testDatas;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @BeforeEach
    void init() throws IOException {
        testDatas = loadJson("testJson.json", List.class);


        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) ->{
            log.info("id{}, body{}",((EnhancedCorrelationData)correlationData).getId(), ((EnhancedCorrelationData)correlationData).getBody());
            if(ack){
                // 交换机 | 队列
                // √     | √
                // √     | ×(reason 在returnCallBack中提示，大概率是代码问题)
                log.info("confirm消息发送成功");
            }else {
                // ×(exchange错误)
                log.error("confirm消息发送失败，原因{}", cause);
            }
        });
    }


    /**
     * 声明fanout.queue1队列和fanout.queue2队列
     * 声明交换机 hmall.fanout，并绑定到fanout.exchange交换机上
     * 发送消息到fanout.exchange交换机上，两个队列都会收到消息
     */
    @Test
    void testSendToExchangeWithFanout() {
        String exchangeName = "hmall.fanout";
        String msg = "hello, everyone";
        rabbitTemplate.convertAndSend(exchangeName, "", msg);
    }

    /**
     * 声明direct.queue1队列和direct.queue2队列
     * 声明交换机 hmall.direct，并将两个队列绑定到 hmall.direct交换机上
     * queue1: routingKey = red || blue, queue1收到消息
     * queue2: routingKey = red || yellow, queue2收到消息
     */
    @Test
    void testSendToExchangeWithDirect() {
        testDatas.stream()
                .filter(json -> json.get("key").equals("direct"))
                .forEach(i->{
                    String exchangeName = i.get("exchangeName");
                    String routingKey = i.get("routingKey");
                    String msg = i.get("msg");
                    rabbitTemplate.convertAndSend(exchangeName, routingKey, msg);
                });
    }

    @Test
    void testSendToExchangeWithTopic() {
        testDatas.stream()
                .filter(json -> json.get("key").equals("topic"))
                .forEach(i->{
                    String exchangeName = i.get("exchangeName");
                    String routingKey = i.get("routingKey");
                    String msg = i.get("msg");
                    rabbitTemplate.convertAndSend(exchangeName, routingKey, msg);
                });
    }

    @Test
    void testConfirmCallBack() throws ExecutionException, InterruptedException {
        String exchangeName = "hamll.direct";
        String msg = "hello, everyone";

        CorrelationData md = new EnhancedCorrelationData(UUID.randomUUID().toString(), msg);

        rabbitTemplate.convertAndSend(exchangeName, "red00", msg, md);

//        try {
//            CorrelationData.Confirm confirm = md.getFuture().get(100, TimeUnit.SECONDS);
//            if(confirm.isAck()){
//                // 交换机 | 队列
//                // √     | √
//                // √     | ×(reason 在returnCallBack中提示，大概率是代码问题)
//                log.info("confirm消息发送成功");
//            }else {
//                // ×(exchange错误)
//                log.error("confirm消息发送失败，原因{}", confirm.getReason());
//            }
//        }catch (TimeoutException timeoutException){
//            // 可能是由于rabbit挂了、网络问题等，这里应该是重新发送
//            log.info("confirm消息发送超时");
//        }



    }


    @Test
    void testSendDelayMessage() throws InterruptedException {

        rabbitTemplate.convertAndSend(
                "delay.direct",
                "hi",
                "hello, i am a message with delay queue",
//                new CustomMessagePostProcessor(10000L)
                msg -> {
                    msg.getMessageProperties().setDelayLong(10000L);
                    return msg;
                }
        );
    }
}
