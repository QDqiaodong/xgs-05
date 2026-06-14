package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.Activity;
import com.handmade.entity.User;
import com.handmade.service.ActivityService;
import com.handmade.service.ActivityWorkService;
import com.handmade.service.ActivityVoteService;
import com.handmade.service.UserService;
import com.handmade.vo.ActivityDetailVO;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityWorkService activityWorkService;

    @Autowired
    private ActivityVoteService activityVoteService;

    @GetMapping("/list")
    public Result<IPage<Activity>> getActivityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(activityService.getActivityList(page, size, status));
    }

    @GetMapping("/ongoing")
    public Result<IPage<Activity>> getOngoingActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(activityService.getOngoingActivities(page, size));
    }

    @GetMapping("/{id}")
    public Result<ActivityDetailVO> getActivityDetail(@PathVariable Long id) {
        ActivityDetailVO detail = activityService.getActivityDetail(id);
        if (detail == null) {
            return Result.error("活动不存在");
        }
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null) {
            detail.setMySubmitCount(activityWorkService.getUserSubmitCount(id, currentUserId));
            detail.setMyVoteCount(activityVoteService.getUserVoteCount(id, currentUserId));
        }
        return Result.success(detail);
    }

    @PostMapping
    public Result<Long> createActivity(@RequestBody Activity activity) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        activity.setCreateBy(currentUserId);
        return Result.success(activityService.createActivity(activity));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(activityService.updateActivity(id, activity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteActivity(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(activityService.deleteActivity(id));
    }
}
