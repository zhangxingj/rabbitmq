package com.rabbitmq.rpc;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RPCServer01 {
    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 1);
    }

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.basicQos(1);
        
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // ���ɷ��صĽ�����ؼ�������correlationIdֵ
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();
                // ���ɷ���
                String message = new String(body, "UTF-8");
                int n = Integer.parseInt(message);

                System.out.println("RPCServer fib(" + message + ")");
                String response = "" + fib(n);
                // �ظ���Ϣ��֪ͨ�Ѿ��յ�����
                channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                // ����Ϣ����Ӧ��
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        System.out.println("RPCServer Awating RPC request");
        
    }
}
