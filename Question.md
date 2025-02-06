# 面试题

## 1. 如何解决消费堆积问题
## 2. RabbitMQ如何确保消息的可靠性

    1.确保交换机、队列以及消息发送的持久性->持久化到磁盘
    2. 3.12后默认队列是持久化的。lazyQueue会将所有消息都持久化
    3. 开启生产者确认机制，RabbitMQ只有在完成消息持久化之后才会给生产者返回ACK回执

## 3. 消费者如何保证消息一定被消费了呢

    1. 开启消费者确认机制为auto，由spring确认消息处理成功后返回ack，异常时返回nack
    2. 开启消费者失败重试机制，并设置 MessageRecoverer，多次重试失败后将消息投递到异常交换机，交由人工处理

## 4. 如何保证支付服务与交易服务之间的订单状态一致性？
    
    1. 支付服务会正在用户支付成功以后利用MQ消息通知交易服务，完成订单状态同步。
    2. 为了保证MQ消息的可靠性，我们采用了生产者确认机制、消费者确认、消费者失败重试等策略，确保消息投递和处理的可靠性。同时也开启了MQ的持久化，避免因服务宕机导致消息丢失。
    3. 我们还在交易服务更新订单状态时做了业务幂等判断，避免因消息重复消费导致订单状态异常。

## 5. 如果交易服务消息处理失败，有没有什么兜底方案？

    我们可以在交易服务设置定时任务，定期查询订单支付状态。这样即便MQ通知失败，还可以利用定时住务作为晃展万案，确保订畢文付状态的最终一致性




---
# 消费者可靠性
## 1. 生产者可靠性-生产者重连

   - 主要针对网络问题导致的连接失败

    ``` yml
      spring:
        rabbitmq:
          connection-timeout: 1s # 连接超时时间
          template:
            retry:
              enabled: true # 开启超时重试机制
              initial-interval: 1000ms # 失败后的初始等待时间
              multiplier: 1 # 失败后下次的等待时长倍数，下次等待时长 = initial-interval * multiplier，例如 初始等待时间 = 1000ms，倍数为1，下次等待时间 = 1000ms * 1 = 1000ms
              max-attempts: 3 # 最大重试次数
    ```

   >  **注意**：SpringAMQP提供的充实机制是***阻塞式***的充实，也就是说多次重试等待的过程中，当前线程式呗阻塞的，会影响业务性能。如果对业务性能有要求的，建议
     ***禁用***重试机制。

## 2. 生产者可靠性-生产者确认
   
   - RabbitMQ有两种确认机制：public confirm 和 public return，开启确认机制后，在MQ成功收到消息后回返回确认消息给生产者
   > **返回结果**有以下几种情况：
   > - 消息投递到了MQ，但是路由失败。此时会通过publisherReturn返回路由异常原因，**然后返回ACK，告知投递成功**
   > - 临时消息投递到了MQ，并且入队成功，返回ACK，告知投递成功
   > - 持久消息投递到MQ，并且入队完成持久化，返回ACK，告知投递成功
   > - 其他情况都会返回NACK，告知投递失败
   
### 代码实现

   1. 在生产者application.yml中添加以下内容
      ``` yml
      spring:
        rabbitmq:
          publisher-confirm-type: correlated # 开启消息确认功能，并设置confirm类型
          publisher-returns: true # 开启消息发送失败返回功能
      ```

      > publisher-confirm-type有三种模式：
      > - NONE 默认，表示不进行任何确认
      > - simple：同步阻塞等待MQ的回执消息
      > - correlated：MQ异步回调方式返回回调信息
   
   2. returnCallBack代码，只关心消息无法正确路由到任何队列的情况。[code](./publisher/src/main/java/com/example/common/RabbitMQConfig.java)
   3. ConfirmCallBack代码，要每一个生产调用时单独指定。

## 3. MQ可靠性 - lazy queue，从3.12开始经典模式下默认使用lazy queue

**AMQP创建方式**

```java
// 1. 使用@Bean
@Bean
public Queue lazyQueue() {
    return QueueBuilder.durable("lazy.queue").lazy().build();
}

// 2. 使用@RabbitListener
@RabbitListener(queuesToDeclare = @Queue(
        name = "lazy.queue3",
        durable = "true",
        arguments = @Argument(name = "x-queue-mode", value = "lazy")
))
public void consumerLazyMessage(String msg) {
    log.info("spring AMQP lazy queue 接收到消息 {}", msg);
}
```

## 4. 消费者确认

- RabbitMQ提供了消费者确认机制。当消费者处理消息结束后，应该向RabbitMQ发送一个回执，告知RabbitMQ自己消息处理状态，回执有三种类型：
  
  - ack：成功处理消息，RabbitMQ从队列中删除该消息
  - nack:消息处理失败，RabbitMQ将消息重新放入队列，让消费者重新处理
  - reject:消息处理失败并拒绝该消息，RabbitMQ将消息从队列中删除
---
- SpringAMQP已经实现了消息确认功能，并通过配置选择ACK处理方式，有三种方式

    - none：不处理。即消息消息投递给消费者后直接返回ACK，让RabbitMQ删除该消息。
    - manual：手动处理。需要自己在业务代码中调用api，发送ack或reject，存在业务入侵，但更灵活
    - auto：自动处理。SpringAMQP利用AOP对我们的消息处理逻辑做了环绕增强，当业务代码正常执行是就会返回ack，当出现异常时：

        - 如果是业务异常：自动返回nack，并且消息会重新放入队列，让消费者重新处理。【但是会一直投递】
        - 如果是消息处理或则校验异常：自动返回reject


## 5. 失消息败处理机制

```yml
spring:
  rabbitmq: 
    listener:
      simple:
        prefetch: 1 #每次只能获取一条信息，处理完成后才能获取下一个信息
        acknowledge-mode: auto # 确认机制
        retry:
          enabled: true # 开启消费者失败重试
          initial-interval: 1000ms # 初始的失败等待时长为 1 秒
          multiplier: 1 # 下次失败的风带时长倍数，下次等待时长 = multiplier * last-interval
          max-attempts: 3 # 最大重试次数，超过后放弃该队列消息
          stateless: true # true 无状态 false 有状态，如果业务中包含事务，这里改为false
```

> **注意**：在开启重试模式后。重试次数耗尽后，如果消息仍然失败，则需要由MessageRecoverer 接口来处理，它包含三种不同的实现：
> 
> - RejectAndDontRequeueRecoverer: 重试耗尽后，直接reject，丢弃消息，默认就是这样的方式
> - ImmediateRequeueMessageRecoverer: 重试耗尽后，返回nack，消息重新入队
> - RepublishMessageRecoverer: 重试耗尽后，将失败消息投递到指定的交换机

```java
@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry", name = "enabled", havingValue = "true")
public class ErrorConfig {

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
    }
}
```

## 6. 业务幂等性

- 幂等性是指一个业务请求，无论执行多少次，最终结果都一样。

**解决方案**
1. 给每个消息都设置**唯一id**，利用id区分是否时重复消息：
   - 每一条消息都设置一个唯一id，利与消息一起投递给消费者
   - 消费者收到消息后处理自己的业务，业务处理辰宫后将消息id保存在数据库中
   - 如果下次由收到相同的消息，去数据库查询判断是否存在，存在则为重复消息放弃处理
    
    ```java
        @Bean
        public MessageConverter messageConverter() {
            Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
            // 配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
            jackson2JsonMessageConverter.setCreateMessageIds(true);
            return jackson2JsonMessageConverter;
        }
    ```
   
2. 是结合业务逻辑，基于业务本身做判断。
---

# 延迟消息