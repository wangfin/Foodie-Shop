package com.imooc.service.impl;

import com.imooc.enums.Sex;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.MD5Utils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UsersMapper usersMapper;

    // 全局唯一ID
    @Autowired
    private Sid sid;

    private static final String USER_FACE =
            "https://notes-pic.oss-cn-shanghai.aliyuncs.com/avatar/%E7%8C%AB%E7%8C%AB%E5%A4%B4.png";

    // 判断数据库中是否已经有这个用户名
    // 因为是查询所以不怎么需要事务
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username){

        // 通过Example，使用条件进行查询，需要传入你想要查询哪一个实体类
        Example userExample = new Example(Users.class);
        // 创建条件
        Example.Criteria userCriteria = userExample.createCriteria();
        // 指相关的列必须等于方法参数中的值
        userCriteria.andEqualTo("username", username);

        Users result = usersMapper.selectOneByExample(userExample);

        // 判断数据库中是否已经有这个用户名
        return result == null ? false : true;
    }

    // 创建用户
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users createUser(UserBO userBO){

        // 创建短id
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);
        user.setUsername(userBO.getUsername());
        // 密码需要加密
        try {
            user.setPassword(MD5Utils.getMD5Str(userBO.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 默认用户昵称同用户名
        user.setNickname(userBO.getUsername());
        // 默认头像
        user.setFace(USER_FACE);
        // 默认生日，有一个字符串到日期的转换
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        // 默认性别，使用枚举类规范
        user.setSex(Sex.secret.type);

        // 创建时间与更新时间
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        // 保存信息
        usersMapper.insert(user);

        return user;
    }

    // 用户登录
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password){
        // 查询实体类
        Example userExample = new Example(Users.class);
        // 查询条件
        Example.Criteria userCriteria = userExample.createCriteria();
        // 查询语句，相等，前面""中需要与pojo中的属性名相同
        userCriteria.andEqualTo("username", username);
        userCriteria.andEqualTo("password", password);
        // 查询，返回结果
        Users result = usersMapper.selectOneByExample(userExample);

        return result;
    }


}
