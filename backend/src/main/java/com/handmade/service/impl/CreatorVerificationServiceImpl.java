package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.CreatorVerification;
import com.handmade.entity.User;
import com.handmade.mapper.CreatorVerificationMapper;
import com.handmade.service.CreatorVerificationService;
import com.handmade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CreatorVerificationServiceImpl extends ServiceImpl<CreatorVerificationMapper, CreatorVerification> implements CreatorVerificationService {

    @Autowired
    private UserService userService;

    @Override
    public Long submitApplication(CreatorVerification verification) {
        verification.setStatus(0);
        verification.setReviewTime(null);
        verification.setReviewerId(null);
        verification.setReviewRemark(null);
        this.save(verification);
        return verification.getId();
    }

    @Override
    public CreatorVerification getLatestByUserId(Long userId) {
        LambdaQueryWrapper<CreatorVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreatorVerification::getUserId, userId);
        wrapper.orderByDesc(CreatorVerification::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public IPage<Map<String, Object>> getApplicationList(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<CreatorVerification> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CreatorVerification::getStatus, status);
        }
        wrapper.orderByDesc(CreatorVerification::getCreateTime);
        IPage<CreatorVerification> verificationPage = this.page(new Page<>(page, size), wrapper);

        Page<Map<String, Object>> result = new Page<>(verificationPage.getCurrent(), verificationPage.getSize());
        result.setTotal(verificationPage.getTotal());

        List<Long> userIds = verificationPage.getRecords().stream()
                .map(CreatorVerification::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            for (User u : users) {
                u.setPassword(null);
                userMap.put(u.getId(), u);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (CreatorVerification v : verificationPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("verification", v);
            item.put("user", userMap.get(v.getUserId()));
            records.add(item);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public Map<String, Object> getApplicationDetail(Long id) {
        CreatorVerification verification = this.getById(id);
        if (verification == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("verification", verification);
        User user = userService.getById(verification.getUserId());
        if (user != null) {
            user.setPassword(null);
        }
        result.put("user", user);
        return result;
    }

    @Override
    @Transactional
    public boolean approveApplication(Long id, Long reviewerId, String remark) {
        CreatorVerification verification = this.getById(id);
        if (verification == null) {
            return false;
        }

        CreatorVerification update = new CreatorVerification();
        update.setId(id);
        update.setStatus(1);
        update.setReviewerId(reviewerId);
        update.setReviewRemark(remark);
        update.setReviewTime(LocalDateTime.now());
        boolean result = this.updateById(update);

        if (result && verification.getUserId() != null) {
            User user = new User();
            user.setId(verification.getUserId());
            user.setIsCertified(1);
            user.setCertifiedTime(LocalDateTime.now());
            userService.updateById(user);
        }

        return result;
    }

    @Override
    public boolean rejectApplication(Long id, Long reviewerId, String remark) {
        CreatorVerification update = new CreatorVerification();
        update.setId(id);
        update.setStatus(2);
        update.setReviewerId(reviewerId);
        update.setReviewRemark(remark);
        update.setReviewTime(LocalDateTime.now());
        return this.updateById(update);
    }
}
