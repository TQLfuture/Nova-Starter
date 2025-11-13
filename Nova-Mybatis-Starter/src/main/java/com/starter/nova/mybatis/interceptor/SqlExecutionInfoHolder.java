package com.starter.nova.mybatis.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.starter.nova.mybatis.model.pojo.SqlExecutionInfo;
import com.starter.nova.mybatis.util.MybatisParameterResolver;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tql
 */
public class SqlExecutionInfoHolder {

    /**
     * 使用ThreadLocal存储SqlExecutionInfo列表，确保每个线程有其独立的数据存储
     */
    private static final ThreadLocal<List<SqlExecutionInfo>> THREAD_LOCAL = new ThreadLocal<>();

    private SqlExecutionInfoHolder() {
    }

    /**
     * 设置与当前线程关联的SqlExecutionInfo列表
     */
    public static void setData(List<SqlExecutionInfo> value) {
        THREAD_LOCAL.set(value);
    }

    /**
     * 获取与当前线程关联的SqlExecutionInfo列表
     *
     * @return
     */
    public static List<SqlExecutionInfo> getData() {
        List<SqlExecutionInfo> sqlExecutionInfoList = THREAD_LOCAL.get();
        if (sqlExecutionInfoList == null) {
            return Collections.emptyList();
        }
        return sqlExecutionInfoList;
    }

    public static void addData(MappedStatement ms, BoundSql boundSql) {
        SqlExecutionInfo sqlExecutionInfo = new SqlExecutionInfo();
        String sql = boundSql.getSql().replaceAll("\\n+", " ");
        sqlExecutionInfo.setSqlStatement(sql);
        // 解析sql语句中的第一个表名，后续用到可以打开
        //sqlExecutionInfo.setFirstTableName(parseTableNameFromSql(sql));
        sqlExecutionInfo.setParameters(MybatisParameterResolver.resolveParameters(ms, boundSql));
        SqlExecutionInfoHolder.addData(sqlExecutionInfo);

    }

    private static String parseTableNameFromSql(String sql) {
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        // 使用正则表达式将多个连续的空格替换为一个空格
        sql = sql.replaceAll("\\s+", " ");
        // 这里只是一个简单的示例，实际的 SQL 解析可能会更复杂
        String[] parts = sql.split(" ");
        if (parts.length == 0) {
            return null;
        }
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("from") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }

    /**
     * 向当前线程关联的SqlExecutionInfo列表中添加一个SqlExecutionInfo对象
     *
     * @param info
     */
    public static void addData(SqlExecutionInfo info) {
        List<SqlExecutionInfo> currentData = THREAD_LOCAL.get();
        if (currentData == null) {
            currentData = new ArrayList<>();
            THREAD_LOCAL.set(currentData);
        }
        currentData.add(info);
    }

    /**
     * 移除与当前线程关联的SqlExecutionInfo列表
     */
    public static void removeData() {
        THREAD_LOCAL.remove();
    }
}