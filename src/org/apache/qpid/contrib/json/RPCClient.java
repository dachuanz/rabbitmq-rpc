package org.apache.qpid.contrib.json;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.qpid.contrib.json.utils.BZip2Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * 使用json进行序列化，方便进行观察调试
 * 
 * @author zdc
 * @param <T>
 * @since 2015年5月27日
 */
public class RPCClient {

    private Connection connection;

    private Channel channel;

    boolean isCompress;

    // String className;

    private String requestQueueName = "rpc_queue";

    private String replyQueueName;

    private QueueingConsumer consumer;

    public RPCClient() throws Exception {
        //  先建立一个连接和一个通道，并为回调声明一个唯一的'回调'队列
        Configuration configuration = new PropertiesConfiguration("config/rabbitmq.properties");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configuration.getString("hostname"));
        factory.setUsername(configuration.getString("username"));
        factory.setPassword(configuration.getString("password"));
        this.isCompress = configuration.getBoolean("isCompress");
        factory.setPort(AMQP.PROTOCOL.PORT);
        connection = factory.newConnection();
        channel = connection.createChannel();
        // 注册'回调'队列，这样就可以收到RPC响应
        replyQueueName = channel.queueDeclare().getQueue();// 生成回调队列
        // System.out.println("[回调]" + replyQueueName);
        consumer = new QueueingConsumer(channel);// 创建消费者
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
                    System.out.println(params[j].getName());
                    paramTypes.add(params[j].getName());
                }
                map.put("parameterTypes", paramTypes);

                if (args != null) {
                    map.put("args", Arrays.asList(args));
                }
                String json = JSON.toJSONString(map, SerializerFeature.WriteClassName);

                System.out.println(json);

                return call(json, method);
            }
        });
    }

    // 发送RPC请求
    public Object call(String message, Method method) throws Exception {
        String response = null;
        String corrId = UUID.randomUUID().toString();// 为每个调用生成唯一的相关ID
        // System.out.println(corrId);
        // 发送请求消息，消息使用了两个属性：replyto和correlationId
        byte[] s = null;
        if (isCompress) {
           // System.out.println("压缩前长度" + message.getBytes().length);

            s = BZip2Utils.compress(message.getBytes());

            //System.out.println("压缩后长度" + s.length);
        } else {
            s = message.getBytes();
        }

        BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();
        channel.basicPublish("", requestQueueName, props, s);// 将RPC请求消息发送到请求队列中
        // 等待接收结果
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            // 检查它的correlationId是否是我们所要找的那个
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody());
                // System.out.println("[反馈]" + response);
                break;
            }
        }
        return JSON.parseObject(response, method.getReturnType());
    }

    public void close() throws Exception {
        // channel.abort();
        // channel.close();
        connection.close();
    }
}
