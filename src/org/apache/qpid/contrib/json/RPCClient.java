
package org.apache.qpid.contrib.json;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class RPCClient {

    private Connection connection;

    private Channel channel;

    private String requestQueueName = "rpc_queue";

    private String replyQueueName;

    private QueueingConsumer consumer;

    public RPCClient() throws Exception {
        // • 先建立一个连接和一个通道，并为回调声明一个唯一的'回调'队列
        Configuration configuration = new PropertiesConfiguration("config/rabbitmq.properties");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configuration.getString("hostname"));
        factory.setUsername(configuration.getString("username"));
        factory.setPassword(configuration.getString("password"));
        factory.setPort(AMQP.PROTOCOL.PORT);
        connection = factory.newConnection();
        channel = connection.createChannel();
        // • 注册'回调'队列，这样就可以收到RPC响应
        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    @SuppressWarnings("unchecked")
    public <T> T createRpcClient(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class == null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("The " + interfaceClass.getName() + " must be interface class!");
        }

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass }, new InvocationHandler() {

            Map map = new HashMap();

            List paramTypes = new ArrayList();

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                map.put("method", method.getName());
                Class[] params = method.getParameterTypes();

                for (int j = 0; j < params.length; j++) {
                    paramTypes.add(params[j].getName());
                }
                map.put("parameterTypes", paramTypes);

                map.put("args", Arrays.asList(args));
                String json = JSON.toJSONString(map);

                // System.out.println(json);

                return call(json);
            }
        });
    }

    // 发送RPC请求
    public Object call(String message) throws Exception {
        String response = null;
        String corrId = java.util.UUID.randomUUID().toString();
        // 发送请求消息，消息使用了两个属性：replyto和correlationId
        BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).deliveryMode(2).build();
        channel.basicPublish("", requestQueueName, props, message.getBytes());
        // 等待接收结果
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            // 检查它的correlationId是否是我们所要找的那个
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody());
                break;
            }
        }
        return JSON.parse(response);
    }

    public void close() throws Exception {
        connection.close();
    }
}
