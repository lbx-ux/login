package com.fjq.login.service;

import com.fjq.login.pojo.dao.User;
import com.fjq.login.pojo.dto.UserDTO;
import com.fjq.login.pojo.dto.UserLoginDTO;

public interface UserService {
    void sendCode(String email);

    void register(UserDTO userDTO);

    User login(UserLoginDTO userLoginDTO);

    User me(String email);
}
