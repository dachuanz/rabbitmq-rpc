package org.apache.qpid.contrib.json;

import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.qpid.contrib.json.processer.EventProcesser;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ReceiveMessageUtils {

	private Channel channel;

	private Connection connection;
	boolean isCompress = false;

	

	/**
	 * 
	 * @param queueName
	 *            队列名称
	 * @param eventProcesser
	 *            处理事件实例
	 * @param clazz
	 *            要接收的消息类型
	 * @throws Exception
	 */
	public void receiveMessage(String queueName, EventProcesser eventProcesser, Class<?> clazz) throws Exception {
		setConfig(null);
		
		

		
		channel = connection.createChannel();
		channel.queueDeclare(queueName, true, false, false, null);
		// System.out.println(" [*] Waiting for messages. To exit press
		// CTRL+C");

		channel.basicQos(1);// 告诉RabbitMQ同一时间给一个消息给消费者
		
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
			
			

				String message = new String(body);
				
				if (clazz != null) {
					eventProcesser.process(JSON.parseObject(message, clazz));
				} else {
					eventProcesser.process(JSON.parse(message));
				}

			}
		};

		channel.basicConsume(queueName, true, consumer);

	}

	public ReceiveMessageUtils(Connection connection) {
		super();
		this.connection = connection;
	}

	public void close() throws Exception {

		connection.close();
	}

	
}
