package ru.itis.pdffiller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ru.itis.pdffiller.dto.PassportDto;
import ru.itis.pdffiller.services.PdfTemplateEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Hello world!
 *
 */
public class PdfFiller
{

    private final static String ACCEPT_EXCHANGE_NAME = "confirmation_exchange";

    private final static String FILLING_ROUTING_KEY = "filling.*";

    private final static String SENDER_EXCHANGE = "sender_exchange";

    public static void main( String[] args )
    {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        ObjectMapper objectMapper = new ObjectMapper();
        PdfTemplateEngine templateEngine = new PdfTemplateEngine();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ACCEPT_EXCHANGE_NAME, FILLING_ROUTING_KEY);
            channel.basicConsume(queueName, false, (consumerTag, message) -> {
                PassportDto passportDto = objectMapper.readValue(message.getBody(), PassportDto.class);
                System.out.println("get " + passportDto);
                File template = null;
                try {
                    template = new File(passportDto.getClass().getClassLoader().getResource("templates/otchisl2.pdf").toURI());
                } catch (URISyntaxException e) {
                    throw new IllegalStateException(e);
                }
                File output = new File("temp.pdf");
                Map<String, String> formsContent = new HashMap<>();
                formsContent.put("fio", passportDto.getName() + " " + passportDto.getSurname());
                formsContent.put("age", passportDto.getAge().toString());
                formsContent.put("passport", passportDto.getPassportSeries() + " " + passportDto.getPassportNumber());
                formsContent.put("given", format.format(passportDto.getPassportGiven()));


                templateEngine.render(template, formsContent, output);
                System.out.println("rendered");
                System.out.println();
                channel.basicPublish(SENDER_EXCHANGE, "", null, Files.readAllBytes(Paths.get(output.toURI())));
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
            },
                    consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
