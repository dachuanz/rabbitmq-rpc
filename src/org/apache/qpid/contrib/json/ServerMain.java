
package org.apache.qpid.contrib.json;

import org.apache.qpid.contrib.json.example.TestJson;
import org.apache.qpid.contrib.json.example.TestJsonIm;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class ServerMain {

    public static void main(String[] args) throws Exception {
        TestJson json = new TestJsonIm();
        System.out.println(Integer.class.getName());
        RpcServer rpcServer = RpcServer.getInstance(json) ;
       
      
        rpcServer.createRpcService(json);
    }
}
