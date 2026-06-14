package com.handmade.controller;

import com.handmade.context.UserContext;
import com.handmade.service.ActivityVoteService;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activity-vote")
public class ActivityVoteController {

    @Autowired
    private ActivityVoteService activityVoteService;

    @PostMapping("/vote")
    public Result<Boolean> vote(@RequestBody Map<String, Object> params) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        Long activityId = params.get("activityId") != null ? Long.valueOf(params.get("activityId").toString()) : null;
        Long activityWorkId = params.get("activityWorkId") != null ? Long.valueOf(params.get("activityWorkId").toString()) : null;
        Long workId = params.get("workId") != null ? Long.valueOf(params.get("workId").toString()) : null;
        Integer voteCount = params.get("voteCount") != null ? Integer.valueOf(params.get("voteCount").toString()) : 1;
        if (activityId == null || activityWorkId == null || workId == null) {
            return Result.error("参数不完整");
        }
        boolean success = activityVoteService.vote(activityId, activityWorkId, workId, currentUserId, voteCount);
        if (!success) {
            return Result.error("投票失败，请检查投票限制");
        }
        return Result.success(true);
    }

    @GetMapping("/my-vote-count")
    public Result<Integer> getMyVoteCount(@RequestParam Long activityId) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.success(0);
        }
        return Result.success(activityVoteService.getUserVoteCount(activityId, currentUserId));
    }

    @GetMapping("/has-voted")
    public Result<Map<String, Object>> hasVotedWork(
            @RequestParam Long activityId,
            @RequestParam Long activityWorkId) {
        Map<String, Object> result = new HashMap<>();
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            result.put("hasVoted", false);
            result.put("voteCount", 0);
            return Result.success(result);
        }
        result.put("hasVoted", activityVoteService.hasVotedWork(activityId, activityWorkId, currentUserId));
        result.put("voteCount", activityVoteService.getUserWorkVoteCount(activityId, activityWorkId, currentUserId));
        return Result.success(result);
    }
}
