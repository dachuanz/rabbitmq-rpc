package org.apache.qpid.contrib.json;

import java.io.IOException;

import org.apache.qpid.contrib.json.processer.EventProcesser;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 
 * 接受消息方法
 * 
 * @author 张大川
 *
 */
public class ReceiveMessageUtils {

	private Channel channel;

	private Connection connection;

	/**
	 * 
	 * @param <T>
	 * @param queueName
	 *            队列名称
	 * @param eventProcesser
	 *            处理事件实例
	 * @param clazz
	 *            要接收的消息类型
	 * @throws Exception
	 */
	public <T> void receiveMessage(String queueName, EventProcesser<T> eventProcesser, Class<T> clazz)
			throws Exception {

		channel = connection.createChannel();
		channel.queueDeclare(queueName, true, false, false, null);

		channel.basicQos(1);// 告诉RabbitMQ同一时间给一个消息给消费者

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {

				String message = new String(body);
				boolean b = false;
				if (clazz != null) {
					eventProcesser.process((T) JSON.parseObject(message, clazz));

				} else {
					Object parse = JSON.parse(message);
					eventProcesser.process((T) parse);
				}
				b = eventProcesser.end();
				if (b) {
					channel.basicAck(envelope.getDeliveryTag(), false);
				} else {
					channel.basicNack(envelope.getDeliveryTag(), false, true);
				}
			}
		};

		channel.basicConsume(queueName, consumer);

	}

	public ReceiveMessageUtils(Connection connection) {
		super();
		this.connection = connection;
	}

	public void close() throws Exception {

		connection.close();
	}

}