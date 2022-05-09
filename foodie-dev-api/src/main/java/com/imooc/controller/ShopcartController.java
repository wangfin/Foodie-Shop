package com.imooc.controller;

import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "购物车接口", tags = {"购物车的相关接口"})
@RestController
@RequestMapping("shopcart")
public class ShopcartController {

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param shopcartBO 购物车的BO
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        // 这里仅作简单判断
        if (StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // TODO 用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redis缓存

        System.out.println(shopcartBO);

        return IMOOCJSONResult.ok();
    }

    /**
     * 从购物车中删除商品
     * @param userId 用户ID
     * @param itemSpecId 商品规格ID
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        // 这里仅作简单判断
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // TODO 用户删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除后端购物车中的数据

        return IMOOCJSONResult.ok();
    }

}
