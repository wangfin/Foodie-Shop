package com.imooc.mapper;

import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.NewItemsVO;
import org.apache.ibatis.annotations.Param;

import java.util.*;

// 自定义的Mapper类，用于自定sql语句
public interface CategoryMapperCustom {
    // 查询一级分类ID下的二级、三级分类
    public List<CategoryVO> getSubCatList(Integer rootCatId);

    // 查询一级分类ID下的六个最新的商品信息
    public List<NewItemsVO> getSixNewItemsLazy(@Param("paramsMap") Map<String, Object> map);
}
