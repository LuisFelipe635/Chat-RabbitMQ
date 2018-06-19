package br.ufs.dcomp.chat;

import com.rabbitmq.client.*;
import java.util.Scanner;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Chat {

    public static void main(String[] argv) throws Exception {
        String queueName;
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("zezinho");
        factory.setPassword("zezinho");
        factory.setHost("ec2-54-184-35-29.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        System.out.print("User: ");
        Scanner s = new Scanner (System.in);
        queueName = s.nextLine();
 
                      //(queue-name, durable, exclusive, auto-delete, params); 
        channel.queueDeclare(queueName, false,   false,     false,       null);
        
        Consumer consumer = new DefaultConsumer(channel) {
            
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           
                throws IOException {
                    
                String message = new String(body, "UTF-8");
                System.out.println();
                System.out.println(message);
            }
        };
                      //(queue-name, autoAck, consumer);    
        channel.basicConsume(queueName, true, consumer);
        
        String msg;
        String queueKey = "";
        System.out.print(">> ");
        
        while (true) {
            msg = s.nextLine();
            if (msg.startsWith("@")) {
                queueKey = msg.substring(1);
                System.out.print("@" + queueKey + ">> ");
            } 
            else {
                System.out.print("@" + queueKey + ">> ");
                DateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
                Date data = new Date();
                String dataS = dateTime.format(data);
                String send = "(" + dataS + ") " + queueName + " diz: " + msg;
                channel.basicPublish("", queueKey, null, send.getBytes("UTF-8"));
            }
        }
    }
}