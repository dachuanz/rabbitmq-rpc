
package org.apache.qpid.contrib.json;

import java.lang.reflect.Proxy;

import org.apache.qpid.contrib.json.example.TestJson;
import org.apache.qpid.contrib.json.example.TestJsonIm;


/**
 * @author zdc
 * @since 2015年5月27日
 */
public class ProxyTest {
public static void main(String[] args) {
    TestJson json = new TestJsonIm();
    AMQPJsonProxy amqpJsonProxy= new AMQPJsonProxy(json);
    TestJson proxy = ( TestJson)Proxy.newProxyInstance(TestJson.class.getClassLoader(),  
            new Class[]{TestJson.class}, amqpJsonProxy);  
   
}
}
