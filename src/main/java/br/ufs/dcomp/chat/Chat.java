package br.ufs.dcomp.chat;

import com.rabbitmq.client.*;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.ByteString;
import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Chat {

    public static void main(String[] argv) throws Exception {
        String queueName;
        //String queueNameFile;
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("zezinho");
        factory.setPassword("zezinho");
        factory.setHost("ec2-34-220-179-43.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        MensagemProto.Conteudo.Builder conteudo = MensagemProto.Conteudo.newBuilder();
        MensagemProto.Mensagem.Builder mensagem = MensagemProto.Mensagem.newBuilder();
        
        System.out.print("User: ");
        Scanner s = new Scanner(System.in);
        queueName = s.nextLine();
        //queueNameFile = queueName.concat("-files");
 
                          //(queue-name,    durable,  exclusive, auto-delete, params); 
        channel.queueDeclare(queueName,     false,    false,     false,       null);
        //channel.queueDeclare(queueNameFile, false,    false,     false,       null);
        
        Consumer consumer = new DefaultConsumer(channel) {
            
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           
                throws IOException {
                    try {   
                        MensagemProto.Mensagem header = MensagemProto.Mensagem.parseFrom(body);
                        MensagemProto.Conteudo content = MensagemProto.Conteudo.parseFrom(body);
                
                        String emissor = header.getEmissor();
                        String data = header.getData();
                        String hora = header.getHora();
                        String grupo = header.getGrupo();
                        String tipoMime = content.getTipo();
                        String cont = content.getCorpo().toString();
                        String nome = content.getNome();
                
                        System.out.println();
                        
                        if (grupo.isEmpty()) 
                            System.out.println("(" + data + " às " + hora +") " +  emissor +" diz: " + cont);
                        else 
                            System.out.println("(" + data + " às " + hora +") " +  emissor + grupo +" diz: " + cont);
                    }
                    catch(IOException e){
                        System.out.println(e.getMessage());
                    }
            }
        };
                      //(queue-name, autoAck, consumer);    
        channel.basicConsume(queueName, true, consumer);
        
        String msg;
        String queueKey = "";
        String groupName = "";
        System.out.print(">> ");
        
        while (true) {
            try {
                msg = s.nextLine();
                if (msg.startsWith("@") || msg.startsWith("#")) {
                    queueKey = msg;
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!newGroup")) {
                    groupName = msg.substring(10);
                    channel.exchangeDeclare(groupName, "fanout");
                    channel.queueBind(queueName, groupName, "");
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!addUser")) {
                    String[] command = msg.split("\\s");
                    channel.queueBind(command[1], command[2], ""); // queueBind(nomeUsuario, nomeGrupo, "");
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!delFromGroup")) {
                    String[] command = msg.split("\\s");
                    channel.queueUnbind(command[1], command[2], ""); // queueUnbind(nomeUsuario, nomeGrupo, "");
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!removeGroup")) {
                    groupName = msg.substring(13);
                    channel.exchangeDelete(groupName);
                    System.out.print(queueKey + ">> ");
                }
                if (!msg.substring(0,1).matches("\\p{Punct}")) {
                    if (queueKey.startsWith("@")) {
                        DateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date data = new Date();
                        String dataS = dateTime.format(data);
                        String[] splitData = dataS.split("\\s");
                    
                        conteudo.setCorpo(ByteString.copyFromUtf8(msg));
                        mensagem.setEmissor(queueName);
                        mensagem.setData(splitData[0]);
                        mensagem.setHora(splitData[1]);
                    
                        MensagemProto.Mensagem wrapper = mensagem.build();
                        byte[] buffer = wrapper.toByteArray();
                    
                        channel.basicPublish("", queueKey.substring(1), null, buffer);
                        System.out.print(queueKey + ">> ");
                    }
                    if (queueKey.startsWith("#")) {
                        DateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date data = new Date();
                        String dataS = dateTime.format(data);
                        String[] splitData = dataS.split("\\s");
                    
                        conteudo.setCorpo(ByteString.copyFromUtf8(msg));
                        mensagem.setEmissor(queueName);
                        mensagem.setData(splitData[0]);
                        mensagem.setHora(splitData[1]);
                        mensagem.setGrupo(queueKey);
                    
                        MensagemProto.Mensagem wrapper = mensagem.build();
                        byte[] buffer = wrapper.toByteArray();
                    
                        channel.basicPublish(queueKey.substring(1),"", null, buffer);
                        System.out.print(queueKey + ">> ");
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Algo deu errado, tente novamente.");
                if (queueKey.isEmpty())
                    System.out.print(">> ");
                else
                    System.out.print(queueKey + ">> ");
            }
        }
    }
}