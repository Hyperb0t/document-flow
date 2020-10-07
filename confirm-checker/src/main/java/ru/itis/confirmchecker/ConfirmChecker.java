package ru.itis.confirmchecker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import ru.itis.confirmchecker.dto.PassportDto;

import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class ConfirmChecker
{

    private static final String RECEIVE_QUEUE_NAME = "check_needed_queue";

    private static final String EXCHANGE_NAME  = "confirmation_exchange";

    private static final String CONFIRMED_ROUTING_KEY = "filling.logging";

    private static final String NON_CONFIRMED_ROUTING_KEY = "not_filling.logging";

    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(1);
            channel.basicConsume(RECEIVE_QUEUE_NAME, false, (consumerTag, message) -> {
                PassportDto passportDto = objectMapper.readValue(message.getBody(), PassportDto.class);
                System.out.println("Имя:" + passportDto.getName());
                System.out.println("Фамилия: " + passportDto.getSurname());
                System.out.println("Возраст: " + passportDto.getAge());
                System.out.println("Серия: " + passportDto.getPassportSeries());
                System.out.println("Номер: " + passportDto.getPassportNumber());
                System.out.println("Выдан: " + format.format(passportDto.getPassportGiven()));
                System.out.println("введити Y чтобы подтвердить или N чтобы опровергнуть");
                boolean suitableAnswer = false;
                while (!suitableAnswer) {
                    String input = scanner.nextLine();
                    if(input.equals("Y") || input.equals("y")) {
                        channel.basicPublish(EXCHANGE_NAME, CONFIRMED_ROUTING_KEY, null,
                                objectMapper.writeValueAsBytes(passportDto));
                        channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                        System.out.println("Подтверждено");
                        System.out.println();
                        suitableAnswer = true;
                    }
                    else if(input.equals("N") || input.equals("n")) {
                        channel.basicPublish(EXCHANGE_NAME, NON_CONFIRMED_ROUTING_KEY, null,
                                objectMapper.writeValueAsBytes(passportDto));
                        channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                        System.out.println("Отвергнуто");
                        System.out.println();
                        suitableAnswer = true;
                    }
                    else {
                        System.out.println("введити Y чтобы подтвердить или N чтобы опровергнуть");
                    }
                }
            },consumerTag -> {});
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
