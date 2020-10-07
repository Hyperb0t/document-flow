package ru.itis.bootrequestacceptor.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.itis.bootrequestacceptor.dto.PassportDto;

@Service
public class PassportHandlerService {

    //take it from app.properties ?
    //use @Value in config ?
    private final String CHECK_NEEDED_ROUTING_KEY ="check_needed";

    private final String CHECK_NOT_NEEDED_ROUTING_KEY ="filling.logging";

    private final String EXCHANGE_NAME = "passport_accept_exchange";

    private final String EXCHANGE_TYPE ="direct";

    private final String CHECK_NEEDED_QUEUE = "check_needed_queue";

    private final String CHECK_NOT_NEEDED_EXCHANGE = "confirmation_exchange";

    @Autowired
    private Connection connection;

    @Autowired
    private ObjectMapper objectMapper;

    public void handle(PassportDto passportDto) {

        try {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            channel.queueBind(CHECK_NEEDED_QUEUE, EXCHANGE_NAME, CHECK_NEEDED_ROUTING_KEY);
            channel.exchangeBind(CHECK_NOT_NEEDED_EXCHANGE, EXCHANGE_NAME, CHECK_NOT_NEEDED_ROUTING_KEY);
            String currentRouting = checkNeeded(passportDto) ? CHECK_NEEDED_ROUTING_KEY : CHECK_NOT_NEEDED_ROUTING_KEY;
            channel.basicPublish(EXCHANGE_NAME, currentRouting, null, objectMapper.writeValueAsBytes(passportDto));
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean checkNeeded(PassportDto passportDto) {
        //connect to db and check
        return Math.random() < 0.7;
    }
}
