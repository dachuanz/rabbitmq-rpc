package org.apache.qpid.contrib.json.processer;

public interface EventProcesser<T> {

    void process(T e) ;
    
    void  next(T t);
    boolean end();

	
}