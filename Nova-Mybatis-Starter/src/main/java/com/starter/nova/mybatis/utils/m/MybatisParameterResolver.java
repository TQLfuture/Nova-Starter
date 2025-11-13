package com.starter.nova.mybatis.utils.m;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * tql
 */
public class MybatisParameterResolver {

    private MybatisParameterResolver() {
    }

    /**
     * 用于从MyBatis的MappedStatement和BoundSql对象中获取SQL参数信息
     *
     * @param ms
     * @param boundSql
     * @return
     */
    public static List<Object> resolveParameters(MappedStatement ms, BoundSql boundSql) {
        // 从BoundSql对象中获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // 如果参数映射列表为空，则返回一个空列表
        if (CollectionUtils.isEmpty(parameterMappings)) {
            return Collections.emptyList();
        }

        // 从BoundSql对象中获取参数对象
        Object parameter = boundSql.getParameterObject();

        // 创建一个参数列表，用于存储提取到的参数值
        List<Object> parameterList = new ArrayList<>(parameterMappings.size());

        // 遍历参数映射列表
        for (ParameterMapping parameterMapping : parameterMappings) {
            // 使用getParameterValue方法来获取每个参数映射的值
            Object value = getParameterValue(ms, boundSql, parameterMapping, parameter);

            // 如果值是IEnum类型，则获取其内部的值
            if (value instanceof IEnum) {
                value = ((IEnum) value).getValue();
            }

            // 将值添加到参数列表中
            parameterList.add(value);
        }

        // 返回参数列表
        return parameterList;
    }

    /**
     * 获取参数值的方法。
     *
     * @param ms               MappedStatement 对象，包含 SQL 语句的映射信息
     * @param boundSql         BoundSql 对象，包含 SQL 语句和参数映射信息
     * @param parameterMapping 参数映射对象，用于描述参数的属性和类型处理器
     * @param parameter        参数对象，通常是一个 JavaBean 或 Map
     * @return 参数值
     */
    private static Object getParameterValue(MappedStatement ms, BoundSql boundSql, ParameterMapping parameterMapping,
                                            Object parameter) {
        // 获取参数映射的属性名
        String propertyName = parameterMapping.getProperty();
        // 获取 MyBatis 配置对象
        Configuration configuration = ms.getConfiguration();
        // 获取类型处理器注册表
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        // 创建参数对象的元数据对象
        MetaObject metaObject = configuration.newMetaObject(parameter);

        // 如果 BoundSql 中包含此属性名的附加参数，则返回对应的附加参数值
        if (boundSql.hasAdditionalParameter(propertyName)) {
            return boundSql.getAdditionalParameter(propertyName);
        } else if (parameter != null && typeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
            // 如果参数对象不为空且类型处理器注册表中存在该参数对象的类型处理器，则返回参数对象
            return parameter;
        } else if (parameter != null) {
            // 如果参数对象不为空，则从元数据对象中获取属性值
            return metaObject.getValue(propertyName);
        }
        // 如果参数对象为空，则返回 null
        return null;
    }
}
