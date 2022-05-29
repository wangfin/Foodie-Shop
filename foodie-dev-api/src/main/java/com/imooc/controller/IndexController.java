package com.imooc.controller;

import com.imooc.enums.YesOrNoEnum;
import com.imooc.pojo.Carousel;
import com.imooc.pojo.Category;
import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.NewItemsVO;
import com.imooc.service.CarouselService;
import com.imooc.service.CategoryService;
import com.imooc.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    // 没加Autowired，导致报了一个NullPointerException的错误
    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取首页轮播图列表
     */
    @ApiOperation(value = "获取首页轮播图列表", notes = "获取首页轮播图列表", httpMethod = "GET")
    @GetMapping("/carousel")
    public IMOOCJSONResult carousel(){
        // 查询所有轮播图并返回结果
        List<Carousel> list = carouselService.queryAll(YesOrNoEnum.YES.type);

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
    public IMOOCJSONResult cats(){
        // 查询所有一级分类并返回结果
        // 因为这里从前端是不传入参数的，所以这里不使用枚举类
        List<Category> list = categoryService.queryAllRootLevelCat();

        return IMOOCJSONResult.ok(list);
    }

    /**
     * 获取首页商品分类（子分类，懒加载）
     */
    @ApiOperation(value = "获取首页商品分类（子分类）", notes = "获取首页商品分类（子分类）", httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public IMOOCJSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类ID", required = true)
            @PathVariable Integer rootCatId){

        // 查询前端传入的id是否为空
        if (rootCatId == null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        // 查询所有一级分类的子分类
        List<CategoryVO> list = categoryService.getSubCatList(rootCatId);
        return IMOOCJSONResult.ok(list);
    }

    /**
     * 获取首页一级商品分类下的六个最新商品信息
     */
    @ApiOperation(value = "获取首页一级商品分类下的六个最新商品信息", notes = "获取首页一级商品分类下的六个最新商品信息", httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")
    public IMOOCJSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类ID", required = true)
            @PathVariable Integer rootCatId){

        // 查询前端传入的id是否为空
        if (rootCatId == null){
            return IMOOCJSONResult.errorMsg("分类不存在");
        }
        // 查询所有一级分类的子分类
        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return IMOOCJSONResult.ok(list);
    }

}
