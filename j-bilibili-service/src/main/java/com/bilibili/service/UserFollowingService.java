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
import java.util.Date;
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
        for(UserFollowing userFollowing : list) {
            for(UserInfo userInfo : userInfoList) {
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

    public List<UserFollowing> getUserFans(Long userId) {
        // 获取当前用户的粉丝列表
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);

        // 获取粉丝的用户id集合
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());

        // 根据粉丝的用户id集合获取基本信息
        List<UserInfo> userInfoList = new ArrayList<>();
        if (fanIdSet.size() > 0) {
            userInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }

        // 获取用户的关注列表
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);
        for(UserFollowing fan : fanList) {
            for(UserInfo userInfo: userInfoList) {
                if (fan.getUserId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            for(UserFollowing following: followingList) {
                if (following.getFollowingId().equals(fan.getUserId())) {
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }

        return fanList;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getIserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowing(List<UserInfo> list, Long userId) {
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        for(UserInfo userInfo : list) {
            userInfo.setFollowed(false);
            for(UserFollowing userFollowing : userFollowingList) {
                // 如果用户关注的用户id等于用户信息的用户id，则设置用户信息的关注状态为true
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(true);
                }
            }
        }

        return list;
    }
}
