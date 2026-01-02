package com.voting.controller.admin;

import com.voting.constant.JwtClaimsConstant;
import com.voting.dto.PasswordEditDTO;
import com.voting.dto.SysUserCreateDTO;
import com.voting.dto.SysUserLoginDTO;
import com.voting.dto.SysUserPageQueryDTO;
import com.voting.dto.SysUserResetPasswordDTO;
import com.voting.dto.SysUserUpdateDTO;
import com.voting.entity.SysUser;
import com.voting.properties.JwtProperties;
import com.voting.result.PageResult;
import com.voting.result.Result;
import com.voting.service.SysUserService;
import com.voting.utils.JwtUtil;
import com.voting.vo.SysUserLoginVO;
import com.voting.vo.SysUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("adminUserController")
@RequestMapping("/admin/user")
@Api(tags = "系统用户管理接口")
@Slf4j
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation("系统用户登录")
    public Result<SysUserLoginVO> login(@RequestBody SysUserLoginDTO loginDTO) {
        log.info("系统用户登录: {}", loginDTO.getUsername());
        SysUser sysUser = sysUserService.login(loginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, sysUser.getId());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        SysUserLoginVO vo = SysUserLoginVO.builder()
                .id(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .phone(sysUser.getPhone())
                .token(token)
                .build();
        return Result.success(vo);
    }

    @PostMapping("/add")
    @ApiOperation("新增用户")
    public Result<Void> addUser(@RequestBody SysUserCreateDTO sysUserCreateDTO) {
        log.info("新增系统用户: {}", sysUserCreateDTO.getUsername());
        sysUserService.add(sysUserCreateDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("用户列表查询")
    public Result<Map<String, Object>> list(SysUserPageQueryDTO sysUserPageQueryDTO) {
        PageResult pageResult = sysUserService.pageQuery(sysUserPageQueryDTO);
        List<SysUser> records = pageResult.getRecords();
        List<SysUserVO> list = records.stream().map(item -> {
            SysUserVO vo = SysUserVO.builder().build();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("total", pageResult.getTotal());
        data.put("list", list);
        return Result.success(data);
    }

    @PutMapping("/update")
    @ApiOperation("编辑用户")
    public Result<Void> update(@RequestBody SysUserUpdateDTO sysUserUpdateDTO) {
        log.info("编辑系统用户: {}", sysUserUpdateDTO.getId());
        sysUserService.update(sysUserUpdateDTO);
        return Result.success();
    }

    @PutMapping("/resetPassword")
    @ApiOperation("重置用户密码")
    public Result<Void> resetPassword(@RequestBody SysUserResetPasswordDTO resetPasswordDTO) {
        log.info("重置用户密码: {}", resetPasswordDTO.getUserId());
        sysUserService.resetPassword(resetPasswordDTO);
        return Result.success();
    }

    @PutMapping("/changePassword")
    @ApiOperation("用户修改密码（校验旧密码）")
    public Result<Void> changePassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("用户修改密码: {}", passwordEditDTO.getEmpId());
        sysUserService.changePassword(passwordEditDTO);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除系统用户: {}", id);
        sysUserService.delete(id);
        return Result.success();
    }
}
