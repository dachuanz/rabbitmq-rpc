package org.apache.qpid.contrib.json;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.qpid.contrib.json.processer.EventProcesser;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class UnitTest {
 public static void main(String[] args) throws Exception {

     Configuration configuration = new PropertiesConfiguration("config/config.properties");
	 ConnectionFactory factory = new ConnectionFactory();
     factory.setHost(configuration.getString("hostname"));
     factory.setUsername(configuration.getString("username"));
     factory.setPassword(configuration.getString("password"));
     factory.setPort(configuration.getInt("port"));
     Connection connection = null;
     connection = factory.newConnection();
     ReceiveMessageUtils utils = new ReceiveMessageUtils(connection);
     EventProcesser eventProcesser = new TestP();
     utils.receiveMessage("xxxxxxxx", eventProcesser,null);
     
}
}
