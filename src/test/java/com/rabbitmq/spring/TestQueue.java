package com.rabbitmq.spring;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rabbitmq.spring.MQProducer;

//TestQueue.java
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-mq.xml"})
public class TestQueue{
  @Autowired
  MQProducer mqProducer;

  final String queue_key = "test_queue_key";

  @Test
  public void send(){
	  System.out.println(1);
      Map<String,Object> msg = new HashMap<String,Object>();
      msg.put("data","hello,rabbmitmq!");
      mqProducer.sendDataToQueue(queue_key,msg);
  }
}