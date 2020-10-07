package ru.itis.loggerstat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ru.itis.loggerstat.dto.PassportDto;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Hello world!
 *
 */
public class LoggerStat
{
    private final static String ACCEPT_EXCHANGE_NAME = "confirmation_exchange";
    private final static String LOGGING_ROUTING_KEY = "*.logging";

    public static void main( String[] args )
    {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("passports.log"), true);
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, ACCEPT_EXCHANGE_NAME, LOGGING_ROUTING_KEY);
            channel.basicConsume(queueName, false, (consumerTag, message) -> {
                    PassportDto passportDto = objectMapper.readValue(message.getBody(), PassportDto.class);
                    String routingKey = message.getEnvelope().getRoutingKey();
                    String confirmedState = routingKey.substring(0, routingKey.indexOf(".")).equals("filling") ?
                            "confirmed" : "not_confirmed";
                    String logLine = Instant.now() + " " + confirmedState
                            + " " + passportDto.toString();
                    System.out.println(logLine);
                    writer.println(logLine);
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
            },
                    consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
