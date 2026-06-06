package com.employee.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TimeUtil {

    @Value("${app.timezone:Asia/Shanghai}")
    private String timezone;

    public LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of(timezone));
    }

    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }

    public String getTimezone() {
        return timezone;
    }

    public int getHour(LocalDateTime time) {
        return time.atZone(ZoneId.of(timezone)).getHour();
    }
}
