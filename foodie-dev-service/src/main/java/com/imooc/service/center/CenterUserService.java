package com.imooc.service.center;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.center.CenterUserBO;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;

/**
 * 个人中心center的用户服务
 */
public interface CenterUserService {

    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return
     */
    public Users queryUserInfo(String userId);

    /**
     * 修改用户信息
     * @param userId 用户ID
     * @param centerUserBO 信息BO
     */
    public Users updateUserInfo(String userId, CenterUserBO centerUserBO);

    /**
     * 用户头像刷新
     * @param userId 用户ID
     * @param faceUrl 更新后的头像文件地址
     * @return
     */
    public Users updateUserFace(String userId, String faceUrl);
}
