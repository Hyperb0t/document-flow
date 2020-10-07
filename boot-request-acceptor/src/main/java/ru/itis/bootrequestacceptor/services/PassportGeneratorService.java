package ru.itis.bootrequestacceptor.services;

import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import ru.itis.bootrequestacceptor.dto.PassportDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
public class PassportGeneratorService {

    private final static int FEMALE = 0;
    private final static int MALE = 1;
    private List<List<String>> genderSplitNames;
    private List<List<String>> genderSplitSurnames;
    private Random r = new Random();

    public PassportGeneratorService() {
        try {
            genderSplitNames = new ArrayList<>();

            genderSplitNames.add(Files.readAllLines(ResourceUtils.getFile("classpath:names_f.txt").toPath()));
            genderSplitNames.add(Files.readAllLines(ResourceUtils.getFile("classpath:names_m.txt").toPath()));

            genderSplitSurnames = new ArrayList<>();
            genderSplitSurnames.add(Files.readAllLines(ResourceUtils.getFile("classpath:surnames_f.txt").toPath()));
            genderSplitSurnames.add(Files.readAllLines(ResourceUtils.getFile("classpath:surnames_m.txt").toPath()));
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public PassportDto generate() {
        int gender = r.nextInt(2);
        String name = genderSplitNames.get(gender).get(r.nextInt(genderSplitNames.get(gender).size()));
        String surname = genderSplitSurnames.get(gender).get(r.nextInt(genderSplitSurnames.get(gender).size()));
        Integer age = 18 + r.nextInt(52);
        Date given = Date.from(Instant.now().minus(Duration.ofDays(r.nextInt(365*30))));
        int series = 1000 + r.nextInt(8999);
        int number = 100000 + r.nextInt(899999);
        return PassportDto.builder()
                .name(name)
                .surname(surname)
                .age(age)
                .passportSeries(series)
                .passportNumber(number)
                .passportGiven(given)
                .build();
    }
}
