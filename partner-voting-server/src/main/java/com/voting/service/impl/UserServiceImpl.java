package com.voting.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.voting.constant.MessageConstant;
import com.voting.constant.StatusConstant;
import com.voting.dto.UserBindDTO;
import com.voting.dto.UserLoginDTO;
import com.voting.dto.UserPreLoginDTO;
import com.voting.dto.UserWxLoginDTO;
import com.voting.entity.SysUser;
import com.voting.exception.AccountLockedException;
import com.voting.exception.LoginFailedException;
import com.voting.exception.PasswordErrorException;
import com.voting.mapper.SysUserMapper;
import com.voting.mapper.UserMapper;
import com.voting.properties.WeChatProperties;
import com.voting.service.UserService;
import com.voting.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

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
    
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Deprecated
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

    @Override
    @Transactional
    public SysUser bindUser(UserBindDTO userBindDTO, Long currentUserId) {
        log.info("用户绑定，姓名: {}, 手机号: {}, 当前用户ID: {}", 
                userBindDTO.getName(), userBindDTO.getPhone(), currentUserId);
        
        // 1. 根据姓名和手机号查询目标用户
        SysUser targetUser = userMapper.getByNameAndPhone(userBindDTO.getName(), userBindDTO.getPhone());
        if (targetUser == null) {
            log.warn("未找到姓名和手机号对应的用户");
            throw new LoginFailedException(MessageConstant.USER_NOT_FOUND_BY_NAME_PHONE);
        }
        
        // 2. 检查目标用户是否已经绑定微信
        if (targetUser.getOpenid() != null && !targetUser.getOpenid().isEmpty()) {
            log.warn("目标用户已绑定微信，用户ID: {}", targetUser.getId());
            throw new LoginFailedException(MessageConstant.USER_ALREADY_BOUND_WECHAT);
        }
        
        // 3. 获取当前微信用户信息
        SysUser currentUser = userMapper.getById(currentUserId);
        if (currentUser == null) {
            log.error("当前用户不存在，用户ID: {}", currentUserId);
            throw new LoginFailedException(MessageConstant.USER_NOT_FOUND);
        }
        
        // 4. 将微信信息填入目标用户
        targetUser.setOpenid(currentUser.getOpenid());
        targetUser.setAvatar(currentUser.getAvatar());
        targetUser.setSex(currentUser.getSex());
        targetUser.setLastLoginTime(LocalDateTime.now());
        userMapper.update(targetUser);
        log.info("目标用户绑定微信成功，用户ID: {}", targetUser.getId());
        
        // 5. 删除临时微信用户记录
        userMapper.deleteById(currentUserId);
        log.info("删除临时微信用户，用户ID: {}", currentUserId);
        
        return targetUser;
    }

    @Override
    public SysUser preLogin(UserPreLoginDTO userPreLoginDTO) {
        log.info("用户预登录，用户名: {}", userPreLoginDTO.getUsername());
        
        // 1. 根据用户名查询用户
        SysUser user = sysUserMapper.getByUsername(userPreLoginDTO.getUsername());
        if (user == null) {
            log.warn("用户名不存在: {}", userPreLoginDTO.getUsername());
            throw new LoginFailedException(MessageConstant.USER_NOT_FOUND_BY_USERNAME);
        }
        
        // 2. 验证密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(userPreLoginDTO.getPassword().getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            log.warn("密码错误，用户名: {}", userPreLoginDTO.getUsername());
            throw new PasswordErrorException(MessageConstant.USER_PASSWORD_ERROR);
        }
        
        // 3. 检查账号状态
        if (StatusConstant.DISABLE.equals(user.getStatus())) {
            log.warn("账号已被锁定，用户ID: {}", user.getId());
            throw new AccountLockedException(MessageConstant.USER_ACCOUNT_LOCKED);
        }
        
        log.info("用户预登录成功，用户ID: {}, 姓名: {}", user.getId(), user.getName());
        return user;
    }

    @Override
    @Transactional
    public SysUser wxLoginNew(UserWxLoginDTO userWxLoginDTO) {
        log.info("用户微信登录，用户ID: {}", userWxLoginDTO.getUserId());
        
        // 1. 调用微信接口获取openid
        String openid = getOpenId(userWxLoginDTO.getCode());
        if (openid == null || openid.isEmpty()) {
            log.error("获取微信openid失败");
            throw new LoginFailedException(MessageConstant.WECHAT_LOGIN_FAILED);
        }
        log.info("获取到的openid: {}", openid);
        
        // 2. 检查openid是否已被其他用户绑定
        SysUser existUser = userMapper.getByOpenId(openid);
        if (existUser != null && !existUser.getId().equals(userWxLoginDTO.getUserId())) {
            log.warn("该微信已绑定其他用户，openid: {}, 已绑定用户ID: {}", openid, existUser.getId());
            throw new LoginFailedException("该微信已绑定其他账号");
        }
        
        // 3. 查询目标用户
        SysUser user = sysUserMapper.getById(userWxLoginDTO.getUserId());
        if (user == null) {
            log.error("用户不存在，用户ID: {}", userWxLoginDTO.getUserId());
            throw new LoginFailedException(MessageConstant.USER_NOT_FOUND);
        }
        
        // 4. 更新用户的openid和最后登录时间
        user.setOpenid(openid);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.update(user);
        log.info("用户微信登录成功，用户ID: {}, openid: {}", user.getId(), openid);
        
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
