package com.imooc.service.impl;

import com.imooc.enums.TypeEnum;
import com.imooc.mapper.CategoryMapper;
import com.imooc.mapper.CategoryMapperCustom;
import com.imooc.pojo.Category;
import com.imooc.pojo.vo.CategoryVO;
import com.imooc.pojo.vo.NewItemsVO;
import com.imooc.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryMapperCustom categoryMapperCustom;

    // 查询所有root级别的商品分类
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<Category> queryAllRootLevelCat(){

        Example categoryExample  = new Example(Category.class);
        Example.Criteria criteria = categoryExample.createCriteria();
        // 获取一级分类，因为前端未传入参数，所以在这里使用枚举类
        criteria.andEqualTo("type", TypeEnum.Level1.type);
        List<Category> result = categoryMapper.selectByExample(categoryExample);

        return result;
    }

    // 根据一级分类id查询子类
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<CategoryVO> getSubCatList(Integer rootCatid){
        return categoryMapperCustom.getSubCatList(rootCatid);
    }

    // 查询首页每个一级分类下的6条最新商品数据
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<NewItemsVO> getSixNewItemsLazy(Integer rootCatId){

        // 传入参数
        Map<String, Object> map = new HashMap<>();
        map.put("rootCatId", rootCatId);

        return categoryMapperCustom.getSixNewItemsLazy(map);
    }
}
