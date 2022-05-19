package com.imooc.enums;

/**
 * 是否枚举 轮播图
 */
public enum YesOrNoEnum {
    NO(0, "否"),
    YES(1, "是");

    public final Integer type;
    public final String value;

    YesOrNoEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
