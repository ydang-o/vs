package com.voting.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.voting.constant.MessageConstant;
import com.voting.dto.UserLoginDTO;
import com.voting.entity.SysUser;
import com.voting.exception.LoginFailedException;
import com.voting.mapper.UserMapper;
import com.voting.properties.WeChatProperties;
import com.voting.service.UserService;
import com.voting.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    //微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public SysUser wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口服务获取用户的openid
        String openid = getOpenId(userLoginDTO.getCode());
        log.info("获取到的openid: {}", openid);
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户
        SysUser user = userMapper.getByOpenId(openid);
        log.info("查询到的用户: {}", user);
        //新用户,注册
        if (user == null) {
            log.info("新用户注册，openid: {}", openid);
            user = SysUser.builder()
                    .openid(openid)
                    .userType(2)  // 2=微信用户
                    .name("微信用户")  // 默认名称
                    .status(1)  // 默认正常状态
                    .createTime(LocalDateTime.now())
                    .build();
            try {
                userMapper.insert(user);
                log.info("用户注册成功，用户ID: {}", user.getId());
            } catch (Exception e) {
                log.error("用户注册失败", e);
                throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
            }
        }
        //返回用户对象
        return user;
    }

    /**
     * 调用微信接口服务,获取微信用户的openid
     *
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
            log.info("调用微信接口，appid: {}, code: {}", weChatProperties.getAppid(), code);
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信接口返回: {}", json);
        //判断openid是否为空,如果为空表示登录失败,抛出异常
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
