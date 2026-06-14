package com.handmade.vo;

import com.handmade.entity.ChainWork;
import com.handmade.entity.User;
import com.handmade.entity.Work;
import lombok.Data;

@Data
public class ChainWorkVO {
    private ChainWork chainWork;
    private Work work;
    private User user;
    private ChainWork parentChainWork;
    private Work parentWork;
    private User parentUser;
}
