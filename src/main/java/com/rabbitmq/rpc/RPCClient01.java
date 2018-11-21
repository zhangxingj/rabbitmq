package com.rabbitmq.rpc;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient01 {
    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;
    private Consumer consumer;

    public RPCClient01() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
    }

    public String call(String message) throws IOException, InterruptedException, TimeoutException {
        
        //final String response;
        final String corrID = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties().builder()
                .correlationId(corrID).replyTo(replyQueueName).build();
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        // 阻塞队列，用于存储回调结果
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        
        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrID)) {
                	response.offer(new String(body, "UTF-8"));
                }
            }
        });
        String result = response.take();
        System.out.println(" [RpcClient] Result:'" + result + "'");
        return result;
    }

    public void close() throws Exception {
        connection.close();
    }

    public static void main(String[] args) throws Exception {
        RPCClient01 rpcClient = null;
        String response;
        try {
        	while(true){
	            rpcClient = new RPCClient01();
	            System.out.println("RPCClient  Requesting fib(20)");
	            response = rpcClient.call("20");
	            System.out.println("RPCClient  Got '" + response + "'");
	            Thread.sleep(5000);
        	}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rpcClient != null) {
                //rpcClient.close();
            }
        }
    }
}