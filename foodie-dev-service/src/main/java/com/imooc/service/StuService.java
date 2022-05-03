package com.imooc.service;

import com.imooc.pojo.Stu;

/**
 * 用来测试的
 */
public interface StuService {
    // 使用id获取信息
    public Stu getStuInfo(int id);
    // 保存信息
    public void saveStu();
    // 更新信息
    public void updateStu(int id);
    // 删除
    public void deleteStu(int id);

    public void saveParent();
    public void saveChildren();

}
