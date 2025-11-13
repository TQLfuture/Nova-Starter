package com.starter.nova.mybatis.interceptor;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ParameterUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.nova.mybatis.model.pojo.SqlExecutionInfo;
import com.starter.nova.mybatis.utils.m.CustomObjectMapper;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 16:47
 * @desc:
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),})
public class AdvancedMybatisPlusInterceptor extends MybatisPlusInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger("APP_SQL");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final ObjectMapper objectMapper = CustomObjectMapper.getInstance();
    private final SqlLoggingInterceptor sqlLoggingInterceptor = new SqlLoggingInterceptor();

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            // 首先，应用MybatisPlusInterceptor中的默认插件
            Object wrappedTarget = super.plugin(target);
            // 然后，应用自定义的CustomInterceptor
            return Plugin.wrap(wrappedTarget, sqlLoggingInterceptor);
        } else {
            return super.plugin(target);
        }
    }

    @Intercepts({
            @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
            @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
            @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                    RowBounds.class, ResultHandler.class}),
            @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                    RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),})
    public class SqlLoggingInterceptor implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            Object result;
            try {
                // 记录开始时间
                long startTime = System.currentTimeMillis();

                // 调用原始方法
                result = invocation.proceed();

                // 记录打印sql执行日志
                logSqlExecution(result, startTime);
            } finally {
                // 移除对象，避免内存泄漏
                SqlExecutionInfoHolder.removeData();
            }
            return result;
        }

        /**
         * 记录SQL执行情况的方法。
         *
         * @param result    SQL执行后返回的结果
         * @param startTime SQL执行开始的时间戳
         */
        private void logSqlExecution(Object result, long startTime) {
            try {
                // 获取当前线程中存储的SqlExecutionInfo列表
                List<SqlExecutionInfo> sqlExecutionInfoList = SqlExecutionInfoHolder.getData();

                // 遍历SqlExecutionInfo列表
                for (SqlExecutionInfo sqlExecutionInfo : sqlExecutionInfoList) {
                    // 如果sqlExecutionInfo中的执行时间未设置，则计算并设置
                    sqlExecutionInfo.setStartTime(startTime);
                    if (sqlExecutionInfo.getEndTime() == null) {
                        sqlExecutionInfo.setEndTime(System.currentTimeMillis());
                    }

                    // 构建日志内容
                    StringBuilder logBuilder = new StringBuilder();
                    logBuilder.append("Execution Time: ").append(dateTimeFormatter.format(LocalDateTime.now())).append("\n");
                    logBuilder.append("SQL: ").append(sqlExecutionInfo.getSqlStatement()).append("\n");
                    String paramString = writeValueAsString(sqlExecutionInfo.getParameters());
                    logBuilder.append("Parameters: ").append(paramString).append("\n");
                    logBuilder.append("Execution Cost: ").append(sqlExecutionInfo.getElapsedTime()).append(" ms\n");

                    // 记录日志
                    LOGGER.info(logBuilder.toString());
                }
            } catch (Exception e) {
                // 如果发生异常，记录异常信息
                LOGGER.error("Error while logging SQL execution: ", e);
            }
        }

    }

    /**
     * 自定义预分页内部拦截器类，继承自 MyBatis-Plus 的 PaginationInnerInterceptor。
     * 用于在执行分页查询前记录 SQL 执行信息。
     */
    public static class PrePaginationInnerInterceptor extends PaginationInnerInterceptor {

        @Override
        public boolean willDoQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                                   ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
            return true;
        }

        /**
         * 在执行查询前记录 SQL 执行信息。
         *
         * @param executor      执行器对象，用于执行 SQL 查询
         * @param ms            MappedStatement 对象，包含 SQL 语句的映射信息
         * @param parameter     参数对象，通常是一个 JavaBean 或 Map
         * @param rowBounds     分页信息对象，用于控制查询结果的范围
         * @param resultHandler 结果处理器对象，用于处理查询结果
         * @param boundSql      BoundSql 对象，包含 SQL 语句和参数映射信息
         * @throws SQLException 当执行查询过程中发生错误时抛出
         */
        @Override
        public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                                ResultHandler resultHandler, BoundSql boundSql) {
            logPagedSqlExecution(ms, parameter, boundSql);
        }


        @Override
        public void setProperties(Properties properties) {
            // DO Nothing
        }

        /**
         * 在执行查询前，记录 SQL 执行信息。
         *
         * @param ms        MappedStatement 对象，包含 SQL 语句的映射信息
         * @param parameter 参数对象，通常是一个 JavaBean 或 Map
         * @param boundSql  BoundSql 对象，包含 SQL 语句和参数映射信息
         */
        private void logPagedSqlExecution(MappedStatement ms, Object parameter, BoundSql boundSql) {
            try {
                IPage<?> page = ParameterUtils.findPage(parameter).orElse(null);
                if (isPageSearchCountInvalid(page)) {
                    return;
                }
                MappedStatement countMs = buildCountMappedStatement(ms, page.countId());
                BoundSql countSql = getCountSql(page, ms, parameter, boundSql, countMs);
                SqlExecutionInfoHolder.addData(ms, countSql);
            } catch (Exception e) {
                LOGGER.error("Error occurred during SQL execution in paged query: ", e);
            }
        }

        /**
         * 判断分页搜索计数是否无效。
         *
         * @param page 分页对象
         * @return 如果分页搜索计数无效，则返回 true；否则返回 false
         */
        private boolean isPageSearchCountInvalid(IPage<?> page) {
            return page == null || page.getSize() < 0 || !page.searchCount();
        }

        /**
         * 获取计数查询的 BoundSql 对象。
         *
         * @param page      分页对象
         * @param ms        MappedStatement 对象，包含 SQL 语句的映射信息
         * @param parameter 参数对象，通常是一个 JavaBean 或 Map
         * @param boundSql  BoundSql 对象，包含 SQL 语句和参数映射信息
         * @param countMs   计数查询的 MappedStatement 对象
         * @return 计数查询的 BoundSql 对象
         */
        private BoundSql getCountSql(IPage<?> page, MappedStatement ms, Object parameter, BoundSql boundSql,
                                     MappedStatement countMs) {
            if (countMs != null) {
                return countMs.getBoundSql(parameter);
            } else {
                countMs = buildAutoCountMappedStatement(ms);
                String countSqlStr = autoCountSql(page, boundSql.getSql());
                PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
                return new BoundSql(countMs.getConfiguration(), countSqlStr, mpBoundSql.parameterMappings(), parameter);
            }
        }
    }

    /**
     * SQL执行内部拦截器类，用于在查询前记录SQL执行信息。
     */
    public static class SqlExecutionInnerInterceptor implements InnerInterceptor {

        /**
         * 在执行查询前，记录SQL执行信息。
         *
         * @param executor      执行器对象，用于执行SQL查询
         * @param ms            MappedStatement 对象，包含 SQL 语句的映射信息
         * @param parameter     参数对象，通常是一个 JavaBean 或 Map
         * @param rowBounds     分页信息对象，用于控制查询结果的范围
         * @param resultHandler 结果处理器对象，用于处理查询结果
         * @param boundSql      BoundSql 对象，包含 SQL 语句和参数映射信息
         * @throws SQLException 当执行查询过程中发生错误时抛出
         */
        @Override
        public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                                ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
            try {
                // 分页插件可能会生成一个count(*)条数的查询，到这里已经是执行过的，所以需要一个结束的时间
                List<SqlExecutionInfo> sqlExecutionInfoList = SqlExecutionInfoHolder.getData();
                for (SqlExecutionInfo sqlExecutionInfo : sqlExecutionInfoList) {
                    ParameterUtils.findPage(parameter).ifPresent(page -> {
                        sqlExecutionInfo.setEndTime(System.currentTimeMillis());
                    });
                }
                SqlExecutionInfoHolder.addData(ms, boundSql);
            } catch (Exception e) {
                // 如果发生异常，记录异常信息
                LOGGER.error("Error while beforeQuery SQL execution: ", e);
            }
        }

        @Override
        public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
            try {
                BoundSql boundSql = ms.getBoundSql(parameter);
                SqlExecutionInfoHolder.addData(ms, boundSql);
            } catch (Exception e) {
                // 如果发生异常，记录异常信息
                LOGGER.error("Error while beforeUpdate SQL execution: ", e);
            }
        }
    }


    /**
     * 定义一个方法，将对象转换为JSON字符串
     *
     * @param value
     * @return
     */
    private String writeValueAsString(Object value) {
        try {
            // 如果传入的对象为空，则返回空字符串
            if (value == null) {
                return "";
            }

            // 使用ObjectMapper对象将传入的对象转换为JSON字符串
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            // 如果在转换过程中发生异常，则记录错误信息，并返回对象的字符串表示形式
            LOGGER.error("Error while serializing value:{}", value, e);
            return value.toString();
        }
    }
}