package com.handmade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.handmade.entity.Favorite;
import com.handmade.entity.Work;
import com.handmade.service.FavoriteService;
import com.handmade.service.WorkService;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private WorkService workService;

    @GetMapping("/list/{userId}")
    public Result<IPage<Work>> getFavoriteList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByDesc(Favorite::getCreateTime);
        IPage<Favorite> favoritePage = favoriteService.page(new Page<>(page, size), wrapper);

        List<Long> workIds = favoritePage.getRecords().stream()
                .map(Favorite::getWorkId)
                .collect(Collectors.toList());

        List<Work> works = workIds.isEmpty() ? List.of() : workService.listByIds(workIds);

        IPage<Work> result = new Page<>(page, size);
        result.setRecords(works);
        result.setTotal(favoritePage.getTotal());
        return Result.success(result);
    }

    @PostMapping
    @Transactional
    public Result<Boolean> addFavorite(@RequestBody Favorite favorite) {
        if (favorite.getUserId() == null || favorite.getWorkId() == null) {
            return Result.error("参数不完整");
        }
        try {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, favorite.getUserId());
            wrapper.eq(Favorite::getWorkId, favorite.getWorkId());
            Favorite exist = favoriteService.getOne(wrapper);
            if (exist != null) {
                return Result.success(true);
            }
            boolean saved = favoriteService.save(favorite);
            if (saved) {
                workService.incrementFavoriteCount(favorite.getWorkId(), true);
            }
            return Result.success(true);
        } catch (Exception e) {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, favorite.getUserId());
            wrapper.eq(Favorite::getWorkId, favorite.getWorkId());
            if (favoriteService.count(wrapper) > 0) {
                return Result.success(true);
            }
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    @DeleteMapping
    @Transactional
    public Result<Boolean> removeFavorite(@RequestParam Long userId, @RequestParam Long workId) {
        if (userId == null || workId == null) {
            return Result.error("参数不完整");
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getWorkId, workId);
        boolean removed = favoriteService.remove(wrapper);
        if (removed) {
            workService.incrementFavoriteCount(workId, false);
        }
        return Result.success(removed);
    }

    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long userId, @RequestParam Long workId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getWorkId, workId);
        return Result.success(favoriteService.count(wrapper) > 0);
    }
}
