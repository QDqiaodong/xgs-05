package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.CreatorVerification;

import java.util.Map;

public interface CreatorVerificationService extends IService<CreatorVerification> {
    Long submitApplication(CreatorVerification verification);

    CreatorVerification getLatestByUserId(Long userId);

    IPage<Map<String, Object>> getApplicationList(Integer page, Integer size, Integer status);

    Map<String, Object> getApplicationDetail(Long id);

    boolean approveApplication(Long id, Long reviewerId, String remark);

    boolean rejectApplication(Long id, Long reviewerId, String remark);
}
