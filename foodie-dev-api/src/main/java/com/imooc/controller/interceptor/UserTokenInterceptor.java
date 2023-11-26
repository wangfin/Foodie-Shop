package com.imooc.controller.interceptor;

import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class UserTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtils redisUtils;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public static final Logger log = LoggerFactory.getLogger(UserTokenInterceptor.class);

    /**
     * 拦截请求，在访问controller调用之前
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 从header中获取userId与userToken
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(userToken)) {
            log.error("尚未登录，请您登录！");
            returnErrorResponse(response, IMOOCJSONResult.errorMsg("尚未登录，请您登录！"));
            return false;
        }

        // 从redis中获取token信息
        String uniqueToken = redisUtils.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(uniqueToken)) {
            log.error("尚未登录，请您登录！");
            returnErrorResponse(response, IMOOCJSONResult.errorMsg("尚未登录，请您登录！"));
            return false;
        }

        if (!uniqueToken.equals(userToken)) {
            log.error("账户在异地登录，请重新登录！");
            returnErrorResponse(response, IMOOCJSONResult.errorMsg("账户在异地登录，请重新登录！"));
            return false;
        }

        /**
         * false：请求被拦截，被驳回，验证出现问题
         * true：请求在经过验证校验以后，是可以放行
         */
        return true;
    }

    /**
     * 用response以json的形式返回错误信息
     * @param response
     */
    public void returnErrorResponse(HttpServletResponse response, IMOOCJSONResult result) {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/json");
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 请求访问controller之后，渲染视图之前
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 请求访问controller之后，渲染视图之前
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
