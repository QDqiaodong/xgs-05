package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.CreatorVerification;
import com.handmade.entity.User;
import com.handmade.service.CreatorVerificationService;
import com.handmade.service.UserService;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/creator-verification")
public class CreatorVerificationController {

    @Autowired
    private CreatorVerificationService creatorVerificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public Result<Long> submitApplication(@RequestBody CreatorVerification verification) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        CreatorVerification existing = creatorVerificationService.getLatestByUserId(currentUserId);
        if (existing != null && existing.getStatus() != null && existing.getStatus() == 0) {
            return Result.error("您已有待审核的认证申请，请耐心等待审核结果");
        }

        verification.setUserId(currentUserId);
        return Result.success(creatorVerificationService.submitApplication(verification));
    }

    @GetMapping("/my-status")
    public Result<CreatorVerification> getMyVerificationStatus() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(creatorVerificationService.getLatestByUserId(currentUserId));
    }

    @GetMapping("/user/{userId}/status")
    public Result<CreatorVerification> getUserVerificationStatus(@PathVariable Long userId) {
        return Result.success(creatorVerificationService.getLatestByUserId(userId));
    }

    @GetMapping("/list")
    public Result<IPage<Map<String, Object>>> getApplicationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(creatorVerificationService.getApplicationList(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getApplicationDetail(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(creatorVerificationService.getApplicationDetail(id));
    }

    @PutMapping("/{id}/approve")
    public Result<Boolean> approveApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String remark) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(creatorVerificationService.approveApplication(id, currentUserId, remark));
    }

    @PutMapping("/{id}/reject")
    public Result<Boolean> rejectApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String remark) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(creatorVerificationService.rejectApplication(id, currentUserId, remark));
    }
}
