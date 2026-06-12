package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.User;
import com.handmade.entity.Work;
import com.handmade.service.UserService;
import com.handmade.service.WorkService;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work")
public class WorkController {

    @Autowired
    private WorkService workService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<IPage<Work>> getWorkList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficultyLevel) {
        return Result.success(workService.getWorkList(page, size, categoryId, keyword, difficultyLevel));
    }

    @PutMapping("/{id}/difficulty")
    public Result<Boolean> setDifficultyLevel(
            @PathVariable Long id,
            @RequestParam Integer difficultyLevel) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
            return Result.error("无权限执行此操作");
        }
        return Result.success(workService.setDifficultyLevel(id, difficultyLevel));
    }

    @GetMapping("/hot")
    public Result<IPage<Work>> getHotWorks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(workService.getHotWorks(page, size));
    }

    @GetMapping("/user/{userId}")
    public Result<IPage<Work>> getUserWorks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(workService.getUserWorks(userId, page, size));
    }

    @GetMapping("/{id}")
    public Result<Work> getWorkDetail(@PathVariable Long id) {
        Work work = workService.getById(id);
        if (work != null) {
            workService.incrementViewCount(id);
        }
        return Result.success(work);
    }

    @GetMapping("/materials/suggest")
    public Result<List<Map<String, Object>>> suggestMaterials(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(workService.suggestMaterials(keyword, limit));
    }

    @GetMapping("/{id}/recommend")
    public Result<List<Work>> getRecommendedWorks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(workService.getRecommendedWorks(id, limit));
    }

    @PostMapping
    public Result<Long> createWork(@RequestBody Work work) {
        work.setViewCount(0);
        work.setFavoriteCount(0);
        work.setLikeCount(0);
        work.setStatus(1);
        work.setIsHot(0);
        workService.save(work);
        workService.clearWorkCaches();
        if (work.getUserId() != null) {
            userService.updateCreatorStats(work.getUserId());
        }
        return Result.success(work.getId());
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateWork(@PathVariable Long id, @RequestBody Work work) {
        work.setId(id);
        boolean result = workService.updateById(work);
        if (result) {
            workService.clearWorkCaches();
        }
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteWork(@PathVariable Long id) {
        Work work = workService.getById(id);
        Long userId = work != null ? work.getUserId() : null;
        boolean result = workService.removeById(id);
        if (result) {
            workService.clearWorkCaches();
            if (userId != null) {
                userService.updateCreatorStats(userId);
            }
        }
        return Result.success(result);
    }
}
