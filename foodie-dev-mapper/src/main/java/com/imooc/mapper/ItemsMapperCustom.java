package com.imooc.mapper;

import com.imooc.pojo.vo.ItemCommentVO;
import com.imooc.pojo.vo.SearchItemsVO;
import com.imooc.pojo.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;

import java.util.*;

public interface ItemsMapperCustom {
    // 查询商品评价
    public List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String, Object> map);

    // 关键词检索商品列表
    public List<SearchItemsVO> searchItems(@Param("paramsMap") Map<String, Object> map);

    // 商品分类ID检索商品列表
    public List<SearchItemsVO> searchItemsByThirdCat(@Param("paramsMap") Map<String, Object> map);

    // 查询商品规格信息，用于在购物车中展示
    public List<ShopcartVO> queryItemsBySpecId(@Param("paramsList") List<String> specIdsList);

    // 在单体阶段，使用数据库乐观锁的机制解决超卖问题
    // 即在数据库中更新库存数量，该函数即用于更新数量
    public int decreaseItemSpecStock(@Param("specId") String specId,
                                     @Param("pendingCounts") Integer pendingCounts);
}