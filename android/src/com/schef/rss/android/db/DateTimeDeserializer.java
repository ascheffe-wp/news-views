/*
 * Copyright (c) 2014. The Washington Post. All rights reserved.
 */

package com.schef.rss.android.db;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeDeserializer implements JsonDeserializer<Date> {
    private static final Pattern XmlDatePattern = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}[+-][0-9]{2}):?([0-9]{2})");
    private static final Pattern ThisWeirdFormatWhichShouldGoPattern = Pattern.compile("201[0-9]{11}"); //20140205164613
    private static final Pattern MillisecondsAsStringPattern = Pattern.compile("[1-9][0-9]*");

    private final DateFormat enUsFormat;
    private final DateFormat localFormat;
    private final DateFormat iso8601FormatUtc;
    private final DateFormat iso8601Format;

    public DateTimeDeserializer(int dateStyle, int timeStyle) {
        enUsFormat = DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US);
        localFormat = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
        iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        iso8601FormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso8601FormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date should be a string value");
        }
        Date date = deserializeToDate(json);
        if (typeOfT == Date.class) {
            return date;
        } else if (typeOfT == Timestamp.class) {
            return new Timestamp(date.getTime());
        } else if (typeOfT == java.sql.Date.class) {
            return new java.sql.Date(date.getTime());
        } else {
            throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
        }
    }

    private Date deserializeToDate(JsonElement json) {
        if (json instanceof JsonPrimitive && ((JsonPrimitive) json).isNumber()) {
            try {
                return new Date(json.getAsLong());
            } catch (NumberFormatException ne) {
                Float dateLmt = json.getAsFloat();
                return new Date(dateLmt.longValue());
            }
        }

        String dateStr = json.getAsString();

        Matcher matcher = ThisWeirdFormatWhichShouldGoPattern.matcher(dateStr);
        if (matcher.matches()) {
            long d = Long.parseLong(dateStr);//yyyymmddhhmmss
            Calendar c = Calendar.getInstance();
            c.set(
                    (int)(d / 10000000000l),
                    (int)((d / 100000000l) % 100 - 1),
                    (int)((d / 1000000l) % 100),
                    (int)((d / 10000l) % 100),
                    (int)((d / 100) % 100),
                    (int)(d % 100)
            );
            return c.getTime();
        }

        matcher = MillisecondsAsStringPattern.matcher(dateStr);
        if (matcher.matches()) {
            return new Date(Long.parseLong(dateStr));
        }

        matcher = XmlDatePattern.matcher(dateStr);
        if (matcher.matches()) {
            if (matcher.groupCount() > 1) {
                synchronized (iso8601Format) {
                    try {
                        return iso8601Format.parse(matcher.group(1) + matcher.group(2));
                    } catch (ParseException e) {
                    }
                }
            }
        }

        synchronized (localFormat) {
            try {
                return localFormat.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }

        synchronized (enUsFormat) {
            try {
                return enUsFormat.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }

        synchronized (iso8601FormatUtc) {
            try {
                return iso8601FormatUtc.parse(json.getAsString());
            } catch (ParseException e) {
                throw new JsonSyntaxException(json.getAsString(), e);
            }
        }
    }
}
