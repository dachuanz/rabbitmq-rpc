package org.apache.qpid.contrib.json;

import org.apache.qpid.contrib.json.processer.EventProcesser;

import cache.cache.Pojo;

/**
 * 
 * @author 测试
 *
 */
public class TestProcesser implements EventProcesser<Pojo> {

	@Override
	public void process(Pojo e) {
		System.out.println("被调用内容" + e.getTaskName());

	}

	@Override
	public void next(Pojo t) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean end() {
		// TODO Auto-generated method stub
		return true;
	}

}
