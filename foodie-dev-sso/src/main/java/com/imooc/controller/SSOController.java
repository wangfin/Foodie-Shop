package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVo;
import com.imooc.service.UserService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class SSOController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtils redisUtils;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public static final String REDIS_USER_TICKET = "redis_user_ticket";

    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";

    public static final String COOKIE_USER_TICKET = "cookie_user_ticket";

    // 获取get请求
    @GetMapping("/hello")
    public Object hello() {
        return "Hello World~";
    }

    // 获取get请求
    @GetMapping("/login")
    public String login(String returnUrl,
                        Model model,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        model.addAttribute("returnUrl", returnUrl);

        // 1. 获取userTicket门票，如果cookie中能够获取到，证明用户登录过，此时签发一个一次性的临时门票
        String userTicket = getCookie(request, COOKIE_USER_TICKET);

        boolean isVerified = verifyUserTicket(userTicket);
        if (isVerified) {
            String tmpTicket = createTmpTicket();
            return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
        }

        // 2. 用户从未登录过，第一次进入则跳转到CAS统一登陆界面
        return "登陆~";
    }

    /**
     * 校验CAS全局用户门票
     */
    private boolean verifyUserTicket(String userTicket) {
        // 1. 验证CAS全局门票不能为空
        if (StringUtils.isBlank(userTicket)) {
            return false;
        }

        // 2. 验证CAS门票是否有效
        String userId = redisUtils.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return false;
        }

        // 3. 验证门票对应的user会话是否存在
        String userRedis = redisUtils.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return false;
        }

        return true;
    }

    /**
     * CAS统一登陆接口
     * 目的：
     * 1. 登录后创建用户的全局会话 -> uniqueToken
     * 2. 创建用户全局门票，用于表示在CAS是否已经登录 -> userTicket
     * 3. 创建用户的临时票据，用于会跳回传 -> tmpTicket
     */
    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String returnUrl,
                          Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) throws Exception {
        model.addAttribute("returnUrl", returnUrl);

        // 1. 判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(username) ||
                StringUtils.isBlank(username)) {
            model.addAttribute("errmsg", "用户名或密码不能为空");
            return "login";
        }

        // 2. 实现登录
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));

        if (userResult == null) {
            model.addAttribute("errmsg", "用户名或密码不正确");
            return "login";
        }

        // 3. 实现用户的redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();

        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(userResult, usersVo);
        usersVo.setUserUniqueToken(uniqueToken);

        redisUtils.set(REDIS_USER_TOKEN + ":" + userResult.getId(), JsonUtils.objectToJson(usersVo));

        // 4. 生成ticket门票，全局门票，代表用户在CAS端登录过
        String userTicket = UUID.randomUUID().toString().trim();
        // 4.1 用户全局门票需要放入CAS端的cookie中
        setCookie(COOKIE_USER_TICKET, userTicket, response);


        // 5. userTicket关联用户id，并且放入到redis中
        redisUtils.set(REDIS_USER_TICKET + ":" + userTicket, userResult.getId());

        // 6. 生成临时票据，回跳到调用端网站，是由CAS端所签发的一个一次性的临时ticket
        String tmpTicket = createTmpTicket();

        /**
         * userTicket：用于表示用户在CAS端的一个登录状态：已经登录
         * tmpTicket：用于颁发给用户进行一次性验证的票据，有时效性
         */

        /**
         * CAS系统的全局门票和用户全局会话
         * 去其他网站需要领取一次性票据也就是tmpTicket，才能登录网站
         * 当使用完临时票据之后需要销毁，且临时门票有时效性
         */

        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
    }

    /**
     * 创建临时票据
     *
     * @return
     */
    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();

        try {
            redisUtils.setEx(REDIS_TMP_TICKET + ":1" + tmpTicket, MD5Utils.getMD5Str(tmpTicket),
                    600, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmpTicket;
    }


    private void setCookie(String key,
                           String val,
                           HttpServletResponse response) {
        Cookie cookie = new Cookie(key, val);
        cookie.setDomain("webswb.cn");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String getCookie(HttpServletRequest request,
                             String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || StringUtils.isBlank(key)) {
            return null;
        }

        String cookieValue = null;
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(key)) {
                cookieValue = cookies[i].getValue();
                break;
            }
        }

        return cookieValue;
    }

    /**
     * 验证临时票据
     */
    @PostMapping("verifyTmpTicket")
    @ResponseBody
    public IMOOCJSONResult verifyTmpTicket(String tmpTicket,
                                           HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {
        // 使用一次性临时票据来验证用户是否登录，如果登录过，把用户会话信息返回站点
        // 使用完毕后，需要销毁临时票据
        String tmpTicketValue = redisUtils.get(REDIS_TMP_TICKET + ":" + tmpTicket);
        if (StringUtils.isBlank(tmpTicketValue)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        }

        // 1. 如果临时票据可以，则需要销毁，并且拿到CAS端cookie中的全局userTicket，以此再获取用户
        if (!tmpTicketValue.equals(MD5Utils.getMD5Str(tmpTicket))) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        } else {
            // 销毁临时票据
            redisUtils.delete(REDIS_TMP_TICKET + ":" + tmpTicket);
        }

        // 1. 验证并且获取用户的userTicket
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        String userId = redisUtils.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        }

        // 2. 验证票据对应的user会话是否存在
        String userRedis = redisUtils.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        }

        // 验证成功，返回OK，携带用户会话
        return IMOOCJSONResult.ok(JsonUtils.jsonToPojo(userRedis, UsersVo.class));

    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    @ResponseBody
    public IMOOCJSONResult logout(String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        // 1. 获取CAS中的用户门票
        String userTicket = getCookie(request, COOKIE_USER_TICKET);

        // 2. 清除userTicket
        deleteCookie(COOKIE_USER_TICKET, response);
        redisUtils.delete(REDIS_USER_TICKET + ":" + userTicket);

        // 3. 清除用户全局会话（分布式会话）
        redisUtils.delete(REDIS_USER_TOKEN + ":" + userId);

        return IMOOCJSONResult.ok();
    }

    private void deleteCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setDomain("webswb.cn");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }


}
