package com.handmade.vo;

import com.handmade.entity.Activity;
import com.handmade.entity.Work;
import com.handmade.entity.User;
import lombok.Data;

@Data
public class ActivityWorkVO {
    private Long id;
    private Long activityId;
    private Long workId;
    private Long userId;
    private String submitRemark;
    private Integer voteCount;
    private Integer rankNum;
    private String submitTime;
    private Integer auditStatus;
    private String auditRemark;
    private Work work;
    private User user;
}
