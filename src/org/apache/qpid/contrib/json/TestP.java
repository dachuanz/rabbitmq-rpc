package org.apache.qpid.contrib.json;

import org.apache.qpid.contrib.json.processer.EventProcesser;

import com.gloryscience.ocn3.collector.bean.modal.PerfHS;
import com.gloryscience.ocn3.collector.bean.modal.PerfPon;
import com.gloryscience.ocn3.collector.bean.modal.PerfSni;

public class TestP implements EventProcesser {

	@Override
	public void process(Object e) {
		if (e instanceof PerfPon) {
           System.out.println("PON");
        } else if (e instanceof PerfSni) {
          
        } else if ( e instanceof PerfHS) {
            
        }

		
	}

}
