package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.User;
import com.handmade.entity.Work;
import com.handmade.mapper.UserMapper;
import com.handmade.service.UserService;
import com.handmade.service.WorkService;
import com.handmade.strategy.CreatorLevelCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private CreatorLevelCalculator creatorLevelCalculator;

    @Autowired
    private WorkService workService;

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getPassword, password);
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateCreatorStats(Long userId) {
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getUserId, userId);
        wrapper.eq(Work::getStatus, 1);
        List<Work> works = workService.list(wrapper);

        int totalWorkCount = works.size();
        int totalViewCount = works.stream().mapToInt(w -> w.getViewCount() != null ? w.getViewCount() : 0).sum();
        int totalFavoriteCount = works.stream().mapToInt(w -> w.getFavoriteCount() != null ? w.getFavoriteCount() : 0).sum();
        int totalLikeCount = works.stream().mapToInt(w -> w.getLikeCount() != null ? w.getLikeCount() : 0).sum();

        int score = creatorLevelCalculator.calculateScore(totalWorkCount, totalViewCount, totalFavoriteCount, totalLikeCount);
        int level = creatorLevelCalculator.determineLevel(score);

        User user = new User();
        user.setId(userId);
        user.setTotalWorkCount(totalWorkCount);
        user.setTotalViewCount(totalViewCount);
        user.setTotalFavoriteCount(totalFavoriteCount);
        user.setTotalLikeCount(totalLikeCount);
        user.setCreatorLevel(level);

        return this.updateById(user);
    }

    @Override
    public User getUserWithStats(Long id) {
        User user = this.getById(id);
        if (user != null) {
            updateCreatorStats(id);
            user = this.getById(id);
        }
        return user;
    }
}
