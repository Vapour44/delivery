package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {


    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User findByOpenid(String openid);

     void insert(User user);
    @Select("SELECT COUNT(id) FROM user WHERE create_time >= #{beginTime} AND create_time < #{endTime}")
    Integer getNewUser(LocalDateTime beginTime, LocalDateTime endTime);
    @Select("SELECT COUNT(id) FROM user WHERE create_time < #{endTime}")
    Integer getTotalUser(LocalDateTime endTime);


}
