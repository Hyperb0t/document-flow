package ru.itis.pdfsaver;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Hello world!
 *
 */
public class PdfSaver
{
    private final static String QUEUE_NAME = "pdf_save_queue";

    public static void main( String[] args )
    {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicConsume(QUEUE_NAME, false, (consumerTag, message) -> {
                File f = new File("pdfs/" + UUID.randomUUID() + ".pdf");
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(message.getBody());
//                    System.out.println(Arrays.toString(message.getBody()));
                    System.out.println("saved to " + f.getAbsolutePath());
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                }
                catch (Exception e) {
                    System.out.println("couldn't save file");
                }
            }, consumerTag -> {});
        }catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }
}
