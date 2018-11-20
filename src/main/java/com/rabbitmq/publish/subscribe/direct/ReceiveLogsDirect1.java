package com.rabbitmq.publish.subscribe.direct;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ReceiveLogsDirect1 {
    // ����������
    private static final String EXCHANGE_NAME = "direct_logs";
    // ·�ɹؼ���
    private static final String[] routingKeys = new String[]{"info" ,"warning"};

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //����������
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        //��ȡ������������
        String queueName=channel.queueDeclare().getQueue();

        //����·�ɹؼ��ֽ��а�
        for (String routingKey:routingKeys){
            channel.queueBind(queueName,EXCHANGE_NAME,routingKey);
            System.out.println("ReceiveLogsDirect1 exchange:"+EXCHANGE_NAME+"," +
                    " queue:"+queueName+", BindRoutingKey:" + routingKey);
        }
        System.out.println("ReceiveLogsDirect1  Waiting for messages");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("ReceiveLogsDirect1 Received '" + envelope.getRoutingKey() + "':'" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
