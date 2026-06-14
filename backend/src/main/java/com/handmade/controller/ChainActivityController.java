package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.ChainActivity;
import com.handmade.entity.User;
import com.handmade.service.ChainActivityService;
import com.handmade.service.ChainWorkService;
import com.handmade.service.UserService;
import com.handmade.vo.ChainActivityDetailVO;
import com.handmade.vo.ChainGenealogyVO;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chain-activity")
public class ChainActivityController {

    @Autowired
    private ChainActivityService chainActivityService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChainWorkService chainWorkService;

    @GetMapping("/list")
    public Result<IPage<ChainActivity>> getChainActivityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return Result.success(chainActivityService.getChainActivityList(page, size, status));
    }

    @GetMapping("/ongoing")
    public Result<IPage<ChainActivity>> getOngoingChainActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(chainActivityService.getOngoingChainActivities(page, size));
    }

    @GetMapping("/{id}")
    public Result<ChainActivityDetailVO> getChainActivityDetail(@PathVariable Long id) {
        ChainActivityDetailVO detail = chainActivityService.getChainActivityDetail(id);
        if (detail == null) {
            return Result.error("接龙活动不存在");
        }
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null) {
            detail.setMySubmitCount(chainWorkService.getUserSubmitCount(id, currentUserId));
        }
        return Result.success(detail);
    }

    @GetMapping("/{id}/genealogy")
    public Result<ChainGenealogyVO> getChainGenealogy(@PathVariable Long id) {
        return Result.success(chainActivityService.getChainGenealogy(id));
    }

    @PostMapping
    public Result<Long> createChainActivity(@RequestBody ChainActivity chainActivity) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        chainActivity.setCreateBy(currentUserId);
        return Result.success(chainActivityService.createChainActivity(chainActivity));
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateChainActivity(@PathVariable Long id, @RequestBody ChainActivity chainActivity) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(chainActivityService.updateChainActivity(id, chainActivity));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteChainActivity(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(chainActivityService.deleteChainActivity(id));
    }
}
