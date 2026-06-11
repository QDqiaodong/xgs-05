package com.handmade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.FavoriteFolder;

public interface FavoriteFolderService extends IService<FavoriteFolder> {
    FavoriteFolder getOrCreateDefaultFolder(Long userId);
}
