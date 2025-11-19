package com.stater.nova.storage.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 19:21
 * @desc:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStsVO {
    /**
     * 临时SecretId
     */
    private String tmpSecretId;

    /**
     * 临时SecretKey
     */
    private String tmpSecretKey;

    /**
     * 临时sessionToken
     */
    private String sessionToken;

    /**
     * 存储桶
     */
    private String bucketName;

    /**
     * 存储桶所在的地域
     */
    private String regionName;

    /**
     * 对象键前缀，即用户文件目录前缀
     */
    private String keyPrefix;

    private String objectKey;

    /**
     * 过期时间，时间戳，单位秒
     */
    private Long expiredTime;

    /**
     *
     */
    private String endPoint;

    private Boolean cName;
}
