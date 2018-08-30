package br.ufs.dcomp.chat;

import com.rabbitmq.client.*;
import com.google.protobuf.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Envio {
    private static String emissor;
    private static String data;
    private static String hora;
    private static String chave;
    //private static String tipoMime;
    private static String daWae;
    
    public Envio(String emissor, String data, String hora, String chave, String wae) {
        this.emissor = emissor;
        this.data = data;
        this.hora = hora;
        this.chave = chave;
        //this.tipoMime = tipo;
        this.daWae = wae;
    }
    
    public static String getEmissor() {
        return emissor;
    }
    
    public static String getData() {
        return data;
    }
    
    public static String getHora() {
        return hora;
    }
    
    public static String getChave() {
        return chave;
    }
    
    //public static String getTipoMime() {
    //    return tipoMime;
    //}
    
    public static String getWae() {
        return daWae;
    }
    
    private static Runnable envio = new Runnable() {
        @Override
        public void run() {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername("zezinho");
            factory.setPassword("01001100");
            factory.setHost("ec2-34-220-179-43.us-west-2.compute.amazonaws.com");
            factory.setVirtualHost("/");
            
            try {    
                Connection connection = factory.newConnection();
                Channel canal = connection.createChannel();
                
                String[] splitWae = getWae().split("\\p{Punct}");
                int i = splitWae.length;
                String arq = splitWae[i-2].concat(".").concat(splitWae[i-1]);
                String chaveArquivo = getChave().substring(1).concat("-files");
                
                File file = new File(getWae());
                int len = (int)file.length();
                byte[] arquivo = new byte[len];
                FileInputStream input  = new FileInputStream(file);
                input.read(arquivo);
                input.close();
                
                MensagemProto.Mensagem.Builder mensagem = MensagemProto.Mensagem.newBuilder();
                    
                mensagem.setEmissor(getEmissor());
                mensagem.setData(getData());
                mensagem.setHora(getHora());
                if(getChave().startsWith("#"))
                    mensagem.setGrupo(getChave());
                else
                    mensagem.setGrupo("");
                //mensagem.setTipo(getTipoMime());
                mensagem.setCorpo(ByteString.copyFrom(arquivo));
                mensagem.setNome(arq);
                
                MensagemProto.Mensagem wrapper = mensagem.build();
                byte[] buffer = wrapper.toByteArray();
                    
                if (getChave().startsWith("@")){
                    canal.basicPublish("", chaveArquivo, null, buffer);
                    System.out.println("Arquivo \"" + getWae() + "\" foi enviado para " + getChave());
                    System.out.print(getChave() + ">> ");
                }
                else {
                    canal.basicPublish(chaveArquivo, "", null, buffer);
                    System.out.println("Arquivo \"" + getWae() + "\" foi enviado para " + getChave());
                    System.out.print(getChave() + ">> ");
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage() + "\nAlgo deu errado, tente novamente.");
                System.out.print(getChave() + ">> ");
            }
        }
    };
    
    public static void main(String[] args){
        new Thread(envio).start();
    }
}