package com.rabbitmq.spring;



import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQProducerImpl implements MQProducer {
    @Autowired
    private AmqpTemplate amqpTemplate;

    
    public void sendDataToQueue(String queueKey, Object object){
    	try{
    		amqpTemplate.convertAndSend(queueKey, object);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}