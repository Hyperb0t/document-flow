package ru.itis.confirmchecker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassportDto {
    private String name;
    private String surname;
    private Integer age;
    private Integer passportSeries;
    private Integer passportNumber;
    private Date passportGiven;
}
