package com.imooc.controller;

import com.imooc.enums.YesOrNoEnum;
import com.imooc.pojo.Carousel;
import com.imooc.pojo.Category;
import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.NewItemsVO;
import com.imooc.service.CarouselService;
import com.imooc.service.CategoryService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    // 没加Autowired，导致报了一个NullPointerException的错误
    @Autowired
    private CarouselService carouselService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取首页轮播图列表
     */
    @ApiOperation(value = "获取首页轮播图列表", notes = "获取首页轮播图列表", httpMethod = "GET")
    @GetMapping("/carousel")
    public IMOOCJSONResult carousel() {
        // 先查询缓存
        List<Carousel> list = new ArrayList<>();
        String carouselStr = redisUtils.get("carousel");
        if (StringUtils.isBlank(carouselStr)) {
            // 查询所有轮播图并返回结果
            list = carouselService.queryAll(YesOrNoEnum.YES.type);
            // 查询完结构后存在redis中
            redisUtils.set("carousel", JsonUtils.objectToJson(list));
        } else {
            list = JsonUtils.jsonToList(carouselStr, Carousel.class);
        }

        /**
         * 轮播图缓存更改逻辑
         * 1. 后台运营系统，一旦广告（轮播图）发生更改，就可以删除缓存，然后重置
         * 2. 定时重置，在凌晨重置
         * 3. 每个轮播图都有可能是一个广告，每个广告都会有一个过期时间，过期后再重置
         */

        return IMOOCJSONResult.ok(list);
    }

    /**
     * 首页分类展示需求：
     * 1. 第一次刷新主页查询大分类，渲染展示到首页
     * 2. 如果鼠标上移到大分类，则加载其子类的内容
     * 如果已经存在子分类，则不需要加载（懒加载）
     */
    /**
     * 获取首页商品分类（一级分类）
     */
    @ApiOperation(value = "获取首页商品分类（一级分类）", notes = "获取首页商品分类（一级分类）", httpMethod = "GET")
    @GetMapping("/cats")
    public IMOOCJSONResult cats() {
        // 查询缓存
        List<Category> list = new ArrayList<>();
        String catsStr = redisUtils.get("cats");
        if (StringUtils.isBlank(catsStr)) {
            // 查询所有一级分类并返回结果
            // 因为这里从前端是不传入参数的，所以这里不使用枚举类
            list = categoryService.queryAllRootLevelCat();
            redisUtils.set("cats", JsonUtils.objectToJson(list));
        } else {
            list = JsonUtils.jsonToList(catsStr, Category.class);
        }

        return IMOOCJSONResult.ok(list);
    }

    /**
     * 获取首页商品分类（子分类，懒加载）
     */
    @ApiOperation(value = "获取首页商品分类（子分类）", notes = "获取首页商品分类（子分类）", httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public IMOOCJSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类ID", required = true)
            @PathVariable Integer rootCatId) {

        // 查询前端传入的id是否为空
        if (rootCatId == null) {
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        // 查询所有一级分类的子分类
        List<CategoryVO> list = new ArrayList<>();
        String catsStr = redisUtils.get("subCat" + rootCatId);
        if (StringUtils.isBlank(catsStr)) {
            list = categoryService.getSubCatList(rootCatId);
            if (!CollectionUtils.isEmpty(list)) {
                // 有值的情况下，设置5分钟过期
                redisUtils.setEx("subCat" + rootCatId, JsonUtils.objectToJson(list), 5, TimeUnit.MINUTES);
            } else {
                // 没有值也存在redis中，这样防止redis缓存穿透
                // 缓存穿透指查询的key在缓存和数据库中都不存在，但是会频繁请求数据库，导致缓存失效
                // 响应的缓存时间设置小一点
                redisUtils.setEx("subCat" + rootCatId, JsonUtils.objectToJson(list), 30, TimeUnit.SECONDS);
            }

        } else {
            list = JsonUtils.jsonToList(catsStr, CategoryVO.class);
        }

        return IMOOCJSONResult.ok(list);
    }

    /**
     * 获取首页一级商品分类下的六个最新商品信息
     */
    @ApiOperation(value = "获取首页一级商品分类下的六个最新商品信息", notes = "获取首页一级商品分类下的六个最新商品信息", httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")
    public IMOOCJSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类ID", required = true)
            @PathVariable Integer rootCatId) {

        // 查询前端传入的id是否为空
        if (rootCatId == null) {
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        // 查询所有一级分类的子分类
        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return IMOOCJSONResult.ok(list);
    }

}
