package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "注册登录", tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username){
        // @RequestParam 表示username是一种请求参数而不是请求路径
        // 这里使用的是apache commons-lang3中的StringUtils
        // 1. 判断用户名是否为空，或者空字符串
        if (StringUtils.isBlank(username)){
            // 返回HTTP状态码，500
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }

        // 2. 查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist){
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }

        // 3. 请求成功，用户名没有重复
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,
                                  HttpServletRequest request,
                                  HttpServletResponse response){
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPwd = userBO.getConfirmPassword();

        // 1. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(username) ||
                StringUtils.isBlank(username)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        // 2. 查询用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist){
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        // 3. 密码长度不能少于6位
        if (password.length() < 6){
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }
        // 4. 判断两次密码是否一致
        if (!password.equals(confirmPwd)){
            return IMOOCJSONResult.errorMsg("两次密码输入不一致");
        }
        // 5. 实现注册
        Users userResult = userService.createUser(userBO);

        // 删除信息
        userResult = setNullProperty(userResult);

        // 在注册完成之后，同样设置cookie
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 生成用户token，存入redis会话
        // TODO 同步购物车数据

        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception{
        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 1. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(username) ||
                StringUtils.isBlank(username)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }

        // 2. 实现登录
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));

        if (userResult == null){
            return IMOOCJSONResult.errorMsg("用户名或密码不正确");
        }

        // 删除部分隐私信息
        userResult = setNullProperty(userResult);

        // 设置cookie，将用户登录信息保存在cookie里，而不是使用session
        // session后面使用分布式session，使用无状态信息
        // JsonUtils.objectToJson将userResult对象信息转换成字符串
        CookieUtils.setCookie(request, response, "user",
                JsonUtils.objectToJson(userResult), true);

        // TODO 生成用户token，存入redis会话
        // TODO 同步购物车数据

        return IMOOCJSONResult.ok(userResult);
    }

    // 将隐私信息删除，不显示
    private Users setNullProperty(Users userResult){
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);

        return userResult;
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response){

        // 清除用户相关信息的cookie
        CookieUtils.deleteCookie(request, response, "user");

        // TODO 用户退出登录，需要请客购物车
        // TODO 分布式会话中需要清除用户数据

        return IMOOCJSONResult.ok();
    }
}
