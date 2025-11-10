package com.lwf.ytlivechatanalyse.auth.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.auth.bean.AuthPrincipal;
import com.lwf.ytlivechatanalyse.auth.filter.LoggingFilter;
import com.lwf.ytlivechatanalyse.auth.service.JwtService;
import com.lwf.ytlivechatanalyse.auth.service.UserService;
import com.lwf.ytlivechatanalyse.auth.bean.UserEntity;
import com.lwf.ytlivechatanalyse.util.CaptchaUtil;
import com.lwf.ytlivechatanalyse.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;
    private final Random random = new Random();
    
    private static final Map<String, AtomicInteger> loginFailMap = new ConcurrentHashMap<>();

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

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha(HttpServletRequest request) {
        String code = CaptchaUtil.generateCode(4);
        String base64Image = CaptchaUtil.generateBase64Image(code, 100, 40);

        HttpSession session = request.getSession();
        session.setAttribute("captcha", code.toLowerCase());

        return new Result<>(200, base64Image);
    }

    @PostMapping("/login")
    public Result<String> login(String userName, String password, String captcha, HttpServletRequest request) {
        String ip = LoggingFilter.getIpAddress(request);
        AtomicInteger failCount = loginFailMap.computeIfAbsent(ip, k -> new AtomicInteger(0));

        if (failCount.get() > 2) {
            String sessionCaptcha = (String) request.getSession().getAttribute("captcha");
            if (StringUtils.isBlank(captcha)) {
                return new Result<>(401, "请输入验证码", failCount.get());
            }
            if (!captcha.equalsIgnoreCase(sessionCaptcha)) {
                request.getSession().setAttribute("captcha", "");
                return new Result<>(401, "验证码错误", failCount.get());
            }
        }

        // 1. 基本校验
        if (StringUtils.isAnyBlank(userName, password)) {
            failCount.incrementAndGet();
            return new Result<>(401, "用户名或密码不能为空", failCount.get());
        }

        // 2. 用户验证
        UserEntity userEntity = userService.findByUserName(userName);
        if (userEntity == null || !userService.checkPassword(userEntity, password)) {
            failCount.incrementAndGet();
            return new Result<>(401, "用户名或密码错误", failCount.get());
        }

        // 3. 登录成功清空计数
        loginFailMap.remove(ip);

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
