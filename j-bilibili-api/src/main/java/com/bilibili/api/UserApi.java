package com.bilibili.api;

import com.alibaba.fastjson.JSONObject;
import com.bilibili.domain.*;
import com.bilibili.service.UserFollowingService;
import com.bilibili.service.UserService;
import com.bilibili.service.util.RSAUtil;
import com.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserFollowingService userFollowingService;

    // 获取用户信息
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo() {
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    // 获取密钥
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPulicKey(){
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    // 创建用户
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return JsonResponse.success();
    }

    // 登录
    @PostMapping("/user-tokens")
    public JsonResponse<String> Login(@RequestBody User user) throws Exception {
       String token =  userService.login(user);
       return new JsonResponse<>(token);
    }

    // 更新用户
    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }

    // 更新用户信息
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

    // 获取用户信息列表
    @GetMapping("/user-infos")
    public JsonResponse<PageResult<UserInfo>> pageListUserInfos(@RequestParam Integer no, @RequestParam Integer size, String nick) {
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();
        params.put("no", no);
        params.put("size", size);
        params.put("nick", nick);
        params.put("userId", userId);

        PageResult<UserInfo> result = userService.pageListUserInfos(params);
        if(result.getTotal() > 0) {
             List<UserInfo> checkUserInfoList = userFollowingService.checkFollowing(result.getList(), userId);
             result.setList(checkUserInfoList);
        }

        return new JsonResponse<>(result);
    }

}
