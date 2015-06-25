package org.apache.qpid.contrib.serialization;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.qpid.contrib.json.SendMessageUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
/**
 * 
 * @author zdc
 *
 */
public class TestSerialization {
	public static void main(String[] args) throws Exception {
		Map test = new HashMap();
		int max = 30;
		int min = 0;
		Random random = new Random();

		int s = random.nextInt(max) % (max - min + 1) + min;
		System.out.println(s);
		for (int i = 0; i < max; i++) {
			Object object = new Object();
			test.put(i, object);

		}
		System.out.println(new Date());
		String string = JSON.toJSONString(test,
				SerializerFeature.WriteClassName);
		System.out.println(string);
		Map hashtable = JSON.parseObject(string, java.util.Map.class);
		SendMessageUtils.sendMessage("xxx", test);
		System.out.println(hashtable.get(s));
		System.out.println(new Date());
	}
}
