package com.example.jsonschemagenerator.generator.dateValidator;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DateValidator {

    private record DateFormat(
            DateTimeFormatter formatter,
            String type
    ){}
    private static final List<DateFormat> DATE_FORMATS = List.of(
            new DateFormat(DateTimeFormatter.ISO_DATE_TIME, "date-time"),
            new DateFormat(DateTimeFormatter.ISO_DATE, "date")
    );

    public Optional<String> detectFormat(String dateString){

        for(var dateFormat : DATE_FORMATS){
            if(isValid(dateFormat.formatter, dateString)){
                return Optional.of(dateFormat.type);
            }
        }

        return Optional.empty();
    }

    private boolean isValid(DateTimeFormatter dateTimeFormatter,String dateString){

        try{
            dateTimeFormatter.parse(dateString);

        }catch (DateTimeException e){
            return false;
        }

        return true;
    }


}
