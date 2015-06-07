package org.apache.qpid.contrib.json;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.qpid.contrib.json.utils.BZip2Utils;

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
	 * @throws Exception 
	 */
	public static void sendMessage(String queueName, Object... object)
			throws Exception {
		Configuration configuration = new PropertiesConfiguration(
				"config/rabbitmq.properties");
		boolean isCompress;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(configuration.getString("hostname"));
		factory.setUsername(configuration.getString("username"));
		factory.setPassword(configuration.getString("password"));
		isCompress = configuration.getBoolean("isCompress");
		BasicProperties basicProps = new BasicProperties.Builder()
				.deliveryMode(2).build();
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		// 声明此队列并且持久化
		channel.queueDeclare(queueName, true, false, false, null);
		Object[] messages = object;
		for (Object object2 : messages) {
			String jsonMessage = JSON.toJSONString(object2,
					SerializerFeature.WriteClassName);
			// System.out.println(jsonMessage);
			if (isCompress)
			{
			channel.basicPublish("", queueName, basicProps,
					BZip2Utils.compress(jsonMessage.getBytes()));}// 持久化消息
			else
			{
				channel.basicPublish("", queueName, basicProps,
						jsonMessage.getBytes());
			}
		}

		channel.close();
		connection.close();

	}

}
