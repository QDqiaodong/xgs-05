package com.handmade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.Favorite;
import com.handmade.mapper.FavoriteMapper;
import com.handmade.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}
