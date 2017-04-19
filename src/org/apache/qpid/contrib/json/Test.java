package org.apache.qpid.contrib.json;

import cache.cache.Pojo;

public class Test {
	public static void main(String[] args) throws Exception {
		Pojo p = new Pojo();
		p.setTaskName("xxx");
		SendMessageUtils.sendMessage("test", p);
	}
}
