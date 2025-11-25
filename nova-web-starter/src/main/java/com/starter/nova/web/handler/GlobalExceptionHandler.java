package com.starter.nova.web.handler;

import com.starter.nova.common.exception.BusinessException;
import com.starter.nova.common.model.BaseResult;
import com.starter.nova.web.plugin.ExceptionHandlerPlugin;
import com.starter.nova.web.util.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 16:20
 * @desc:
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public final static String ACCESS_DENIED_EXCEPTION = "AccessDeniedException";

    public final static String AUTHORIZATION = "Authorization";

    private ApplicationContext context;

    public GlobalExceptionHandler(ApplicationContext context) {
        this.context = context;
    }

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<?> baseException(BusinessException ex) {
        plugin(ex);
        String message = I18nUtil.getMessage(ex.getMessage(), ex.getArgs());
        ex.setMessage(message);
        log.error(message, ex);
        Integer code = ex.getCode();
        if (code != null) {
            return ResponseEntity.ok(new BaseResult<>(code, message, null));
        }
        return ResponseEntity.ok(BaseResult.error(message));
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<?> validExceptionHandler(BindException ex) {
        plugin(ex);
        log.warn(ex.getMessage(), ex);
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        Set<String> messageSet
                = Optional.of(allErrors).orElse(new ArrayList<>()).stream().map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        if (messageSet.isEmpty()) {
            return ResponseEntity.ok(BaseResult.error(ex.getMessage()));
        }
        String message = I18nUtil.getMessage(messageSet.iterator().next());
        return ResponseEntity.ok(BaseResult.error(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> mismatchErrorHandler(MethodArgumentTypeMismatchException ex) {
        log.error("参数转换失败，参数类型不匹配，方法：{} 形参:{} 类型:{} msg:{} "
                , Objects.requireNonNull(ex.getParameter().getMethod()).getName(), ex.getName(), ex.getParameter()
                        .getParameterType(), ex.getMessage());
        plugin(ex);
        return ResponseEntity.ok(BaseResult.error("参数转换失败:" + ex.getMessage()));
    }


    @ExceptionHandler({Exception.class})
    public ResponseEntity<?> otherException(Exception ex) throws Exception {
        plugin(ex);
        log.error(ex.getMessage(), ex);
        Class<? extends Exception> clazz = ex.getClass();
        if (ACCESS_DENIED_EXCEPTION.equals(clazz.getSimpleName())) {
            // return ResponseEntity.ok(BaseResult.unauthorized(StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : clazz.getSimpleName()));
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
            if (request != null) {
                log.error("Permission Denied! url:{} token:{}", request.getRequestURI(), request.getHeader(AUTHORIZATION));
            } else {
                log.error("Permission Denied! ");
            }
            // 交给security处理接口访问异常
            throw ex;
        }
        return ResponseEntity.ok(BaseResult.error(StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : clazz.getSimpleName()));
    }

    public void plugin(Throwable throwable) {
        // 插件逻辑处理
        Map<String, ExceptionHandlerPlugin> beansOfType = context.getBeansOfType(ExceptionHandlerPlugin.class);
        for (ExceptionHandlerPlugin plugin : beansOfType.values()) {
            plugin.handle(throwable);
        }
    }

}