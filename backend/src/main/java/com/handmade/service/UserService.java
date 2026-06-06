package com.handmade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.User;

public interface UserService extends IService<User> {
    User login(String username, String password);
}
