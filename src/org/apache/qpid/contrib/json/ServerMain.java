package org.apache.qpid.contrib.json;

import org.apache.log4j.PropertyConfigurator;
import org.apache.qpid.contrib.json.example.TestJson;
import org.apache.qpid.contrib.json.example.TestJsonIm;

import com.rabbitmq.client.ConnectionFactory;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class ServerMain {
	static {
        PropertyConfigurator.configure("config/log4j.properties");
    }
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.10.6");
		factory.setUsername("hfc");
		factory.setPassword("hfc");
		factory.setPort(5672);
		factory.setAutomaticRecoveryEnabled(true);
		System.out.println("xxxxxxxxxxxx");
		TestJson deviceStatusService = new TestJsonIm();
		RPCServer.getInstance(deviceStatusService, factory.newConnection());

	}
}
