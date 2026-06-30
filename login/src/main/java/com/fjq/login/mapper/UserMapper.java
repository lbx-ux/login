package com.fjq.login.mapper;

import com.fjq.login.pojo.dao.User;
import com.fjq.login.pojo.dto.UserDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where email=#{email}")
    User selectByEmail(String email);


    @Insert("insert into user (username, email, password,created_at,updated_at) VALUES (#{username},#{email},#{password},#{createdAt},#{updatedAt})")
    void register(User newUser);
}
