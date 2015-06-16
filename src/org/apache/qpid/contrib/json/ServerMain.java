
package org.apache.qpid.contrib.json;

import org.apache.log4j.PropertyConfigurator;
import org.apache.qpid.contrib.json.example.TestJson;
import org.apache.qpid.contrib.json.example.TestJsonIm;

import com.gloryscience.ocn3.collector.bean.modal.PerfPon;

/**
 * @author zdc
 * @since 2015年5月27日
 */
public class ServerMain {
	public static void main(String[] args) throws Exception {
		PerfPon object = new PerfPon();
		object.setId(3333333333333L);
		
		SendMessageUtils.sendMessage("xxxxxxxx", object);
	}
}
