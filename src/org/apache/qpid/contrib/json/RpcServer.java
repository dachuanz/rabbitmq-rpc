

package org.apache.qpid.contrib.json;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class RpcServer {

    private static RpcServer instance;

    public synchronized static RpcServer getInstance(Object obj) throws Exception {
        if (null == instance) {

            instance = new RpcServer(obj);

        }
        return instance;
    }

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private Class<?> serviceAPI;

    private Object serviceImpl;

    public void createRpcService(Object obj) {
        this.serviceImpl = obj;
        this.serviceAPI = obj.getClass();
    }

    public static void main(String[] args) {

    }

    private static final Map<String, Class<?>> primitiveClazz; // 基本类型的class

    private static final String BYTE = "byte";

    private static final String CHARACTOR = "char";

    private static final String SHORT = "short";

    private static final String LONG = "long";

    private static final String FLOAT = "float";

    private static final String DOUBLE = "double";

    private static final String BOOLEAN = "boolean";

    private static final String INT = "int";

    static {

        primitiveClazz = new HashMap<String, Class<?>>();
        primitiveClazz.put(INT, int.class);
        primitiveClazz.put(BYTE, byte.class);
        primitiveClazz.put(CHARACTOR, char.class);
        primitiveClazz.put(SHORT, short.class);
        primitiveClazz.put(LONG, long.class);
        primitiveClazz.put(FLOAT, float.class);
        primitiveClazz.put(DOUBLE, double.class);
        primitiveClazz.put(BOOLEAN, boolean.class);

    }

    public RpcServer(Object obj) throws Exception {
        // • 先建立连接、通道，并声明队列
        Configuration configuration = new PropertiesConfiguration("config/rabbitmq.properties");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configuration.getString("hostname"));
        factory.setUsername(configuration.getString("username"));
        factory.setPassword(configuration.getString("password"));
        factory.setPort(AMQP.PROTOCOL.PORT);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        // •可以运行多个服务器进程。通过channel.basicQos设置prefetchCount属性可将负载平均分配到多台服务器上。
        channel.basicQos(1);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        // 打开应答机制=false
        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);// 第二个参数，自动确认设置为true,即使rpc失败，也能略过这个请求。
        System.out.println("Awaiting RPC requests " + new Date());
        while (true) {

            Delivery delivery = consumer.nextDelivery();
            BasicProperties props = delivery.getProperties();
            BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId()).contentType("application/json").deliveryMode(2)
                    .build();
            // replyProps.setContentType(contentType);
            String message = new String(delivery.getBody());
            @SuppressWarnings("rawtypes")
            Map map = (Map) JSON.parse(message);
            System.out.println("收到消息 " + new Date() + "~~~~" + message);
            String methodName = map.get("method") + "";// service是服务器端提供服务的对象，但是，要通过获取到的调用方法的名称，参数类型，以及参数来选择对象的方法，并调用。获得方法的名称
            List parameterTypes = (List) map.get("parameterTypes");// 获得参数的类型
            List arguments = (List) map.get("args");// 获得参数
            Class[] classes = new Class[parameterTypes.size()];
            for (int j = 0; j < parameterTypes.size(); j++) {
                if (primitiveClazz.get(parameterTypes.get(j) + "") != null) {

                    classes[j] = primitiveClazz.get(parameterTypes.get(j) + "");// 处理基本类型
                } else {
                    classes[j] = Class.forName(parameterTypes.get(j) + "");
                }
            }

            Method method = obj.getClass().getMethod(methodName, classes);// 通过反射机制获得方法
            Object result = null;
            if (arguments != null) {
                result = method.invoke(obj, arguments.toArray());
            } else {
                result = method.invoke(obj);
            }
          // Class class1 = Class.forName(obj.getClass().getName());
           String response = JSON.toJSONString(result,SerializerFeature.WriteClassName);// 使用fastjson序列化
            // 返回处理结果队列
            channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());
            // 发送应答
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

        }

    }

    public Class<?> getServiceAPI() {
        return serviceAPI;
    }

    public void setServiceAPI(Class<?> serviceAPI) {
        this.serviceAPI = serviceAPI;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }
}
