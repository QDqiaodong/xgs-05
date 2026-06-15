package com.handmade.vo;

import lombok.Data;

@Data
public class CheckInStatsVO {
    private Integer totalDays;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer totalDurationMinutes;
    private Integer totalWorks;
    private Integer thisMonthDays;
}
