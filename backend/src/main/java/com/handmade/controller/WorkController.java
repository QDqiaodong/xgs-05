package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.entity.Work;
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

    @GetMapping("/list")
    public Result<IPage<Work>> getWorkList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(workService.getWorkList(page, size, categoryId, keyword));
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

    @PostMapping
    public Result<Long> createWork(@RequestBody Work work) {
        work.setViewCount(0);
        work.setFavoriteCount(0);
        work.setLikeCount(0);
        work.setStatus(1);
        work.setIsHot(0);
        workService.save(work);
        return Result.success(work.getId());
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateWork(@PathVariable Long id, @RequestBody Work work) {
        work.setId(id);
        return Result.success(workService.updateById(work));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteWork(@PathVariable Long id) {
        return Result.success(workService.removeById(id));
    }
}
