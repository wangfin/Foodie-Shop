package com.imooc.service;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 判断用户名是否存在
     */
    public boolean queryUsernameIsExist(String Username);

    /**
     * 创建用户
     */
    // 补充一下BO的相关内容，BO指的是业务对象，Business Object
    // 其他的有领域对象DO，Domain Object
    // 值对象/视图对象 VO，Value Object
    public Users createUser(UserBO userBO);

    /**
     * 用户登录
     */
    public Users queryUserForLogin(String username, String password);

}
