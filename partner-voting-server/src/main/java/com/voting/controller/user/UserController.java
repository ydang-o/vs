package com.voting.controller.user;

import com.voting.constant.JwtClaimsConstant;
import com.voting.context.BaseContext;
import com.voting.dto.UserBindDTO;
import com.voting.dto.UserLoginDTO;
import com.voting.dto.UserPreLoginDTO;
import com.voting.dto.UserWxLoginDTO;
import com.voting.entity.SysUser;
import com.voting.properties.JwtProperties;
import com.voting.result.Result;
import com.voting.service.UserService;
import com.voting.utils.JwtUtil;
import com.voting.vo.UserLoginVO;
import com.voting.vo.UserPreLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation("微信登录（旧版，已废弃）")
    @Deprecated
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信授权登录:{}", userLoginDTO.getCode());
        //微信登录
        SysUser user = userService.wxLogin(userLoginDTO);
        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    @PostMapping("/preLogin")
    @ApiOperation("用户预登录（用户名密码验证）")
    public Result<UserPreLoginVO> preLogin(@RequestBody UserPreLoginDTO userPreLoginDTO) {
        log.info("用户预登录，用户名: {}", userPreLoginDTO.getUsername());
        
        // 验证用户名和密码
        SysUser user = userService.preLogin(userPreLoginDTO);
        
        // 返回用户基本信息（不包含token）
        UserPreLoginVO vo = UserPreLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .sex(user.getSex())
                .build();
        
        return Result.success(vo);
    }

    @PostMapping("/wxLogin")
    @ApiOperation("微信登录（新版）")
    public Result<UserLoginVO> wxLogin(@RequestBody UserWxLoginDTO userWxLoginDTO) {
        log.info("用户微信登录，用户ID: {}", userWxLoginDTO.getUserId());
        
        // 微信登录，更新openid
        SysUser user = userService.wxLoginNew(userWxLoginDTO);
        
        // 生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        
        return Result.success(userLoginVO);
    }

    @PostMapping("/bind")
    @ApiOperation("绑定用户（旧版，已废弃）")
    @Deprecated
    public Result<UserLoginVO> bindUser(@RequestBody UserBindDTO userBindDTO) {
        log.info("用户绑定，姓名: {}, 手机号: {}", userBindDTO.getName(), userBindDTO.getPhone());
        
        // 获取当前微信登录用户ID
        Long currentUserId = BaseContext.getCurrentId();
        
        // 绑定用户
        SysUser user = userService.bindUser(userBindDTO, currentUserId);
        
        // 生成新的jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        
        return Result.success(userLoginVO);
    }
}
