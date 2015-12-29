package org.apache.qpid.contrib.json;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.qpid.contrib.json.processer.EventProcesser;
import org.apache.qpid.contrib.json.utils.BZip2Utils;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author zdc
 * @since 2015年5月28日
 */
public class ReceiveMessageUtils {
	final static Logger logger = Logger.getLogger(ReceiveMessageUtils.class);
	private Channel channel;

	private Connection connection;
	boolean isCompress=false;

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
	public void receiveMessage(String queueName, EventProcesser eventProcesser,
			Class<?> clazz) throws Exception {

		Configuration configuration = new PropertiesConfiguration(
				"config/rabbitmq.properties");
		
		if (configuration.containsKey("isCompress"))
		{
			this.isCompress = configuration.getBoolean("isCompress");
		} 
		channel = connection.createChannel();
		channel.queueDeclare(queueName, true, false, false, null);
		// System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		channel.basicQos(1);// 告诉RabbitMQ同一时间给一个消息给消费者
		/*
		 * We're about to tell the server to deliver us the messages from the
		 * queue. * Since it will push us messages asynchronously, * we provide
		 * a callback in the form of an object that will buffer the messages *
		 * until we're ready to use them. That is what QueueingConsumer does.
		 */
		QueueingConsumer consumer = new QueueingConsumer(channel);
		/*
		 * 名字为TASK_QUEUE_NAME的Channel的值回调给QueueingConsumer,即使一个worker在处理消息的过程中停止了
		 * ，这个消息也不会失效
		 */
		channel.basicConsume(queueName, false, consumer);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();// 得到消息传输信息
			byte[] s = null;
			if (isCompress) {
				// System.out.println("压缩前长度" + message.getBytes().length);

				s = BZip2Utils.decompress(delivery.getBody());

				// System.out.println("压缩后长度" + s.length);
			} else {
				s = delivery.getBody();
			}
			String message = new String(s);
 
			logger.debug("[Received]" + message + "'");
			if (clazz != null) {
				eventProcesser.process(JSON.parseObject(message,clazz));
			} else {
				eventProcesser.process(JSON.parse(message));
			}

			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);// 下一个消息
		}

	}

	public ReceiveMessageUtils(Connection connection) {
		super();
		this.connection = connection;
	}

	public void close() throws Exception {

		connection.close();
	}
}