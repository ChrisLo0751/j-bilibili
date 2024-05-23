package com.bilibili.service;

import com.bilibili.dao.UserFollowingDao;
import com.bilibili.domain.FollowingGroup;
import com.bilibili.domain.UserFollowing;
import com.bilibili.domain.UserInfo;
import com.bilibili.domain.constant.UserConstant;
import com.bilibili.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if (groupId == null) {
            // 如果groupId为空，则获取默认的关注分组
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            // 如果groupId不为空，则根据groupId获取关注分组
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if (followingGroup == null) {
                throw new ConditionException("关注分组不存在！");
            }
        }

        Long followingId = userFollowing.getFollowingId();
        // 根据followingId获取用户信息
        if (userService.getUserById(followingId) == null) {
            throw new ConditionException("关注的用户不存在！");
        }

        // 删除已经存在的关注关系
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);

        // 添加新的关注关系
        userFollowingDao.addUserFollowing(userFollowing);
    }

    public List<FollowingGroup> getUserFollowings(Long userId) {
        // 获取关注的用户列表
        List<UserFollowing> list =  userFollowingDao.getUserFollowings(userId);

        // 获取关注用户的id集合
        Set<Long> followingSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());

        // 根据关注用户的id集合获取关注用户的基本信息
        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingSet.size() > 0) {
            userInfoList = userService.getUserInfoByUserIds(followingSet);
        }

        // 为关注用户设置基本信息
        for(UserFollowing userFollowing: list) {
            for(UserInfo userInfo: userInfoList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        // 获取关注分组
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);
        FollowingGroup allGroup = new FollowingGroup();

        // 设置全部关注分组
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);

        // 将关注用户按关注分组进行分类
        for(FollowingGroup group: groupList) {
            List<UserInfo> infoList = new ArrayList<>();
            for(UserFollowing userFollowing: list) {
                if (group.getId().equals(userFollowing.getGroupId())) {
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }

        return  result;
    }
}
