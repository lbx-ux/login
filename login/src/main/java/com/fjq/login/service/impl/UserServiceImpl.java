package com.fjq.login.service.impl;

import com.fjq.login.common.exception.BusinessException;
import com.fjq.login.mapper.UserMapper;
import com.fjq.login.pojo.dao.User;
import com.fjq.login.pojo.dto.UserDTO;
import com.fjq.login.pojo.dto.UserLoginDTO;
import com.fjq.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    // Redis中存储验证码的 Key 前缀
    private static final String CODE_PREFIX = "login:code:";


    @Override
    public void sendCode(String email) {
        User user=userMapper.selectByEmail(email);
        if(user!=null){
            throw new BusinessException(409,"该邮箱已被注册");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, 5, TimeUnit.MINUTES);
        mailService.sendMail(email, code);
    }

    @Override
    public void register(UserDTO userDTO) {
        User user=userMapper.selectByEmail(userDTO.getEmail());
        if(user!=null){
            throw new BusinessException(409,"该邮箱已被注册");
        }
        //从Redis中获取验证码
        String cachedCode = redisTemplate.opsForValue().get(CODE_PREFIX + userDTO.getEmail());
        if(cachedCode==null||!cachedCode.equals(userDTO.getCode())){
            throw new BusinessException(400,"验证码错误或已过期");
        }
        // 登录成功后，主动删除 Redis 中的验证码，防止重复使用
        redisTemplate.delete(CODE_PREFIX + userDTO.getEmail());
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User newUser = new User();
        BeanUtils.copyProperties(userDTO, newUser);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        userMapper.register(newUser);
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        User user=userMapper.selectByEmail(userLoginDTO.getEmail());
        if(user==null){
            throw new BusinessException(401,"当前邮箱尚未注册账号");
        }
        String encodePassword=user.getPassword();
        if(!passwordEncoder.matches(userLoginDTO.getPassword(),encodePassword)){
            throw new BusinessException(401,"密码输入错误");
        }

        return user;
    }

    @Override
    public User me(String email) {
        return userMapper.selectByEmail(email);
    }

}
