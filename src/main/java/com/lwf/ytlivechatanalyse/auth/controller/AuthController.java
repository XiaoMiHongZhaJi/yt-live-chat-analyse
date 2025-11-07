package com.lwf.ytlivechatanalyse.auth.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.auth.bean.AuthPrincipal;
import com.lwf.ytlivechatanalyse.auth.service.JwtService;
import com.lwf.ytlivechatanalyse.auth.service.UserService;
import com.lwf.ytlivechatanalyse.auth.bean.UserEntity;
import com.lwf.ytlivechatanalyse.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;
    private final Random random = new Random();

    @RequestMapping("/queryList")
    @PreAuthorize("principal.userName == 'admin'")
    public Result<UserEntity> queryList(int limit, int page) {
        PageHelper.startPage(page, limit);
        return new Result<>(new PageInfo<>(userService.selectList()));
    }

    @PostMapping("/createUser")
    @PreAuthorize("principal.userName == 'admin'")
    public Result<String> createUser(Authentication authentication, String userName, String password) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return new Result<>(401, "用户验证失败");
        }
        AuthPrincipal authPrincipal = (AuthPrincipal) authentication.getPrincipal();
        if (!"admin".equals(authPrincipal.getUserName())) {
            return new Result<>(401, "管理员验证失败");
        }
        if (StringUtils.isBlank(userName)){
            return new Result<>(500, "用户名不能为空");
        }
        UserEntity exist = userService.findByUserName(userName);
        if (exist != null) {
            return new Result<>(500, "用户名已存在");
        }
        if (StringUtils.isBlank(password)) {
            password = String.valueOf(10000000 + random.nextInt(90000000));
        }
        userService.createUser(userName, password);
        return new Result<>(200, password);
    }

    @PostMapping("/updateUserInfo")
    @PreAuthorize("principal.userName == 'admin'")
    public Result<String> updateUserInfo(Authentication authentication, UserEntity userEntity) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return new Result<>(401, "用户验证失败");
        }
        AuthPrincipal authPrincipal = (AuthPrincipal) authentication.getPrincipal();
        String userId = authPrincipal.getUserId();
        userService.updateUserInfo(userEntity);
        return new Result<>(200, "success");
    }

    @PostMapping("/login")
    public Result<String> login(String userName, String password) {
        if (StringUtils.isAnyBlank(userName, password)) {
            return new Result<>(401, "用户名或密码不能为空");
        }
        UserEntity userEntity = userService.findByUserName(userName);
        if (userEntity == null) {
            return new Result<>(401, "用户名或密码错误");
        }
        if (!userService.checkPassword(userEntity, password)) {
            return new Result<>(401, "用户名或密码错误");
        }
        String token = jwtService.generateToken(userEntity.getUserId(), userEntity.getUserName());
        userService.updateLoginTime(userEntity.getId());
        return new Result<>(200, token);
    }

    @PostMapping("/changePassword")
    public Result<String> changePassword(Authentication authentication, String newPassword) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return new Result<>(401, "用户验证失败");
        }
        AuthPrincipal authPrincipal = (AuthPrincipal) authentication.getPrincipal();
        String userId = authPrincipal.getUserId();
        if (StringUtils.isBlank(newPassword)) {
            return new Result<>(500, "新密码不能为空");
        }
        userService.changePassword(userId, newPassword);
        return new Result<>(200, "success");
    }
}
