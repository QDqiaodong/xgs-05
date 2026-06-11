package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.FavoriteFolder;
import com.handmade.mapper.FavoriteFolderMapper;
import com.handmade.service.FavoriteFolderService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteFolderServiceImpl extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder> implements FavoriteFolderService {

    @Override
    public FavoriteFolder getOrCreateDefaultFolder(Long userId) {
        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoriteFolder::getUserId, userId);
        wrapper.eq(FavoriteFolder::getIsDefault, 1);
        wrapper.orderByAsc(FavoriteFolder::getId);
        wrapper.last("LIMIT 1");
        FavoriteFolder defaultFolder = this.getOne(wrapper);
        if (defaultFolder != null) {
            return defaultFolder;
        }
        defaultFolder = new FavoriteFolder();
        defaultFolder.setUserId(userId);
        defaultFolder.setName("默认收藏夹");
        defaultFolder.setDescription("系统默认收藏夹");
        defaultFolder.setIsDefault(1);
        defaultFolder.setSort(0);
        this.save(defaultFolder);
        return defaultFolder;
    }
}
