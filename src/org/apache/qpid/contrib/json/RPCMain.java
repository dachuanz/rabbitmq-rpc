
package org.apache.qpid.contrib.json;

import org.apache.log4j.PropertyConfigurator;
import org.apache.qpid.contrib.json.example.TestJson;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class RPCMain {
	static {
        PropertyConfigurator.configure("config/log4j.properties");
    }
    public static void main(String[] args) throws Exception {
        RPCClient rpcClient = new RPCClient();
        System.out.println(" ");
        TestJson json = rpcClient.createRpcClient(org.apache.qpid.contrib.json.example.TestJson.class);
        System.out.println("x222");
        System.out.println(" [.] Got '" + json.gettest(4,0) + "'");
        rpcClient.close();
    }
}
