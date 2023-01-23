package com.gordonfromblumberg.games.core.common.utils;

import java.util.Calendar;

public class DateTimeFormatter {
    private static final int YEAR_0 = 0;
    private static final int YEAR_1 = 1;
    private static final int YEAR_2 = 2;
    private static final int YEAR_3 = 3;
    private static final int YEAR_MONTH_DEL = 4;
    private static final int MONTH_0 = 5;
    private static final int MONTH_1 = 6;
    private static final int MONTH_DAY_DEL = 7;
    private static final int DAY_0 = 8;
    private static final int DAY_1 = 9;
    private static final int DATE_TIME_DEL = 10;
    private static final int HOUR_0 = 11;
    private static final int HOUR_1 = 12;
    private static final int HOUR_MINUTE_DEL = 13;
    private static final int MINUTE_0 = 14;
    private static final int MINUTE_1 = 15;
    private static final int MINUTE_SECOND_DEL = 16;
    private static final int SECOND_0 = 17;
    private static final int SECOND_1 = 18;
    private static final int SECOND_MILLIS_DEL = 19;
    private static final int MILLIS_0 = 20;
    private static final int MILLIS_1 = 21;
    private static final int MILLIS_2 = 22;

    private final Calendar calendar = Calendar.getInstance();
    private final boolean withMillis;
    private final char[] charBuffer;

    public DateTimeFormatter(boolean withMillis) {
        this.withMillis = withMillis;
        charBuffer = new char[withMillis ? 23 : 19];
        charBuffer[YEAR_MONTH_DEL] = charBuffer[MONTH_DAY_DEL] = '/';
        charBuffer[DATE_TIME_DEL] = ' ';
        charBuffer[HOUR_MINUTE_DEL] = charBuffer[MINUTE_SECOND_DEL] = ':';
        if (withMillis) {
            charBuffer[SECOND_MILLIS_DEL] = '.';
        }
    }

    public String format(long timestamp) {
        calendar.setTimeInMillis(timestamp);

        int year = calendar.get(Calendar.YEAR);
        charBuffer[YEAR_3] = Character.forDigit(year % 10, 10);
        year /= 10;
        charBuffer[YEAR_2] = Character.forDigit(year % 10, 10);
        year /= 10;
        charBuffer[YEAR_1] = Character.forDigit(year % 10, 10);
        year /= 10;
        charBuffer[YEAR_0] = Character.forDigit(year % 10, 10);

        int month = calendar.get(Calendar.MONTH) + 1;
        charBuffer[MONTH_1] = Character.forDigit(month % 10, 10);
        month /= 10;
        charBuffer[MONTH_0] = Character.forDigit(month % 10, 10);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        charBuffer[DAY_1] = Character.forDigit(day % 10, 10);
        day /= 10;
        charBuffer[DAY_0] = Character.forDigit(day % 10, 10);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        charBuffer[HOUR_1] = Character.forDigit(hour % 10, 10);
        hour /= 10;
        charBuffer[HOUR_0] = Character.forDigit(hour % 10, 10);

        int minute = calendar.get(Calendar.MINUTE);
        charBuffer[MINUTE_1] = Character.forDigit(minute % 10, 10);
        minute /= 10;
        charBuffer[MINUTE_0] = Character.forDigit(minute % 10, 10);

        int second = calendar.get(Calendar.SECOND);
        charBuffer[SECOND_1] = Character.forDigit(second % 10, 10);
        second /= 10;
        charBuffer[SECOND_0] = Character.forDigit(second % 10, 10);

        if (withMillis) {
            int millis = calendar.get(Calendar.MILLISECOND);
            charBuffer[MILLIS_2] = Character.forDigit(millis % 10, 10);
            millis /= 10;
            charBuffer[MILLIS_1] = Character.forDigit(millis % 10, 10);
            millis /= 10;
            charBuffer[MILLIS_0] = Character.forDigit(millis % 10, 10);
        }

        return String.valueOf(charBuffer);
    }
}
