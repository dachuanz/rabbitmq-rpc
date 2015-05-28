// Copyright (c) 2000-2009 GloryScience. All Rights Reserved.
// This software is the confidential and proprietary information of GloryScience
// Original author: zdc
// -------------------------------------------------------------------------
// GloryScience MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
// THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
// TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
// PARTICULAR PURPOSE, OR NON-INFRINGEMENT. Glory Science SHALL NOT BE
// LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
// MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
//
// THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
// CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
// PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
// NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
// SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
// SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
// PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). GloryScience
// SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
// HIGH RISK ACTIVITIES.
// -------------------------------------------------------------------------
package org.apache.qpid.contrib.json;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.qpid.contrib.json.processer.EventProcesser;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author zdc
 * @since 2015年5月28日
 */
public class ReceiveMessageUtils {

    private Channel channel;

    private Connection connection;
/**
 * 
 * @param queueName 队列名称
 * @param eventProcesser 事件名称
 * @param clazz 要接收的消息类型
 * @throws IOException
 * @throws ConfigurationException
 * @throws ShutdownSignalException
 * @throws ConsumerCancelledException
 * @throws InterruptedException
 */
    public void receiveMessage(String queueName, EventProcesser eventProcesser, Class<?> clazz) throws IOException, ConfigurationException,
            ShutdownSignalException, ConsumerCancelledException, InterruptedException {

        Configuration configuration = new PropertiesConfiguration("config/rabbitmq.properties");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configuration.getString("hostname"));
        factory.setUsername(configuration.getString("username"));
        factory.setPassword(configuration.getString("password"));
        factory.setPort(AMQP.PROTOCOL.PORT);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);// 告诉RabbitMQ同一时间给一个消息给消费者
        /*
         * We're about to tell the server to deliver us the messages from the queue. 21. * Since it will push us
         * messages asynchronously, 22. * we provide a callback in the form of an object that will buffer the messages
         *  * until we're ready to use them. That is what QueueingConsumer does.
         */
        QueueingConsumer consumer = new QueueingConsumer(channel);
        /*
         * 名字为TASK_QUEUE_NAME的Channel的值回调给QueueingConsumer,即使一个worker在处理消息的过程中停止了，这个消息也不会失效 27.
         */
        channel.basicConsume(queueName, false, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();// 得到消息传输信息
            String message = new String(delivery.getBody());

            System.out.println(" [x] Received '" + message + "'");
            if (clazz != null) {
                eventProcesser.process(JSON.parseObject(message, clazz));
            } else {
                eventProcesser.process(JSON.parse(message));
            }
            System.out.println(" [x] Done");

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);// 下一个消息
        }

        // retur;

    }

    public void close() throws Exception {
        // channel.abort();
        // channel.close();
        connection.close();
    }
}
