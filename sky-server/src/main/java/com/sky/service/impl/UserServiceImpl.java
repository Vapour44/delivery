package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    public static final String wx_login ="https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        Map<String,String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        //调用微信接口服务，获得openid
        String json = HttpClientUtil.doGet(wx_login, map);
        JSONObject jsonObject = JSONObject.parseObject(json);
        String openid = jsonObject.getString("openid");

        //判断openid是否为空
        if(openid==null){
            throw new LoginFailedException("登录失败");
        }
        User user = userMapper.findByOpenid(openid);

        if(user==null) {
            //如果用户不存在，则需要注册
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;


    }
}
