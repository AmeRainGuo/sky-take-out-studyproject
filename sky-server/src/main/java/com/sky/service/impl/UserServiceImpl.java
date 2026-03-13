package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信登录的url
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        log.info("微信登录，用户信息：{}", userLoginDTO);
        String openid = getOpenid(userLoginDTO);
        if(openid == null){
            log.info("微信登录失败，openid为空");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户，如果是新用户，自动完成注册
        User user = userMapper.getByOpenid(openid);

        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户信息
        return user;
    }

    private String getOpenid(UserLoginDTO userLoginDTO) {
        //调用微信接口，获取微信用户的openid
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);

        log.info("微信登录接口返回数据：{}", json);
        //将json数据转换为JSONObject对象
        JSONObject jsonObject = JSON.parseObject(json);
        //从JSONObject对象中获取openid JSONObject对象可以通过键名获取对应的值
        String openid = jsonObject.getString("openid");
        //判断Openid是否存在，如果为空，则说明登录失败
        return openid;
    }
}
