package com.starter.nova.mybatis.model.pojo;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 16:05
 * @desc:
 */
@Data
public class SqlExecutionInfo {

    /**
     * sql语句
     */
    private String sqlStatement;

    /**
     * 参数
     */
    private Object parameters;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 耗时
     */
    private Long elapsedTime;

    /**
     * sql语句中第一个出现的表名
     */
    private String firstTableName;

    public Long getElapsedTime() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return endTime - startTime;
    }

    public String getFullSql(String paramString) {
        if (sqlStatement == null) {
            return null;
        }
        if (paramString == null || !paramString.startsWith("[") || !paramString.endsWith("]")) {
            return sqlStatement;
        }

        List<String> paramsList = Arrays.stream(paramString.substring(1, paramString.length() - 1).split(","))
                .filter(StringUtils::isNotBlank).toList();

        String[] sqlParts = sqlStatement.split("\\?", -1);

        StringBuilder fullSqlBuilder = new StringBuilder();

        int minSize = Math.min(sqlParts.length - 1, paramsList.size());

        for (int i = 0; i < sqlParts.length; i++) {
            fullSqlBuilder.append(sqlParts[i]);
            if (i < minSize) {
                fullSqlBuilder.append(paramsList.get(i));
            } else if (i < sqlParts.length - 1) {
                fullSqlBuilder.append("?");
            }
        }

        return fullSqlBuilder.toString();
    }
}