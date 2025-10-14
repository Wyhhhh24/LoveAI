package com.air.aiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.air.aiagent.Enum.VerificationCodeTypeEnum;
import com.air.aiagent.domain.dto.AddUserRequest;
import com.air.aiagent.domain.dto.UserLoginRequest;
import com.air.aiagent.domain.vo.UserVO;
import com.air.aiagent.exception.BusinessException;
import com.air.aiagent.exception.ErrorCode;
import com.air.aiagent.utils.MailUtils;
import com.air.aiagent.utils.ValidationUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.air.aiagent.domain.entity.User;
import com.air.aiagent.service.UserService;
import com.air.aiagent.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.air.aiagent.constant.Constant.*;

/**
* @author 30280
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-09-12 12:44:39
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AsyncTaskService asyncTaskService;

    @Resource
    private MailUtils mailUtils;

    /**
     * 用户登录
     */
    @Override
    public UserVO login(UserLoginRequest request, HttpServletRequest httpServletRequest) {
        // 1.判断 QQ 邮箱是否为空
        if(StrUtil.isBlank(request.getQqEmail())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        // 2.通过 QQ 邮箱，判断用户是否存在
        User user = null;
        user = getOne(new LambdaQueryWrapper<User>().eq(User::getQqEmail, request.getQqEmail()));
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在，请前往注册");
        }
        // 3.密码登录、验证码登录，进行校验
        if(StrUtil.isNotBlank(request.getPassword())){
            // 3.1.判断用户是否设置密码
            if(StrUtil.isBlank(user.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"该账户暂未设置密码，请通过验证码登录");
            }
            // 3.2.密码登录
            if(!request.getPassword().equals(user.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }else{
            // 3.3.验证码登录                                                               redisKey:"loveai:verificationCode:1:3028077121@qq.com"
            if(!request.getVerificationCode().equals(stringRedisTemplate.opsForValue().get(VERIFICATIONCODE + VerificationCodeTypeEnum.LOGIN.getValue() +
                    ":" + request.getQqEmail()))){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
            }
        }
        // 4.登录成功，存储登录态到 session(存到 Redis ) 中
        httpServletRequest.getSession().setAttribute(LOGIN_USER, user);

        // 5.返回脱敏的用户信息
        return entityToVO(user);
    }

    /**
     * 用户注册
     */
    @Override
    public Boolean register(AddUserRequest request) {
        // 1.获取验证码
        String verificationCode = request.getVerificationCode();
        // 1.1.判断验证码是否为空
        if(StrUtil.isBlank(verificationCode))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不能为空");

        // 2.判断该用户是否存在
        User user = null;
        user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getQqEmail, request.getQqEmail()));
        if(user != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已注册过账户，请前往登录");
        }

        // 3.验证码进行匹配                                         redisKey:"loveai:verificationCode:0:3028077121@qq.com"
        String targetCode = stringRedisTemplate.opsForValue().get(VERIFICATIONCODE + VerificationCodeTypeEnum.REGISTER.getValue() + ":" + request.getQqEmail());
        // 3.1.判断验证码是否过期
        if(StrUtil.isBlank(targetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码无效");
        }
        // 3.2.验证码匹配
        if(!StrUtil.equals(targetCode, verificationCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        // 4.验证码匹配成功，创建用户
        user = User.builder()
                .qqEmail(request.getQqEmail())
                .username(this.getRandomNickName())
                .build();
        return this.save(user);
    }

    /**
     * 发送验证码
     */
    @Override
    public Boolean sendEmailCode(AddUserRequest request) {
        // 1.判断 QQ 邮箱格式是否正确
        if(!ValidationUtils.isValidQqEmail(request.getQqEmail())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "QQ 邮箱格式错误");
        }

        // 2.判断是登录还是注册时发送的验证码
        String verificationCodeType = request.getVerificationCodeType();
        if(!VerificationCodeTypeEnum.isValid(verificationCodeType)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "参数错误，不存在该类型的验证码，系统异常");
        }

        // 3.随机生成验证码，异步发送验证码
        String verificationCode = mailUtils.generateVerificationCode();
        asyncTaskService.executeAsyncTask(() -> {
            try {
                // 4.发送验证码
                mailUtils.sendVerificationCode(request.getQqEmail(), verificationCode);
                // 5.验证码保存到 redis 中，设置过期时间    redisKey:"loveai:verificationCode:0/1:3028077121@qq.com"
                stringRedisTemplate.opsForValue().set(VERIFICATIONCODE + verificationCodeType + ":" + request.getQqEmail(),
                        verificationCode, 3, TimeUnit.MINUTES); // 设置 3 分钟过期时间
            } catch (MessagingException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送失败");
            }
        },"Send verificationCode:" + verificationCode + " to " + request.getQqEmail());
        return true;
    }


    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1.从 session 中获取用户
        User user = (User)request.getSession().getAttribute(LOGIN_USER);
        // 2.判断用户是否登录
        if(user == null || user.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        // 3.判断是否可以查询到所记录的用户
        User currentUser = getById(user.getId());
        if(currentUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        return currentUser;
    }


    /**
     * 脱敏，Entity 转换为 VO
     */
    @Override
    public UserVO entityToVO(User user){
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    /**
     * 随机获取用户昵称
     */
    @Override
    public String getRandomNickName(){
        List<String> userNickName = USER_NICK_NAME_PREFIX;
        int index = RandomUtil.randomInt(0, userNickName.size());
        return userNickName.get(index);
    }
}




