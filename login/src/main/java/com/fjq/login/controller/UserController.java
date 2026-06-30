package com.fjq.login.controller;

import com.fjq.login.common.api.Result;
import com.fjq.login.common.context.BaseContext;
import com.fjq.login.common.utils.JwtUtil;
import com.fjq.login.pojo.dao.User;
import com.fjq.login.pojo.dto.EmailRequestDTO;
import com.fjq.login.pojo.dto.UserDTO;
import com.fjq.login.pojo.dto.UserLoginDTO;
import com.fjq.login.pojo.vo.UserLoginVO;
import com.fjq.login.pojo.vo.UserVO;
import com.fjq.login.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody EmailRequestDTO emailRequestDTO){
        log.info("发送验证码给: {}", emailRequestDTO.getEmail());
        userService.sendCode(emailRequestDTO.getEmail());
        return Result.success("验证码已发送");
    }
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO){
        log.info("注册用户: {}", userDTO);
        userService.register(userDTO);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("{}用户登录",userLoginDTO);
        User user=userService.login(userLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email",user.getEmail());
        claims.put("username",user.getUsername());

        String token=jwtUtil.createJWT(claims);

        UserVO userVO=UserVO.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return Result.success(userVO);
    }

    @GetMapping("/me")
    public Result<UserLoginVO> me(){
        String email=BaseContext.getCurrent();
        User user=userService.me(email);
        UserLoginVO userLoginVO=new UserLoginVO(user.getUsername(),user.getEmail());
        return Result.success(userLoginVO);
    }

}
