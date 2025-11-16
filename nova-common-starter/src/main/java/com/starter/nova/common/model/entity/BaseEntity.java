package com.starter.nova.common.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * @author tql
 * @date: 2025/11/6
 * @time: 10:37
 * @desc:
 */
@Data
@FieldNameConstants
public class BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    protected Long id;

    @Version
    protected int version;

    @TableLogic(delval = "id")
    @TableField(fill = FieldFill.INSERT)
    protected Long deleteFlag;

    @TableField(fill = FieldFill.INSERT)
    protected Integer testFlag;

    @TableField(fill = FieldFill.INSERT)
    protected String creator;

    @TableField(fill = FieldFill.INSERT)
    protected Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String modifier;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected Long modifierId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    /**
     * 租户ID
     */
    protected Long tenantId;
}
