
package org.apache.qpid.contrib.json;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author zdc
 * @since 2015年5月28日
 */
public class MessageSendUtils {

    /**
     * 
     * @param queueName
     *            队列名称
     * @param object
     *            要传送的对象
     * @throws IOException
     * @throws ConfigurationException
     */
    public static void sendMessage(String queueName, Object... object) throws IOException, ConfigurationException {
        Configuration configuration = new PropertiesConfiguration("config/rabbitmq.properties");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configuration.getString("hostname"));
        factory.setUsername(configuration.getString("username"));
        factory.setPassword(configuration.getString("password"));
        BasicProperties basicProps = new BasicProperties.Builder().contentType("application/json").deliveryMode(2).build();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // 声明此队列并且持久化
        channel.queueDeclare(queueName, true, false, false, null);
        Object[] messages = object;
        for (Object object2 : messages) {
            String jsonMessage = JSON.toJSONString(object2, SerializerFeature.WriteClassName);
            System.out.println(jsonMessage);
            channel.basicPublish("", queueName, basicProps, jsonMessage.getBytes());// 持久化消息
        }

        channel.close();
        connection.close();

    }

}
