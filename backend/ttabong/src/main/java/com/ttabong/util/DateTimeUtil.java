package com.ttabong.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtil {

    private static final String DEFAULT_ZONE = "Asia/Seoul";

    public static LocalDateTime convertToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDateTime();
    }

    public static LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.of(DEFAULT_ZONE)).toLocalDate();
    }
}