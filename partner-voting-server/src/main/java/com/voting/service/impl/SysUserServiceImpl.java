package com.voting.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.voting.constant.MessageConstant;
import com.voting.constant.StatusConstant;
import com.voting.dto.PasswordEditDTO;
import com.voting.dto.SysUserCreateDTO;
import com.voting.dto.SysUserLoginDTO;
import com.voting.dto.SysUserPageQueryDTO;
import com.voting.dto.SysUserResetPasswordDTO;
import com.voting.dto.SysUserUpdateDTO;
import com.voting.entity.SysUser;
import com.voting.exception.BaseException;
import com.voting.exception.AccountLockedException;
import com.voting.exception.AccountNotFoundException;
import com.voting.exception.PasswordErrorException;
import com.voting.mapper.PartnerMapper;
import com.voting.mapper.SysUserMapper;
import com.voting.result.PageResult;
import com.voting.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PartnerMapper partnerMapper;

    @Override
    public SysUser login(SysUserLoginDTO loginDTO) {
        SysUser sysUser = sysUserMapper.getByUsername(loginDTO.getUsername());
        if (sysUser == null) {
            throw new AccountNotFoundException(com.voting.constant.MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String encrypted = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encrypted.equals(sysUser.getPassword())) {
            throw new PasswordErrorException(com.voting.constant.MessageConstant.PASSWORD_ERROR);
        }

        if (StatusConstant.DISABLE.equals(sysUser.getStatus())) {
            throw new AccountLockedException(com.voting.constant.MessageConstant.ACCOUNT_LOCKED);
        }

        sysUserMapper.updateLastLoginTime(sysUser.getId(), LocalDateTime.now());
        return sysUser;
    }

    @Override
    public void add(SysUserCreateDTO sysUserCreateDTO) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserCreateDTO, sysUser);
        sysUser.setStatus(StatusConstant.ENABLE);
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setPassword(DigestUtils.md5DigestAsHex(sysUserCreateDTO.getPassword().getBytes()));
        sysUserMapper.insert(sysUser);
    }

    @Override
    public PageResult pageQuery(SysUserPageQueryDTO sysUserPageQueryDTO) {
        PageHelper.startPage(sysUserPageQueryDTO.getPageNum(), sysUserPageQueryDTO.getPageSize());
        Page<SysUser> page = sysUserMapper.pageQuery(sysUserPageQueryDTO);
        long total = page.getTotal();
        List<SysUser> records = page.getResult();
        return new PageResult(total, records);
    }

    @Override
    public void update(SysUserUpdateDTO sysUserUpdateDTO) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserUpdateDTO, sysUser);
        sysUserMapper.update(sysUser);
    }

    @Override
    public void resetPassword(SysUserResetPasswordDTO resetPasswordDTO) {
        String encodedPassword = DigestUtils.md5DigestAsHex(resetPasswordDTO.getNewPassword().getBytes());
        sysUserMapper.updatePassword(resetPasswordDTO.getUserId(), encodedPassword);
    }

    @Override
    public void changePassword(PasswordEditDTO passwordEditDTO) {
        Long userId = passwordEditDTO.getEmpId();
        // 如果未传则使用当前登录用户
        if (userId == null) {
            userId = com.voting.context.BaseContext.getCurrentId();
        }
        SysUser user = sysUserMapper.getById(userId);
        if (user == null) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        String oldEncoded = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldEncoded.equals(user.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        String newEncoded = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());
        sysUserMapper.updatePassword(userId, newEncoded);
    }

    @Override
    public void delete(Long id) {
        int related = partnerMapper.countByUserId(id);
        if (related > 0) {
            throw new BaseException("当前用户已关联合伙人，无法删除");
        }
        sysUserMapper.deleteById(id);
    }
}
