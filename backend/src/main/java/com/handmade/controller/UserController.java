package com.handmade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.handmade.entity.User;
import com.handmade.service.UserService;
import com.handmade.strategy.CreatorLevelCalculator;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CreatorLevelCalculator creatorLevelCalculator;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        wrapper.eq(User::getPassword, user.getPassword());
        User loginUser = userService.getOne(wrapper);
        if (loginUser != null) {
            loginUser.setPassword(null);
            userService.updateCreatorStats(loginUser.getId());
            loginUser = userService.getById(loginUser.getId());
            loginUser.setPassword(null);
            String token = Base64.getEncoder().encodeToString(loginUser.getId().toString().getBytes());
            Map<String, Object> result = new HashMap<>();
            result.put("user", loginUser);
            result.put("token", token);
            return Result.success(result);
        }
        return Result.error("用户名或密码错误");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserWithStats(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/{id}/level")
    public Result<Map<String, Object>> getUserLevel(@PathVariable Long id) {
        User user = userService.getUserWithStats(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("creatorLevel", user.getCreatorLevel());
        result.put("levelName", creatorLevelCalculator.getLevelName(user.getCreatorLevel()));
        result.put("totalWorkCount", user.getTotalWorkCount());
        result.put("totalViewCount", user.getTotalViewCount());
        result.put("totalFavoriteCount", user.getTotalFavoriteCount());
        result.put("totalLikeCount", user.getTotalLikeCount());

        int score = creatorLevelCalculator.calculateScore(
                user.getTotalWorkCount() != null ? user.getTotalWorkCount() : 0,
                user.getTotalViewCount() != null ? user.getTotalViewCount() : 0,
                user.getTotalFavoriteCount() != null ? user.getTotalFavoriteCount() : 0,
                user.getTotalLikeCount() != null ? user.getTotalLikeCount() : 0
        );
        result.put("score", score);

        int nextLevelScore = creatorLevelCalculator.getNextLevelScore(user.getCreatorLevel());
        result.put("nextLevelScore", nextLevelScore);
        result.put("nextLevelName", creatorLevelCalculator.getLevelName(user.getCreatorLevel() + 1));

        int currentLevelMinScore;
        switch (user.getCreatorLevel()) {
            case 1: currentLevelMinScore = 0; break;
            case 2: currentLevelMinScore = 100; break;
            case 3: currentLevelMinScore = 500; break;
            case 4: currentLevelMinScore = 2000; break;
            case 5: currentLevelMinScore = 5000; break;
            default: currentLevelMinScore = 0;
        }
        result.put("currentLevelMinScore", currentLevelMinScore);

        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null);
        return Result.success(userService.updateById(user));
    }
}
