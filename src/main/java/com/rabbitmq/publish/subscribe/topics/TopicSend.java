package com.rabbitmq.publish.subscribe.topics;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TopicSend {
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try{
            ConnectionFactory factory=new ConnectionFactory();
            factory.setHost("localhost");
            connection=factory.newConnection();
            channel=connection.createChannel();

            //����һ��ƥ��ģʽ�Ľ�����
            channel.exchangeDeclare(EXCHANGE_NAME,"topic");
            //�����͵���Ϣ
            String[] routingKeys=new String[]{
                    "quick.orange.rabbit",
                    "lazy.orange.elephant",
                    "quick.orange.fox",
                    "lazy.brown.fox",
                    "quick.brown.fox",
                    "quick.orange.male.rabbit",
                    "lazy.orange.male.rabbit"
            };
            //������Ϣ
            for(String severity :routingKeys){
                String message = "From "+severity+" routingKey' s message!";
                channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
                System.out.println("TopicSend Sent '" + severity + "':'" + message + "'");
            }
        }catch (Exception e){
            e.printStackTrace();
            if (connection!=null){
                channel.close();
                connection.close();
            }
        }finally {
            if (connection!=null){
                channel.close();
                connection.close();
            }
        }
    }
}
