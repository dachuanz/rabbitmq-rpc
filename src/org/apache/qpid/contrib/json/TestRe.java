package org.apache.qpid.contrib.json;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.qpid.contrib.json.processer.EventProcesser;

import com.rabbitmq.client.ConnectionFactory;

import cache.cache.Pojo;

public class TestRe {
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		// System.out.println();
		factory.setHost("120.92.73.218");
		factory.setUsername("root");
		factory.setPassword("harry17108319");
		factory.useNio();
	com.rabbitmq.client.Connection connection=	factory.newConnection();
	ReceiveMessageUtils messageUtils = new ReceiveMessageUtils(connection);
	EventProcesser<Pojo> p = new TestProcesser();
	messageUtils.receiveMessage("test", p, Pojo.class);
	}
	
}
