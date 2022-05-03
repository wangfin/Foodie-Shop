package com.imooc.service;

import com.imooc.pojo.Items;
import com.imooc.pojo.ItemsImg;
import com.imooc.pojo.ItemsParam;
import com.imooc.pojo.ItemsSpec;
import com.imooc.pojo.vo.CommentLevelCountsVO;
import com.imooc.pojo.vo.ItemCommentVO;
import com.imooc.pojo.vo.ShopcartVO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

public interface ItemService {

    /**
     * 根据商品ID查询商品详情
     * @param id 全局唯一ID
     * @return
     */
    public Items queryItemById(String id);

    /**
     * 根据商品ID查询商品图片列表
     * @param itemId
     * @return
     */
    public List<ItemsImg> queryItemImgList(String itemId);

    /**
     * 根据商品ID查询商品规格
     * @param itemId
     * @return
     */
    public List<ItemsSpec> queryItemSpecList(String itemId);

    /**
     * 根据商品ID查询商品属性
     * @param itemId
     * @return
     */
    public ItemsParam queryItemsParam(String itemId);

    /**
     * 根据商品ID查询商品的评价等级数量
     * @param itemId
     */
    public CommentLevelCountsVO queryCommentCounts(String itemId);

    /**
     * 根据商品ID查询商品的评价，这里对评价进行了分页
     * @param itemId
     * @param level 好评，中评，差评
     * @return
     */
    public PagedGridResult queryPagedComments(String itemId, Integer level,
                                              Integer page, Integer pageSize);

    /**
     * 根据关键字搜索商品列表
     * @param keywords 关键字
     * @param sort 按什么排序，默认排序，销量排序，价格排序
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult searchItems(String keywords, String sort,
                                       Integer page, Integer pageSize);

    /**
     * 根据商品三级分类ID搜索商品列表
     * @param catId 关键字
     * @param sort 按什么排序，默认排序，销量排序，价格排序
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult searchItemsByThirdCat(Integer catId, String sort,
                                       Integer page, Integer pageSize);

    /**
     * 根据前端传入的商品规格IDS查询购物车中最新的商品数据（用于刷新渲染购物车中的商品数据）
     * @param specIds 商品规格的拼接字符串
     * @return
     */
    public List<ShopcartVO> queryItemsBySpecIds(String specIds);
}
