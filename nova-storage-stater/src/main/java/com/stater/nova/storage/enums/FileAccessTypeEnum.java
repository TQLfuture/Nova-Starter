package com.stater.nova.storage.enums;

import lombok.Getter;

/**
 * 上传文件访问类型#PRIVATELY#0:私有:PUBLICLY,1:公有
 */
@Getter
public enum FileAccessTypeEnum {
    PRIVATE(1, "私有"),
    SECRET(2, "（机密）隐私数据"),
    PUBLIC(3, "公共静态资源");

    private Integer code;

    private String msg;

    FileAccessTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static FileAccessTypeEnum codeOf(Integer code) {
        for (FileAccessTypeEnum value : FileAccessTypeEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static FileAccessTypeEnum nameOf(String name) {
        for (FileAccessTypeEnum value : FileAccessTypeEnum.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

}
