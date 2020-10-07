package ru.itis.bootrequestacceptor.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.itis.bootrequestacceptor.dto.PassportDto;
import ru.itis.bootrequestacceptor.services.PassportGeneratorService;
import ru.itis.bootrequestacceptor.services.PassportHandlerService;

import java.util.Collections;

@Controller

public class PassportController {

    @Autowired
    private PassportHandlerService passportHandlerService;

    @Autowired
    private PassportGeneratorService passportGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/accept")
    public ResponseEntity acceptPassportData(@RequestBody PassportDto passportDto) throws JsonProcessingException {
        passportHandlerService.handle(passportDto);
        return new ResponseEntity("get passport data: \n" + objectMapper.writeValueAsString(passportDto),
                new LinkedMultiValueMap(Collections.singletonMap("Content-Type",Collections.singletonList("text/html;charset=UTF-8"))),
                HttpStatus.OK);
    }

    @GetMapping("/simulate")
    public ResponseEntity simulatePassportData() throws JsonProcessingException {
        PassportDto passportDto = passportGeneratorService.generate();
        passportHandlerService.handle(passportDto);
        return new ResponseEntity("generated passport data: \n" + objectMapper.writeValueAsString(passportDto),
                new LinkedMultiValueMap(Collections.singletonMap("Content-Type",Collections.singletonList("text/html;charset=UTF-8"))),
                HttpStatus.OK);
    }
}
