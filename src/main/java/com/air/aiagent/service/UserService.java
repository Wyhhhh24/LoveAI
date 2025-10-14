package com.air.aiagent.service;
import com.air.aiagent.domain.dto.AddUserRequest;
import com.air.aiagent.domain.dto.UserLoginRequest;
import com.air.aiagent.domain.entity.User;
import com.air.aiagent.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author 30280
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-09-12 12:44:39
*/
public interface UserService extends IService<User> {

    /**
     * 脱敏，Entity 转换为 VO
     */
    UserVO entityToVO(User user);

    /**
     * 随机获取用户昵称
     */
    String getRandomNickName();

    /**
     * 用户登录
     */
    UserVO login(UserLoginRequest request, HttpServletRequest httpServletRequest);

    /**
     * 用户注册
     */
    Boolean register(AddUserRequest request);

    /**
     * 发送验证码
     */
    Boolean sendEmailCode(AddUserRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);
}
