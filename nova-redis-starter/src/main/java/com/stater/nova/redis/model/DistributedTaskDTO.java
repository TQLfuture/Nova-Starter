package com.stater.nova.redis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 21:02
 * @desc:
 */
@Data
@SuppressWarnings("unused")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributedTaskDTO {

    /**
     * 分布式任务ID
     */
    private String taskId;

    /**
     * 分布式任务名称
     */
    private String taskName;

    /**
     * 错误信息编码
     */
    private String errorMsgCode;

    /**
     * 不打印错误日志
     */
    private Boolean notPrintErrorLog;
}
