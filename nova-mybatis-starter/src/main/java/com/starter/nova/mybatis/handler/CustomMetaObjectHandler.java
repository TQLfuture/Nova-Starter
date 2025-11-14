package com.starter.nova.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.starter.nova.common.model.entity.BaseEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author tql
 * @description: 自定义元数据处理器
 * @date: 2025/11/13
 * @time: 16:56
 * @desc:
 */
public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, BaseEntity.Fields.creatorId, Long.class, 0L);
        this.strictInsertFill(metaObject, BaseEntity.Fields.creator, String.class, "UNKNOWN");
        this.strictInsertFill(metaObject, BaseEntity.Fields.createTime, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, BaseEntity.Fields.deleteFlag, Integer.class, 0);
        Object testFlag = this.getFieldValByName(BaseEntity.Fields.testFlag, metaObject);
        if (testFlag == null) {
            this.strictInsertFill(metaObject, BaseEntity.Fields.testFlag, Integer.class, 0);
        }
        // 更新字段
        this.updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, BaseEntity.Fields.modifierId, Long.class, 0L);
        this.strictUpdateFill(metaObject, BaseEntity.Fields.modifier, String.class, "UNKNOWN");
        this.strictUpdateFill(metaObject, BaseEntity.Fields.updateTime, LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
        Object obj = fieldVal.get();
        if (Objects.nonNull(obj)) {
            metaObject.setValue(fieldName, obj);
        }
        return this;
    }
}
