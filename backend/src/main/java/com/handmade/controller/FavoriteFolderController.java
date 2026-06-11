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
@RequestMapping("/api/favorite-folder")
public class FavoriteFolderController {

    @Autowired
    private FavoriteFolderService favoriteFolderService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private WorkService workService;

    @GetMapping("/list/{userId}")
    public Result<List<Map<String, Object>>> getFolderList(@PathVariable Long userId) {
        favoriteFolderService.getOrCreateDefaultFolder(userId);
        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteFolder::getUserId, userId);
        wrapper.orderByAsc(FavoriteFolder::getSort);
        wrapper.orderByAsc(FavoriteFolder::getId);
        List<FavoriteFolder> folders = favoriteFolderService.list(wrapper);

        List<Long> folderIds = folders.stream().map(FavoriteFolder::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Favorite> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Favorite::getUserId, userId);
        countWrapper.in(Favorite::getFolderId, folderIds);
        List<Favorite> favorites = favoriteService.list(countWrapper);
        Map<Long, Long> folderCountMap = favorites.stream()
                .collect(Collectors.groupingBy(Favorite::getFolderId, Collectors.counting()));

        List<Map<String, Object>> result = folders.stream().map(folder -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", folder.getId());
            map.put("name", folder.getName());
            map.put("description", folder.getDescription());
            map.put("coverImage", folder.getCoverImage());
            map.put("isDefault", folder.getIsDefault());
            map.put("sort", folder.getSort());
            map.put("createTime", folder.getCreateTime());
            map.put("updateTime", folder.getUpdateTime());
            map.put("count", folderCountMap.getOrDefault(folder.getId(), 0L));
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @PostMapping
    public Result<FavoriteFolder> createFolder(@RequestBody FavoriteFolder folder) {
        if (folder.getUserId() == null || folder.getName() == null || folder.getName().trim().isEmpty()) {
            return Result.error("参数不完整");
        }
        folder.setName(folder.getName().trim());
        folder.setIsDefault(0);
        if (folder.getSort() == null) {
            folder.setSort(0);
        }
        favoriteFolderService.save(folder);
        return Result.success(folder);
    }

    @PutMapping
    public Result<FavoriteFolder> updateFolder(@RequestBody FavoriteFolder folder) {
        if (folder.getId() == null) {
            return Result.error("参数不完整");
        }
        FavoriteFolder exist = favoriteFolderService.getById(folder.getId());
        if (exist == null) {
            return Result.error("收藏夹不存在");
        }
        if (exist.getIsDefault() != null && exist.getIsDefault() == 1) {
            if (folder.getIsDefault() != null && folder.getIsDefault() == 0) {
                return Result.error("默认收藏夹不能取消默认");
            }
        }
        if (folder.getName() != null) {
            exist.setName(folder.getName().trim());
        }
        if (folder.getDescription() != null) {
            exist.setDescription(folder.getDescription());
        }
        if (folder.getCoverImage() != null) {
            exist.setCoverImage(folder.getCoverImage());
        }
        if (folder.getSort() != null) {
            exist.setSort(folder.getSort());
        }
        favoriteFolderService.updateById(exist);
        return Result.success(exist);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Boolean> deleteFolder(@PathVariable Long id) {
        FavoriteFolder folder = favoriteFolderService.getById(id);
        if (folder == null) {
            return Result.error("收藏夹不存在");
        }
        if (folder.getIsDefault() != null && folder.getIsDefault() == 1) {
            return Result.error("默认收藏夹不能删除");
        }
        Long userId = folder.getUserId();
        FavoriteFolder defaultFolder = favoriteFolderService.getOrCreateDefaultFolder(userId);
        if (!defaultFolder.getId().equals(id)) {
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId);
            wrapper.eq(Favorite::getFolderId, id);
            List<Favorite> favorites = favoriteService.list(wrapper);
            for (Favorite fav : favorites) {
                LambdaQueryWrapper<Favorite> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(Favorite::getUserId, userId);
                checkWrapper.eq(Favorite::getWorkId, fav.getWorkId());
                checkWrapper.eq(Favorite::getFolderId, defaultFolder.getId());
                if (favoriteService.count(checkWrapper) == 0) {
                    fav.setFolderId(defaultFolder.getId());
                    favoriteService.updateById(fav);
                } else {
                    favoriteService.removeById(fav.getId());
                }
            }
        }
        favoriteFolderService.removeById(id);
        return Result.success(true);
    }

    @GetMapping("/works/{folderId}")
    public Result<IPage<Work>> getFolderWorks(
            @PathVariable Long folderId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        FavoriteFolder folder = favoriteFolderService.getById(folderId);
        if (folder == null) {
            return Result.error("收藏夹不存在");
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getFolderId, folderId);
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
}
