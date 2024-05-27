package com.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.bilibili.dao.UserDao;
import com.bilibili.domain.PageResult;
import com.bilibili.domain.User;
import com.bilibili.domain.UserInfo;
import com.bilibili.domain.constant.UserConstant;
import com.bilibili.domain.exception.ConditionException;
import com.bilibili.service.util.MD5Util;
import com.bilibili.service.util.RSAUtil;
import com.bilibili.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public void addUser(User user) {
        String phone = user.getPhone();
        // 手机号非空判断
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空！");
        }
        // 验证手机号码是否为11位数字
        if (phone.length() != 11) {
            throw new ConditionException("手机号格式不正确！");
        }

        // 验证手机号码是否只包含数字字符
        if (!Pattern.matches("\\d+", phone)) {
            throw new ConditionException("手机号包含非法字符！");
        }

        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号已经注册！");
        }
        // 密码解密
        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败！");
        }
        // 生成md5密码
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        // 数据入库
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        userDao.addUser(user);
        // 添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if(StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)){
            throw new ConditionException("参数异常！");
        }

        User dbUser = userDao.getUserByPhone(phone);
        if(dbUser == null) {
            throw new ConditionException("当前用户不存在！");
        }

        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("解密失败！");
        }

        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("密码错误！");
        }

        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public User getUserById(Long userId) {
        return userDao.getUserById(userId);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfo(userInfo);
    }

    public void updateUsers(User user) throws Exception{
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if (dbUser == null) {
            throw new ConditionException("用户不存在！");
        }

        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }

        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> followingSet) {
        return userDao.getUserInfoByUserIds(followingSet);
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        // 起始位置
        params.put("start", (no-1)*size);
        // 每页数量
        params.put("limit", size);

        List<UserInfo> list = new ArrayList<>();
        // 计算总数
        Integer total = userDao.countUserInfos(params);
        if (total > 0) {
            // 查询用户信息列表
            list = userDao.pageListUserInfos(params);
        }

        return new PageResult<>(total, list);
    }
}
