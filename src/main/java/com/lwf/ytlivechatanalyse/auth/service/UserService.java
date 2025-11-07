package com.lwf.ytlivechatanalyse.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lwf.ytlivechatanalyse.auth.bean.UserEntity;
import com.lwf.ytlivechatanalyse.auth.dao.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserMapper userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserEntity createUser(String userName, String plainPassword) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setUserId(UUID.randomUUID().toString().substring(0, 8));
        userEntity.setPassword(encoder.encode(plainPassword));
        userRepository.insert(userEntity);
        return findByUserName(userName);
    }

    public UserEntity findByUserId(String userId) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.notLike("status", 4);
        return userRepository.selectOne(queryWrapper);
    }

    public UserEntity findByUserName(String userName) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        queryWrapper.notLike("status", 4);
        return userRepository.selectOne(queryWrapper);
    }

    public UserEntity findById(Long id) {
        return userRepository.selectById(id);
    }

    public boolean checkPassword(UserEntity user, String raw) {
        return encoder.matches(raw, user.getPassword());
    }

    public void changePassword(String userId, String newPassword) {
        UserEntity userEntity = findByUserId(userId);
        if(userEntity != null){
            userEntity.setPassword(encoder.encode(newPassword));
            userRepository.updateById(userEntity);
        }
    }

    public List<UserEntity> selectList() {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("id");
        queryWrapper.notLike("status", 4);
        queryWrapper.select("id", "user_id", "user_name", "status", "create_time", "update_time", "update_time", "last_login_time");
        return userRepository.selectList(queryWrapper);
    }

    public void updateLoginTime(Long id) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(id);
        userEntity.setLastLoginTime(new Date());
        userRepository.updateById(userEntity);
    }

    public void updateUserInfo(UserEntity userEntity) {
        userEntity.setUpdateTime(new Date());
        String password = userEntity.getPassword();
        if (StringUtils.isBlank(password)) {
            userEntity.setPassword(null);
        } else {
            userEntity.setPassword(encoder.encode(password));
        }
        userRepository.updateById(userEntity);
    }
}
