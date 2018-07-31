package br.ufs.dcomp.chat;

import com.rabbitmq.client.*;
import com.google.protobuf.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Chat {

    public static void main(String[] argv) throws Exception {
        String queueName;
        String queueNameFile;
         
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("zezinho");
        factory.setPassword("zezinho");
        factory.setHost("ec2-34-220-179-43.us-west-2.compute.amazonaws.com");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Channel channelFile = connection.createChannel();
        
        MensagemProto.Mensagem.Builder mensagem = MensagemProto.Mensagem.newBuilder();
        
        System.out.print("User: ");
        Scanner s = new Scanner(System.in);
        queueName = s.nextLine();
        queueNameFile = queueName.concat("-files");
 
                          //(queue-name,    durable,  exclusive, auto-delete, params); 
        channel.queueDeclare(queueName,     false,    false,     false,       null);
        channelFile.queueDeclare(queueNameFile, false,    false,     false,       null);
        
        Consumer consumer = new DefaultConsumer(channel) {
            
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           
                throws IOException {
                    try {   
                        MensagemProto.Mensagem message = MensagemProto.Mensagem.parseFrom(body);
                        
                        String emissor = message.getEmissor();
                        String data = message.getData();
                        String hora = message.getHora();
                        String grupo = message.getGrupo();
                        String cont = message.getCorpo().toStringUtf8();
                        
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
        
        Consumer consumerFile = new DefaultConsumer(channelFile) {
            
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           
                throws IOException {
                    try {   
                        MensagemProto.Mensagem message = MensagemProto.Mensagem.parseFrom(body);
                
                        String emissor = message.getEmissor();
                        String data = message.getData();
                        String hora = message.getHora();
                        String grupo = message.getGrupo();
                        String tipoMime = message.getTipo();
                        String nome = message.getNome();
                        byte[] conteudo = message.getCorpo().toByteArray();
                        
                        File dir = new File("chat/downloads/");
                        dir.mkdirs();
                        
                        File file = new File(dir, nome);
                        FileOutputStream saida = new FileOutputStream(file);
                        BufferedOutputStream out = new BufferedOutputStream(saida);
                        out.write(conteudo);
                        out.flush();
                        out.close();
                
                        System.out.println("(" + data + " às " + hora +") Arquivo \"" + nome + "\" recebido de " + emissor);
                    }
                    catch(IOException e){
                        System.out.println(e.getMessage());
                    }
            }
        };
                      //(queue-name, autoAck, consumer);    
        channel.basicConsume(queueName, true, consumer);
        channelFile.basicConsume(queueNameFile, true, consumerFile);
        
        DateFormat dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String msg;
        String queueKey = "";
        String groupName = "";
        String groupNameFile = "";
        System.out.print(">> ");
        
        while (true) {
            try {
                msg = s.nextLine();
                if (msg.startsWith("@") || msg.startsWith("#")) {
                    queueKey = msg;
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!new")) {
                    groupName = msg.substring(10);
                    groupNameFile = groupName.concat("-files");
                    
                    channel.exchangeDeclare(groupName, "fanout");
                    channel.queueBind(queueName, groupName, "");
                    
                    channelFile.exchangeDeclare(groupNameFile, "fanout");
                    channelFile.queueBind(queueNameFile, groupNameFile, "");
                    
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!add")) {
                    String[] command = msg.split("\\s");
                    
                    channel.queueBind(command[1], command[2], ""); // queueBind(nomeUsuario, nomeGrupo, "");
                    channelFile.queueBind(command[1].concat("-files"), command[2].concat("-files"), "");
                    
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!del")) {
                    String[] command = msg.split("\\s");
                    
                    channel.queueUnbind(command[1], command[2], ""); // queueUnbind(nomeUsuario, nomeGrupo, "");
                    channelFile.queueUnbind(command[1].concat("-files"), command[2].concat("-files"), "");
                    
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!rem")) {
                    groupName = msg.substring(13);
                    groupNameFile = groupName.concat("-files");
                    
                    channel.exchangeDelete(groupName);
                    channelFile.exchangeDelete(groupNameFile);
                    
                    System.out.print(queueKey + ">> ");
                }
                if (msg.startsWith("!up")) {
                    String daWaeSlash = msg.substring(8);
                    String daWae = "";
                    if(daWaeSlash.startsWith("/"))
                        daWae = daWaeSlash.substring(1);
                    else
                        daWae = daWaeSlash;
                    //Path source = Paths.get(daWae);
                    //String tipoMime = Files.probeContentType(source);
                    
                    Date data = new Date();
                    String dataS = dateTime.format(data);
                    String[] splitData = dataS.split("\\s");
                    
                    System.out.println("Enviando \"" + daWae + "\" para " + queueKey + "...");
                    System.out.print(queueKey + ">> ");
                    Envio envio = new Envio(queueName, splitData[0], splitData[1], queueKey, daWae);
                    envio.main(new String[]{});
                }
                if (!msg.substring(0,1).matches("\\p{Punct}")) {
                    if (queueKey.startsWith("@")) {
                        Date data = new Date();
                        String dataS = dateTime.format(data);
                        String[] splitData = dataS.split("\\s");
                    
                        mensagem.setEmissor(queueName);
                        mensagem.setData(splitData[0]);
                        mensagem.setHora(splitData[1]);
                        mensagem.setTipo("text/plain");
                        mensagem.setCorpo(ByteString.copyFromUtf8(msg));
                    
                        MensagemProto.Mensagem wrapper = mensagem.build();
                        byte[] buffer = wrapper.toByteArray();
                    
                        channel.basicPublish("", queueKey.substring(1), null, buffer);
                        System.out.print(queueKey + ">> ");
                    }
                    if (queueKey.startsWith("#")) {
                        Date data = new Date();
                        String dataS = dateTime.format(data);
                        String[] splitData = dataS.split("\\s");
                    
                        mensagem.setEmissor(queueName);
                        mensagem.setData(splitData[0]);
                        mensagem.setHora(splitData[1]);
                        mensagem.setGrupo(queueKey);
                        mensagem.setTipo("text/plain");
                        mensagem.setCorpo(ByteString.copyFromUtf8(msg));
                    
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