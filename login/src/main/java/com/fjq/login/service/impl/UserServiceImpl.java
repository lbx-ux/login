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
    // Redis中存储发送频率限制的 Key 前缀
    private static final String LIMIT_PREFIX = "login:limit:";


    @Override
    public void sendCode(String email) {
        // =================  防刷机制（原子操作） =================
        String limitKey = LIMIT_PREFIX + email;
        /*
         * setIfAbsent 尝试设置一个 Key 并在 60 秒后过期。
         * 如果 Key 不存在：设置成功，返回 true。
         * 如果 Key 已存在：设置失败，返回 false，说明 60 秒内已经发送过。
         */
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", 60, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isAbsent)) {
            // 获取还剩多少秒解禁，让前端提示更友好（可选）
            Long expire = redisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
            long waitSeconds = (expire != null && expire > 0) ? expire : 60;
            // 抛出自定义异常，状态码可以使用 429 (Too Many Requests)
            throw new BusinessException(429, "发送过于频繁，请 " + waitSeconds + " 秒后再试");
        }


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
