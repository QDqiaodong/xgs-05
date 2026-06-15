package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.CheckInRecord;
import com.handmade.service.CheckInRecordService;
import com.handmade.vo.CheckInStatsVO;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInRecordController {

    @Autowired
    private CheckInRecordService checkInRecordService;

    @PostMapping
    public Result<CheckInRecord> checkIn(@RequestBody CheckInRecord record) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        record.setUserId(currentUserId);
        return Result.success(checkInRecordService.checkIn(record));
    }

    @GetMapping("/today")
    public Result<CheckInRecord> getTodayRecord() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(checkInRecordService.getTodayRecord(currentUserId));
    }

    @GetMapping("/month")
    public Result<List<CheckInRecord>> getMonthRecords(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(checkInRecordService.getMonthRecords(currentUserId, year, month));
    }

    @GetMapping("/calendar")
    public Result<List<Map<String, Object>>> getCalendarWall(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(checkInRecordService.getCalendarWall(currentUserId, year, month));
    }

    @GetMapping("/list")
    public Result<IPage<CheckInRecord>> getCheckInList(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (userId == null) {
            userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
        }
        return Result.success(checkInRecordService.getCheckInList(userId, page, size));
    }

    @GetMapping("/stats")
    public Result<CheckInStatsVO> getUserStats(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
        }
        return Result.success(checkInRecordService.getUserStats(userId));
    }

    @GetMapping("/timeline")
    public Result<List<CheckInRecord>> getGrowthTimeline(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "30") Integer limit) {
        if (userId == null) {
            userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
        }
        return Result.success(checkInRecordService.getGrowthTimeline(userId, limit));
    }

    @GetMapping("/{id}")
    public Result<CheckInRecord> getDetail(@PathVariable Long id) {
        return Result.success(checkInRecordService.getById(id));
    }

    @PostMapping("/{id}/like")
    public Result<Boolean> likeCheckIn(@PathVariable Long id) {
        return Result.success(checkInRecordService.likeCheckIn(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCheckIn(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        CheckInRecord record = checkInRecordService.getById(id);
        if (record == null || !record.getUserId().equals(currentUserId)) {
            return Result.error("无权限删除");
        }
        return Result.success(checkInRecordService.removeById(id));
    }
}
