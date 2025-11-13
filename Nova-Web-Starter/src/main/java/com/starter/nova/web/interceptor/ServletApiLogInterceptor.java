package com.starter.nova.web.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.nova.common.exception.BusinessException;
import com.starter.nova.common.model.BaseResult;
import com.starter.nova.web.anno.ApiLogIgnore;
import com.starter.nova.web.enums.RequestDirectionEnum;
import com.starter.nova.web.model.message.LogAspectModel;
import com.starter.nova.web.util.I18nUtil;
import com.starter.nova.web.util.WebObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.io.InputStreamSource;
import org.springframework.validation.DataBinder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


/**
 * @Author: tql
 */
@RequiredArgsConstructor
public class ServletApiLogInterceptor implements MethodInterceptor, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger("APP_IN");
    private static final String APP_OUT_RESPONSE_CODE = "res_code";
    private static final String RESPONSE_COST = "cost";


    /**
     * 慢日志
     */
    private static final String SLOW_LOG_PATTERN = "api.slow=>{}";
    /**
     * 普通日志
     */
    private static final String LOG_PATTERN = "api.log=>{}";
    /**
     * 错误日志
     */
    private static final String ERROR_LOG_PATTERN = "api.error=>{}";
    private final ObjectMapper objectMapper = WebObjectMapper.getInstance();


    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return invocation.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = invocation.proceed();
            long cost = System.currentTimeMillis() - startTime;
            ApiLogIgnore apiLogIgnore = invocation.getMethod().getAnnotation(ApiLogIgnore.class);
            if (apiLogIgnore == null) {
                logServiceCall(invocation, result, cost);
            }
            return result;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            MDC.put(RESPONSE_COST, String.valueOf(cost));
            if (e instanceof BusinessException businessException) {
                BusinessExceptionOutput consoleException = new BusinessExceptionOutput(businessException.getCode(), businessException.getMessage(), businessException.getArgs());
                //BusinessException 业务异常打印4000 标识错误码没有改造完成
                MDC.put(APP_OUT_RESPONSE_CODE, null != consoleException.getCode() ? String.valueOf(consoleException.getCode()) : "4000");
                String message = I18nUtil.getMessage(consoleException.getMessage(), consoleException.getArgs());
                consoleException.setMessage(message);
                String jsonStr = objectMapper.writeValueAsString(buildLogAspectDO(invocation.getArguments(), consoleException, cost));
                LOGGER.info("{}", jsonStr);
            }
            throw e;
        } finally {
            MDC.remove(APP_OUT_RESPONSE_CODE);
            MDC.remove(RESPONSE_COST);
        }
    }

    private void logServiceCall(MethodInvocation invocation, Object result, long cost) {
        if (invocation == null) {
            return;
        }
        try {
            BaseResult baseResult = objectMapper.convertValue(result, BaseResult.class);
            if (null != baseResult) {
                MDC.put(APP_OUT_RESPONSE_CODE, String.valueOf(baseResult.getCode()));
            }
            MDC.put(RESPONSE_COST, String.valueOf(cost));
            String jsonStr = objectMapper.writeValueAsString(buildLogAspectDO(invocation.getArguments(), result, cost));
            LOGGER.info("{}", jsonStr);
        } catch (Exception e) {
            LOGGER.warn("logServiceCall error", e);
        } finally {
            MDC.remove(APP_OUT_RESPONSE_CODE);
            MDC.remove(RESPONSE_COST);
        }
    }

    private LogAspectModel buildLogAspectDO(Object[] args, Object result, long cost) {
        HttpServletRequest request = getHttpServletRequest();
        return LogAspectModel.builder()
                .direction(RequestDirectionEnum.IN.getCode())
                .url(request.getRequestURI())
                .method(request.getMethod())
                .head(getHeaders(request))
                .args(getRequestArgs(args))
                .cost(cost)
                .result(result)
                .build();
    }

    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
            return attributes.getRequest();
        }

        throw new RuntimeException("Request fail");
    }

    /**
     * 获取http header部分数据
     *
     * @param request
     * @return
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Enumeration<String> enumerations = request.getHeaderNames();
        if (enumerations == null) {
            return null;
        }

        Map<String, String> headers = new HashMap<>(8);
        while (enumerations.hasMoreElements()) {
            String name = enumerations.nextElement();
            headers.put(name, request.getHeader(name));
        }

        return headers;
    }


    /**
     * 获取有效的请求参数（过滤掉不能序列化的）
     *
     * @param args
     * @return
     */
    private static Object getRequestArgs(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return args;
        }

        return Stream.of(args).filter(arg -> !needFilter(arg)).toArray();
    }

    /**
     * 是否需要过滤
     *
     * @param object
     * @return
     */
    private static boolean needFilter(Object object) {
        // 检查是否为 MultipartFile 数组
        if (object instanceof MultipartFile[] || object instanceof MultipartFile) {
            return true;
        }
        return object instanceof ServletRequest
                || object instanceof ServletResponse
                || object instanceof DataBinder
                || object instanceof InputStreamSource
                || object instanceof StandardMultipartHttpServletRequest;
    }

    /**
     * 业务异常输出,不需要继承异常，输出的时候不需要堆栈和调用链的信息（过多数据的数据阿里云无法读取json的字段）
     */
    @Data
    class BusinessExceptionOutput {
        private Integer code;
        private String message;
        private Object[] args;

        public BusinessExceptionOutput(Integer code, String message, Object[] args) {
            this.code = code;
            this.message = message;
            this.args = args;
        }
    }
}
