package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.enums.CommentLevelEnum;
import com.imooc.enums.YesOrNoEnum;
import com.imooc.mapper.*;
import com.imooc.pojo.*;
import com.imooc.pojo.vo.CommentLevelCountsVO;
import com.imooc.pojo.vo.ItemCommentVO;
import com.imooc.pojo.vo.SearchItemsVO;
import com.imooc.pojo.vo.ShopcartVO;
import com.imooc.service.ItemService;
import com.imooc.utils.DesensitizationUtil;
import com.imooc.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemsMapper itemsMapper;
    @Autowired
    private ItemsImgMapper itemsImgMapper;
    @Autowired
    private ItemsSpecMapper itemsSpecMapper;
    @Autowired
    private ItemsParamMapper itemsParamMapper;
    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;
    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    // 根据商品ID查询商品详情
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Items queryItemById(String itemId) {
        return itemsMapper.selectByPrimaryKey(itemId);
    }

    // 根据商品ID查询商品图片列表
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsImg> queryItemImgList(String itemId) {
        Example itemsImgExp = new Example(ItemsImg.class);
        Example.Criteria criteria = itemsImgExp.createCriteria();
        criteria.andEqualTo("itemId", itemId);

        return itemsImgMapper.selectByExample(itemsImgExp);
    }

    // 根据商品ID查询商品规格列表
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsSpec> queryItemSpecList(String itemId) {
        Example itemsSpecExp = new Example(ItemsSpec.class);
        Example.Criteria criteria = itemsSpecExp.createCriteria();
        criteria.andEqualTo("itemId", itemId);

        return itemsSpecMapper.selectByExample(itemsSpecExp);
    }

    // 根据商品ID查询商品属性
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsParam queryItemsParam(String itemId) {
        Example itemsParamExp = new Example(ItemsParam.class);
        Example.Criteria criteria = itemsParamExp.createCriteria();
        criteria.andEqualTo("itemId", itemId);

        return itemsParamMapper.selectOneByExample(itemsParamExp);
    }

    // 查询商品评价数量
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public CommentLevelCountsVO queryCommentCounts(String itemId) {

        Integer goodCounts = getCommentCounts(itemId, CommentLevelEnum.GOOD.type);
        Integer normalCounts = getCommentCounts(itemId, CommentLevelEnum.NORMAL.type);
        Integer badCounts = getCommentCounts(itemId, CommentLevelEnum.BAD.type);
        Integer totalCounts = goodCounts + normalCounts + badCounts;

        // 通过VO将数据回传
        CommentLevelCountsVO countsVO = new CommentLevelCountsVO();
        countsVO.setTotalCounts(totalCounts);
        countsVO.setGoodCounts(goodCounts);
        countsVO.setNormalCounts(normalCounts);
        countsVO.setBadCounts(badCounts);

        return countsVO;
    }

    // 通用函数，用于查询不同等级的评论
    @Transactional(propagation = Propagation.SUPPORTS)
    Integer getCommentCounts(String itemId, Integer level){
        // 使用pojo作为查询条件
        ItemsComments condition = new ItemsComments();
        condition.setItemId(itemId);
        if (level != null){
            condition.setCommentLevel(level);
        }

        // 查询数量
        return itemsCommentsMapper.selectCount(condition);
    }

    // 查询商品具体评价内容
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryPagedComments(String itemId, Integer level, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("level", level);

        // mybits-pagehelper 分页助手
        // 分页助手需要在sql查询之前设置，他会统一拦截sql请求，并添加一段代码用于分页查询
        /**
         * page: 第几页
         * pageSize：每页显示条数
         */
        PageHelper.startPage(page, pageSize);
        List<ItemCommentVO> list = itemsMapperCustom.queryItemComments(map);
        // 使用通用脱敏工具类对nickname进行脱敏
        for (ItemCommentVO vo : list){
            vo.setNickname(DesensitizationUtil.commonDisplay(vo.getNickname()));
        }

        return setterPagedGrid(list, page);
    }

    // 通用组件，单独抽取出来，分页数据封装
    private PagedGridResult setterPagedGrid(List<?> list, Integer page){
        // 分页处理，分页数据需要封装到PagedGridResult.java传给前端
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());

        return grid;
    }

    // 根据关键字搜索商品列表
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult searchItems(String keywords, String sort, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("keywords", keywords);
        map.put("sort", sort);

        PageHelper.startPage(page, pageSize);
        List<SearchItemsVO> list = itemsMapperCustom.searchItems(map);

        return setterPagedGrid(list, page);
    }

    // 根据商品分类ID搜索商品列表
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult searchItemsByThirdCat(Integer catId, String sort, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("catId", catId);
        map.put("sort", sort);

        PageHelper.startPage(page, pageSize);
        List<SearchItemsVO> list = itemsMapperCustom.searchItemsByThirdCat(map);

        return setterPagedGrid(list, page);
    }

    // 根据商品规格IDs查询商品信息，用于购物车展示
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ShopcartVO> queryItemsBySpecIds(String specIds) {

        // 传入进来的specids是一个拼接字符串，需要先分割
        String ids[] = specIds.split(",");
        List<String> specIdsList = new ArrayList<>();
        Collections.addAll(specIdsList, ids);

        return itemsMapperCustom.queryItemsBySpecId(specIdsList);
    }

    // 根据商品规格ID查询商品信息
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsSpec queryItemSpecById(String specId) {
        return itemsSpecMapper.selectByPrimaryKey(specId);
    }

    // 根据商品ID获得商品图片主图的URL
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public String queryItemMainImgById(String itemId) {
        ItemsImg itemsImg = new ItemsImg();
        itemsImg.setItemId(itemId);
        itemsImg.setIsMain(YesOrNoEnum.YES.type);
        ItemsImg result = itemsImgMapper.selectOne(itemsImg);
        return result != null ? result.getUrl() : "";
    }

    // 用户购买时，需要从库存中减少相应的数量
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void decreaseItemSpecStock(String specId, int buyCounts) {

        // 到库存这边，就会涉及到高并发导致的超卖的问题，即多个线程同时访问，他们本身判断库存都安全，但是总得导致库存不足
        // 解决方案1：使用synchronized关键字，不推荐使用，在集群下，会有多个服务器，还是会导致超卖问题，并且性能低下
        // 解决方案2：锁数据库，不推荐使用，导致数据集性能降低
        // 解决方案3：分布式锁 zookeeper redis

        // 加锁
        // 1. 查询库存
        // 2. 判断库存，是否能够减少到0以下
        // 解锁

        // 在单体阶段，使用数据库乐观锁的方式来处理超卖问题
        int result = itemsMapperCustom.decreaseItemSpecStock(specId, buyCounts);
        // 这里的结果1表示数据库更新成功，即库存够的
        if (result != 1){
            throw new RuntimeException("订单创建失败，原因：库存不足！");
        }

    }
}
