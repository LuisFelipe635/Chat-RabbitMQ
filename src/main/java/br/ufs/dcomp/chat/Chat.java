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
        //String queueNameFile;
        
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
        //queueNameFile = queueName.concat("-files");
 
                          //(queue-name,    durable,  exclusive, auto-delete, params); 
        channel.queueDeclare(queueName,     false,    false,     false,       null);
        //channel.queueDeclare(queueNameFile, false,    false,     false,       null);
        
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
        String promptControl = "";
        System.out.print(">> ");
        
        while (true) {
            msg = s.nextLine();
            if (msg.startsWith("@")) {
                queueKey = msg.substring(1);
                promptControl = msg;
                System.out.print("@" + queueKey + ">> ");
            }
            if (msg.startsWith("#")) {
                queueKey = msg.substring(1);
                promptControl = msg;
                System.out.print("#" + queueKey + ">> ");
            }
            if (msg.startsWith("!new")) {
                queueKey = msg.substring(10);
                channel.exchangeDeclare(queueKey, "fanout");
                channel.queueBind(queueName, queueKey, "");
                System.out.print(promptControl + ">> ");
            }
            if (msg.startsWith("!add")) {
                String[] command = msg.split("\\s");
                queueKey = command[2];
                channel.queueBind(command[1], queueKey, "");
                System.out.print(promptControl + ">> ");
            }
            if (msg.startsWith("!del")) {
                String[] command = msg.split("\\s");
                queueKey = command[2];
                channel.queueUnbind(command[1], queueKey, "");
                System.out.print(promptControl + ">> ");
            }
            if (msg.startsWith("!rem")) {
                queueKey = msg.substring(13);
                channel.exchangeDelete(queueKey);
                System.out.print(promptControl + ">> ");
            }
            if (!msg.startsWith("!")) {
                if (promptControl.startsWith("@")) {
                    System.out.print("@" + queueKey + ">> ");
                    DateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
                    Date data = new Date();
                    String dataS = dateTime.format(data);
                    String send = "(" + dataS + ") " + queueName + " diz: " + msg;
                    channel.basicPublish("", queueKey, null, send.getBytes("UTF-8"));
                }
                else {
                    System.out.print("#" + queueKey + ">> ");
                    DateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy à HH:mm");
                    Date data = new Date();
                    String dataS = dateTime.format(data);
                    String send = "(" + dataS + ") " + queueName + "#" + queueKey + " diz: " + msg;
                    channel.basicPublish(queueKey, "", null, send.getBytes("UTF-8"));
                }
            }
        }
    }
}