package ru.practicum.ewm.util;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.exception.custom.InvalidParameterException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Slf4j
public final class ParserUtils {
    private ParserUtils() {
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(STANDARD));
        } catch (DateTimeParseException e) {
            log.warn("Попытка передать дату в другом формате");
            throw new InvalidParameterException("Передан неподдерживаемый формат даты");
        }
    }

    public static <T extends Enum<T>> T parseEnum(String enumStr, Class<T> enumClass) {
        if (enumStr == null || enumStr.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, enumStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Попытка передать несуществующий статус события");
            throw new InvalidParameterException("Передан неподдерживаемый параметр: " + enumStr);
        }
    }

    public static <T extends Enum<T>> List<T> parseEnum(List<String> enumStr, Class<T> enumClass) {
        if (enumStr == null || enumStr.isEmpty()) {
            return Collections.emptyList();
        }

        return enumStr.stream()
                .map(str -> parseEnum(str, enumClass))
                .toList();
    }
}
