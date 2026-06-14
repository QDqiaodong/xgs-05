package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.User;
import com.handmade.service.ActivityWorkService;
import com.handmade.service.UserService;
import com.handmade.vo.ActivityWorkVO;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activity-work")
public class ActivityWorkController {

    @Autowired
    private ActivityWorkService activityWorkService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<IPage<ActivityWorkVO>> getActivityWorks(
            @RequestParam Long activityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "time") String sortBy) {
        return Result.success(activityWorkService.getActivityWorks(activityId, page, size, sortBy));
    }

    @GetMapping("/my")
    public Result<IPage<ActivityWorkVO>> getMySubmissions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(activityWorkService.getMySubmissions(currentUserId, page, size));
    }

    @PostMapping("/submit")
    public Result<Long> submitWork(@RequestBody Map<String, Object> params) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        Long activityId = params.get("activityId") != null ? Long.valueOf(params.get("activityId").toString()) : null;
        Long workId = params.get("workId") != null ? Long.valueOf(params.get("workId").toString()) : null;
        String submitRemark = params.get("submitRemark") != null ? params.get("submitRemark").toString() : null;
        if (activityId == null || workId == null) {
            return Result.error("参数不完整");
        }
        Long id = activityWorkService.submitWork(activityId, workId, currentUserId, submitRemark);
        if (id == null) {
            return Result.error("投稿失败，请检查活动时间、投稿限制或作品权限");
        }
        return Result.success(id);
    }

    @PutMapping("/{id}/audit")
    public Result<Boolean> auditWork(
            @PathVariable Long id,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String auditRemark) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(activityWorkService.auditWork(id, auditStatus, auditRemark, currentUserId));
    }
}
