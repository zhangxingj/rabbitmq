package com.rabbitmq.rpc;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {
    private static final String RPC_QUEUE_NAME = "rpc_queue";

    public static void main(String[] arg){
    	RPCServer.execute("localhost", "guest", "guest");
    }
    
    public static void execute(String host, String userName, String password){
        // �������ӹ���
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        // ��Ҫ�ڹ����̨����һ��hry�ʺ�
        //factory.setUsername(userName);
        //factory.setPassword(password);

        Connection connection = null;
        try {
            // ����TCP����
            connection = factory.newConnection();
            // ��TCP���ӵĻ����ϴ���ͨ��
            final Channel channel = connection.createChannel();
            // ����һ��rpc_queue����
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            // ����ͬʱ���ֻ�ܻ�ȡһ����Ϣ
            channel.basicQos(1);
            System.out.println(" [RpcServer] Awaiting RPC requests");
            // ������Ϣ�Ļص�������
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    // ���ɷ��صĽ�����ؼ�������correlationIdֵ
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    // ���ɷ���
                    String response = generateResponse(body);
                    // �ظ���Ϣ��֪ͨ�Ѿ��յ�����
                    channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    // ����Ϣ����Ӧ��
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    // �����������������е��߳�
                    synchronized(this) {
                        this.notify();
                    }
                }
            };
            // ������Ϣ
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
            // ���յ���Ϣǰ�����߳̽���ȴ�״̬
            while (true) {
                synchronized(consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                // ��ֵ�жϣ�Ϊ�˴�������
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ��ͣ10s�������ؽ��
     * @param body
     * @return
     */
    private static String generateResponse(byte[] body) {
        System.out.println(" [RpcServer] receive requests: " + new String(body));
        try {
            Thread.sleep(1000 *1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "response:" + new String(body) + "-" + System.currentTimeMillis();
    }
}
