
package org.apache.qpid.contrib.json;

import org.apache.qpid.contrib.json.example.TestJson;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class RPCMain {

    public static void main(String[] args) throws Exception {
        RPCClient rpcClient = new RPCClient();
        System.out.println(" ");
        TestJson json = rpcClient.createRpcClient(org.apache.qpid.contrib.json.example.TestJson.class);

        System.out.println(" [.] Got '" + json.getJson(15) + "'");
        rpcClient.close();
    }
}
