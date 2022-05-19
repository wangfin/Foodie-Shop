package com.imooc.enums;

/**
 * 是否枚举 轮播图
 */
public enum TypeEnum {
    Level1(1, "第1级"),
    Level2(2, "第2级"),
    Level3(3, "第3级");

    public final Integer type;
    public final String value;

    TypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
