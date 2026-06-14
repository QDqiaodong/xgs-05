package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.User;
import com.handmade.service.ChainWorkService;
import com.handmade.service.UserService;
import com.handmade.vo.ChainWorkVO;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chain-work")
public class ChainWorkController {

    @Autowired
    private ChainWorkService chainWorkService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<IPage<ChainWorkVO>> getChainWorks(
            @RequestParam Long chainActivityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy) {
        return Result.success(chainWorkService.getChainWorks(chainActivityId, page, size, sortBy));
    }

    @GetMapping("/my")
    public Result<IPage<ChainWorkVO>> getMyChainWorks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(chainWorkService.getMyChainWorks(currentUserId, page, size));
    }

    @GetMapping("/{id}")
    public Result<ChainWorkVO> getChainWorkDetail(@PathVariable Long id) {
        return Result.success(chainWorkService.getChainWorkDetail(id));
    }

    @PostMapping("/submit")
    public Result<Long> submitChainWork(@RequestBody Map<String, Object> data) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        Long chainActivityId = data.get("chainActivityId") != null ? Long.valueOf(data.get("chainActivityId").toString()) : null;
        Long workId = data.get("workId") != null ? Long.valueOf(data.get("workId").toString()) : null;
        Long parentChainWorkId = data.get("parentChainWorkId") != null ? Long.valueOf(data.get("parentChainWorkId").toString()) : null;
        String inspirationRemark = data.get("inspirationRemark") != null ? data.get("inspirationRemark").toString() : null;

        if (chainActivityId == null || workId == null) {
            return Result.error("参数不完整");
        }

        Long result = chainWorkService.submitChainWork(chainActivityId, workId, currentUserId, parentChainWorkId, inspirationRemark);
        if (result == null) {
            return Result.error("投稿失败，请检查条件");
        }
        return Result.success(result);
    }

    @PutMapping("/{id}/audit")
    public Result<Boolean> auditChainWork(
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
        return Result.success(chainWorkService.auditChainWork(id, auditStatus, auditRemark, currentUserId));
    }
}
