package com.rabbitmq.rpc;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient {
    private static final String RPC_QUEUE_NAME = "rpc_queue";

    public static void main(String[] arg){
    	RPCClient.execute("localhost", "guest", "guest", "hello rabbit rpc!");
    }
    
    public static void execute(String host, String userName, String password, String message){
        // �������ӹ���
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        // ��Ҫ�ڹ����̨����һ��hry�ʺ�
        factory.setUsername(userName);
        factory.setPassword(password);

        Connection connection = null;
        Channel channel = null;
        try {
            // ����TCP����
            connection = factory.newConnection();
            // ��TCP���ӵĻ����ϴ���ͨ��
            channel = connection.createChannel();
            // ������ʱ���У����������ɵĶ�������
            String replyQueueName = channel.queueDeclare().getQueue();

            // Ψһ��־��������
            final String corrId = UUID.randomUUID().toString();
            // ���ɷ�����Ϣ������
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId) // Ψһ��־��������
                    .replyTo(replyQueueName) // ���ûص�����
                    .build();
            // ������Ϣ�����͵�Ĭ�Ͻ�����
            channel.basicPublish("", RPC_QUEUE_NAME, props, message.getBytes("UTF-8"));
            System.out.println(" [RpcClient] Requesting : " + message);

            // �������У����ڴ洢�ص����
            final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
            // ������Ϣ�Ļ��˷���
            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });
            // ��ȡ�ص��Ľ��
            String result = response.take();
            System.out.println(" [RpcClient] Result:'" + result + "'");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                // ��ֵ�жϣ�Ϊ�˴�������
                channel.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
