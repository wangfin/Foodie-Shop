package com.imooc.service;

import com.imooc.pojo.UserAddress;
import com.imooc.pojo.bo.AddressBO;

import java.util.List;

public interface AddressService {

    /**
     * 根据用户ID查询用户的收货地址列表
     * @param userId 用户ID
     * @return
     */
    public List<UserAddress> queryAll(String userId);

    /**
     * 用户新增地址
     * @param addressBO 地址BO
     */
    public void addNewUserAddress(AddressBO addressBO);

    /**
     * 用户修改地址
     * @param addressBO 地址BO
     */
    public void updateUserAddress(AddressBO addressBO);

    /**
     * 根据用户ID和地址ID删除对应的地址信息
     * @param userId 用户ID
     * @param addressId 地址ID
     */
    public void deleteUserAddress(String userId, String addressId);

    /**
     * 修改用户的默认地址
     * @param userId 用户ID
     * @param addressId 地址ID
     */
    public void updateUserAddressToBeDefault(String userId, String addressId);
}
