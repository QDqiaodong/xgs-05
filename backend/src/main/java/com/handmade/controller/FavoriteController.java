package com.handmade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.handmade.entity.Favorite;
import com.handmade.entity.FavoriteFolder;
import com.handmade.entity.Work;
import com.handmade.service.FavoriteFolderService;
import com.handmade.service.FavoriteService;
import com.handmade.service.WorkService;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private WorkService workService;

    @Autowired
    private FavoriteFolderService favoriteFolderService;

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
        Long folderId = favorite.getFolderId();
        if (folderId == null) {
            FavoriteFolder defaultFolder = favoriteFolderService.getOrCreateDefaultFolder(favorite.getUserId());
            folderId = defaultFolder.getId();
        }
        try {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, favorite.getUserId());
            wrapper.eq(Favorite::getWorkId, favorite.getWorkId());
            wrapper.eq(Favorite::getFolderId, folderId);
            Favorite exist = favoriteService.getOne(wrapper);
            if (exist != null) {
                return Result.success(true);
            }
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(favorite.getUserId());
            newFavorite.setWorkId(favorite.getWorkId());
            newFavorite.setFolderId(folderId);
            boolean saved = favoriteService.save(newFavorite);
            if (saved) {
                LambdaQueryWrapper<Favorite> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(Favorite::getUserId, favorite.getUserId());
                countWrapper.eq(Favorite::getWorkId, favorite.getWorkId());
                if (favoriteService.count(countWrapper) == 1) {
                    workService.incrementFavoriteCount(favorite.getWorkId(), true);
                }
            }
            return Result.success(true);
        } catch (Exception e) {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, favorite.getUserId());
            wrapper.eq(Favorite::getWorkId, favorite.getWorkId());
            wrapper.eq(Favorite::getFolderId, folderId);
            if (favoriteService.count(wrapper) > 0) {
                return Result.success(true);
            }
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    @DeleteMapping
    @Transactional
    public Result<Boolean> removeFavorite(@RequestParam Long userId, @RequestParam Long workId,
                                          @RequestParam(required = false) Long folderId) {
        if (userId == null || workId == null) {
            return Result.error("参数不完整");
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getWorkId, workId);
        if (folderId != null) {
            wrapper.eq(Favorite::getFolderId, folderId);
        }
        List<Favorite> favorites = favoriteService.list(wrapper);
        if (favorites.isEmpty()) {
            return Result.success(true);
        }
        boolean removed = favoriteService.remove(wrapper);
        if (removed) {
            LambdaQueryWrapper<Favorite> remainWrapper = new LambdaQueryWrapper<>();
            remainWrapper.eq(Favorite::getUserId, userId);
            remainWrapper.eq(Favorite::getWorkId, workId);
            if (favoriteService.count(remainWrapper) == 0) {
                workService.incrementFavoriteCount(workId, false);
            }
        }
        return Result.success(removed);
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> checkFavorite(@RequestParam Long userId, @RequestParam Long workId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getWorkId, workId);
        List<Favorite> favorites = favoriteService.list(wrapper);
        boolean favorited = !favorites.isEmpty();
        List<Long> folderIds = favorites.stream().map(Favorite::getFolderId).collect(Collectors.toList());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("favorited", favorited);
        result.put("folderIds", folderIds);
        return Result.success(result);
    }

    @PostMapping("/move")
    @Transactional
    public Result<Boolean> moveFavorite(@RequestParam Long userId, @RequestParam Long workId,
                                        @RequestParam Long fromFolderId, @RequestParam Long toFolderId) {
        if (userId == null || workId == null || fromFolderId == null || toFolderId == null) {
            return Result.error("参数不完整");
        }
        LambdaQueryWrapper<Favorite> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Favorite::getUserId, userId);
        checkWrapper.eq(Favorite::getWorkId, workId);
        checkWrapper.eq(Favorite::getFolderId, toFolderId);
        if (favoriteService.count(checkWrapper) > 0) {
            LambdaQueryWrapper<Favorite> removeWrapper = new LambdaQueryWrapper<>();
            removeWrapper.eq(Favorite::getUserId, userId);
            removeWrapper.eq(Favorite::getWorkId, workId);
            removeWrapper.eq(Favorite::getFolderId, fromFolderId);
            favoriteService.remove(removeWrapper);
            return Result.success(true);
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getWorkId, workId);
        wrapper.eq(Favorite::getFolderId, fromFolderId);
        Favorite favorite = favoriteService.getOne(wrapper);
        if (favorite == null) {
            return Result.error("收藏记录不存在");
        }
        favorite.setFolderId(toFolderId);
        favoriteService.updateById(favorite);
        return Result.success(true);
    }
}
