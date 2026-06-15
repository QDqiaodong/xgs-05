package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.CheckInRecord;
import com.handmade.mapper.CheckInRecordMapper;
import com.handmade.service.CheckInRecordService;
import com.handmade.vo.CheckInStatsVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckInRecordServiceImpl extends ServiceImpl<CheckInRecordMapper, CheckInRecord> implements CheckInRecordService {

    @Override
    public CheckInRecord checkIn(CheckInRecord record) {
        Long userId = record.getUserId();
        LocalDate today = record.getCheckInDate() != null ? record.getCheckInDate() : LocalDate.now();

        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getCheckInDate, today);
        CheckInRecord existing = this.getOne(wrapper);

        if (existing != null) {
            record.setId(existing.getId());
            this.updateById(record);
            return this.getById(existing.getId());
        } else {
            record.setCheckInDate(today);
            record.setViewCount(0);
            record.setLikeCount(0);
            this.save(record);
            return record;
        }
    }

    @Override
    public CheckInRecord getTodayRecord(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
                .eq(CheckInRecord::getCheckInDate, today);
        return this.getOne(wrapper);
    }

    @Override
    public List<CheckInRecord> getMonthRecords(Long userId, Integer year, Integer month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
                .between(CheckInRecord::getCheckInDate, start, end)
                .orderByAsc(CheckInRecord::getCheckInDate);
        return this.list(wrapper);
    }

    @Override
    public IPage<CheckInRecord> getCheckInList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(CheckInRecord::getUserId, userId);
        }
        wrapper.orderByDesc(CheckInRecord::getCheckInDate);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public CheckInStatsVO getUserStats(Long userId) {
        CheckInStatsVO stats = new CheckInStatsVO();

        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
                .orderByAsc(CheckInRecord::getCheckInDate);
        List<CheckInRecord> allRecords = this.list(wrapper);

        int totalDays = allRecords.size();
        stats.setTotalDays(totalDays);

        if (totalDays == 0) {
            stats.setCurrentStreak(0);
            stats.setLongestStreak(0);
            stats.setTotalDurationMinutes(0);
            stats.setTotalWorks(0);
            stats.setThisMonthDays(0);
            return stats;
        }

        int totalDuration = allRecords.stream()
                .mapToInt(r -> r.getWorkDuration() != null ? r.getWorkDuration() : 0)
                .sum();
        stats.setTotalDurationMinutes(totalDuration);

        long totalWorks = allRecords.stream()
                .filter(r -> r.getWorkId() != null)
                .count();
        stats.setTotalWorks((int) totalWorks);

        YearMonth currentMonth = YearMonth.now();
        long thisMonthCount = allRecords.stream()
                .filter(r -> YearMonth.from(r.getCheckInDate()).equals(currentMonth))
                .count();
        stats.setThisMonthDays((int) thisMonthCount);

        List<LocalDate> dates = allRecords.stream()
                .map(CheckInRecord::getCheckInDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate prevDate = null;

        for (LocalDate date : dates) {
            if (prevDate == null) {
                currentStreak = 1;
            } else if (date.minusDays(1).equals(prevDate)) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            longestStreak = Math.max(longestStreak, currentStreak);
            prevDate = date;
        }
        stats.setLongestStreak(longestStreak);

        LocalDate today = LocalDate.now();
        int actualCurrentStreak = 0;
        LocalDate checkDate = today;

        if (!dates.contains(today)) {
            checkDate = today.minusDays(1);
        }

        while (dates.contains(checkDate)) {
            actualCurrentStreak++;
            checkDate = checkDate.minusDays(1);
        }
        stats.setCurrentStreak(actualCurrentStreak);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getCalendarWall(Long userId, Integer year, Integer month) {
        List<CheckInRecord> records = getMonthRecords(userId, year, month);
        Map<LocalDate, CheckInRecord> recordMap = records.stream()
                .collect(Collectors.toMap(CheckInRecord::getCheckInDate, r -> r));

        YearMonth ym = YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();
        List<Map<String, Object>> calendar = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", date.toString());
            dayInfo.put("day", day);
            dayInfo.put("isToday", date.equals(LocalDate.now()));
            dayInfo.put("isFuture", date.isAfter(LocalDate.now()));

            CheckInRecord record = recordMap.get(date);
            if (record != null) {
                dayInfo.put("checkedIn", true);
                dayInfo.put("recordId", record.getId());
                dayInfo.put("title", record.getTitle());
                dayInfo.put("content", record.getContent());
                dayInfo.put("images", record.getImages());
                dayInfo.put("workDuration", record.getWorkDuration());
                dayInfo.put("moodTag", record.getMoodTag());
                dayInfo.put("weatherTag", record.getWeatherTag());
                dayInfo.put("likeCount", record.getLikeCount());
            } else {
                dayInfo.put("checkedIn", false);
            }

            calendar.add(dayInfo);
        }

        return calendar;
    }

    @Override
    public List<CheckInRecord> getGrowthTimeline(Long userId, Integer limit) {
        LambdaQueryWrapper<CheckInRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckInRecord::getUserId, userId)
                .isNotNull(CheckInRecord::getImages)
                .ne(CheckInRecord::getImages, "")
                .orderByDesc(CheckInRecord::getCheckInDate)
                .last("LIMIT " + (limit != null ? limit : 30));
        return this.list(wrapper);
    }

    @Override
    public boolean likeCheckIn(Long id) {
        CheckInRecord record = this.getById(id);
        if (record == null) {
            return false;
        }
        record.setLikeCount((record.getLikeCount() != null ? record.getLikeCount() : 0) + 1);
        return this.updateById(record);
    }
}
