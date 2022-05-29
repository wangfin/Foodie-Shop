package com.imooc.controller;

import com.imooc.pojo.*;
import com.imooc.pojo.vo.*;
import com.imooc.service.ItemService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商品接口", tags = {"商品信息展示的相关接口"})
@RestController
@RequestMapping("items")
public class ItemsController extends BaseController{

    @Autowired
    private ItemService itemService;

    /**
     * 查询商品详情
     * @return
     */
    @ApiOperation(value = "查询商品详情", notes = "查询商品详情", httpMethod = "GET")
    @GetMapping("/info/{itemId}")
    public IMOOCJSONResult info(
            @ApiParam(name = "itemId", value = "商品ID", required = true)
            @PathVariable String itemId){

        // 判断输入是否为空
        if (StringUtils.isBlank(itemId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // 查询商品信息
        Items item = itemService.queryItemById(itemId);
        List<ItemsImg> itemsImgList = itemService.queryItemImgList(itemId);
        List<ItemsSpec> itemsSpecList = itemService.queryItemSpecList(itemId);
        ItemsParam itemsParam = itemService.queryItemsParam(itemId);

        // 因为最后需要同时回传四个数据，所以不能直接使用IMOOCResult，需要先去构建一个VO
        ItemInfoVO itemInfoVO = new ItemInfoVO();
        itemInfoVO.setItem(item);
        itemInfoVO.setItemImgList(itemsImgList);
        itemInfoVO.setItemSpecList(itemsSpecList);
        itemInfoVO.setItemParams(itemsParam);

        return IMOOCJSONResult.ok(itemInfoVO);

    }


    /**
     * 查询商品评价等级
     * @return
     */
    @ApiOperation(value = "查询商品评价等级", notes = "查询商品评价等级", httpMethod = "GET")
    // 这里的参数不是路径参数了，是请求参数
    @GetMapping("/commentLevel")
    public IMOOCJSONResult commentLevel(
            @ApiParam(name = "itemId", value = "商品ID", required = true)
            @RequestParam String itemId){

        // 判断输入是否为空
        if (StringUtils.isBlank(itemId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // 查询商品评级等级数量
        CommentLevelCountsVO countsVO = itemService.queryCommentCounts(itemId);
        return IMOOCJSONResult.ok(countsVO);
    }

    /**
     * 查询商品评论具体信息
     * @return
     */
    @ApiOperation(value = "查询商品评论具体信息", notes = "查询商品评论具体信息", httpMethod = "GET")
    // 这里的参数不是路径参数了，是请求参数
    @GetMapping("/comments")
    public IMOOCJSONResult commentLevel(
            @ApiParam(name = "itemId", value = "商品ID", required = true)
            @RequestParam String itemId,
            @ApiParam(name = "level", value = "评价等级", required = false)
            @RequestParam Integer level,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize){

        // 判断输入是否为空
        if (StringUtils.isBlank(itemId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // 判断page是否为空，如果不为空，则设置为默认的1
        if (page == null){
            page = 1;
        }

        // 设置默认pageSize
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询商品不同评级的评论具体信息，具有分页功能
        PagedGridResult grid = itemService.queryPagedComments(itemId, level, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }

    /**
     * 根据关键词检索商品列表
     * @return
     */
    @ApiOperation(value = "根据关键词检索商品列表", notes = "根据关键词检索商品列表", httpMethod = "GET")
    // 这里的参数不是路径参数了，是请求参数
    @GetMapping("/search")
    public IMOOCJSONResult commentLevel(
            @ApiParam(name = "keywords", value = "检索关键字", required = true)
            @RequestParam String keywords,
            @ApiParam(name = "sort", value = "排序方式", required = true)
            @RequestParam String sort,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize){

        // 判断输入是否为空
        if (StringUtils.isBlank(keywords)){
            return IMOOCJSONResult.errorMsg(null);
        }

        // 判断page是否为空，如果不为空，则设置为默认的1
        if (page == null){
            page = 1;
        }

        // 设置默认pageSize
        if (pageSize == null){
            pageSize = PAGE_SIZE;
        }

        // 根据keywords查询商品列表，具有分页功能
        PagedGridResult grid = itemService.searchItems(keywords, sort, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }

    /**
     * 根据商品分类ID检索商品列表
     * @return
     */
    @ApiOperation(value = "根据商品分类ID检索商品列表", notes = "根据商品分类ID检索商品列表", httpMethod = "GET")
    // 这里的参数不是路径参数了，是请求参数
    @GetMapping("/catItems")
    public IMOOCJSONResult commentLevel(
            @ApiParam(name = "catId", value = "商品三级分类ID", required = true)
            @RequestParam Integer catId,
            @ApiParam(name = "sort", value = "排序方式", required = false)
            @RequestParam String sort,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize){

        // 判断输入是否为空
        if (catId == null){
            return IMOOCJSONResult.errorMsg(null);
        }

        // 判断page是否为空，如果不为空，则设置为默认的1
        if (page == null){
            page = 1;
        }

        // 设置默认pageSize
        if (pageSize == null){
            pageSize = PAGE_SIZE;
        }

        // 根据keywords查询商品列表，具有分页功能
        PagedGridResult grid = itemService.searchItemsByThirdCat(catId, sort, page, pageSize);

        return IMOOCJSONResult.ok(grid);
    }

    /**
     * 根据商品规格ids查询最新的商品数据（用于更新渲染购物车）
     * 由于用户长时间未登录网站，刷新购物车中的数据（主要是商品价格），类似于京东淘宝
     * @return
     */
    @ApiOperation(value = "根据商品规格ids查询最新的商品数据（用于更新渲染购物车）",
            notes = "根据商品规格ids查询最新的商品数据（用于更新渲染购物车）", httpMethod = "GET")
    @GetMapping("/refresh")
    public IMOOCJSONResult refresh(
            @ApiParam(name = "itemSpecIds", value = "拼接的规格id", required = true, example = "1001, 1002, 1003")
            @RequestParam String itemSpecIds){

        // 判断输入是否为空
        if (StringUtils.isBlank(itemSpecIds)){
            return IMOOCJSONResult.ok(null);
        }

        List<ShopcartVO> list = itemService.queryItemsBySpecIds(itemSpecIds);

        return IMOOCJSONResult.ok(list);
    }


}
