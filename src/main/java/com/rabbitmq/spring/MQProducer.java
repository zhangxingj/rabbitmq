package com.rabbitmq.spring;

//MQProducer.java
public interface MQProducer {
	/**
	 * ������Ϣ��ָ������
	 * 
	 * @param queueKey
	 * @param object
	 */
	public void sendDataToQueue(String queueKey, Object object);
}