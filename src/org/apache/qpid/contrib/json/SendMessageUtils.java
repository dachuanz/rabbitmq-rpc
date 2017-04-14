package org.apache.qpid.contrib.json;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.qpid.contrib.json.utils.BZip2Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * 
 * @author 张大川
 *
 */
public class SendMessageUtils {

	/**
	 * 
	 * 
	 * @param queueName
	 *            队列名称
	 * @param object
	 *            要传送的对象
	 * @throws Exception
	 */
	public static void sendMessage(String queueName, Object... object) throws Exception {
		Configuration config = null;
		boolean isCompress = false;
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class).configure(params.properties().setFileName("rabbitmq.properties"));
		try {
			config = builder.getConfiguration();

		} catch (ConfigurationException cex) {
			cex.printStackTrace();
		}
		ConnectionFactory factory = new ConnectionFactory();
		// System.out.println();
		factory.setHost(config.getString("hostname"));
		factory.setUsername(config.getString("username"));
		factory.setPassword(config.getString("password"));
		factory.useNio();
		if (config.containsKey("isCompress")) {
			isCompress = config.getBoolean("isCompress");
		}
		if (config.containsKey("port")) {
			factory.setPort(config.getInt("port"));
		} else {
			factory.setPort(AMQP.PROTOCOL.PORT);
		}

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queueName, true, false, false, null);

		String jsonMessage;
		Object[] messages = object;
		for (Object object2 : messages) {
			jsonMessage = JSON.toJSONString(object2, SerializerFeature.WriteClassName);

			if (isCompress) {
				channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN,
						BZip2Utils.compress(jsonMessage.getBytes()));
			} // 持久化消息

			else {
				channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, jsonMessage.getBytes());
			}
		}

		channel.close();
		connection.close();

	}

}
