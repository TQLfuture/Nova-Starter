package com.starter.nova.web.util;

import com.starter.nova.web.model.message.Message;
import com.starter.nova.web.model.message.MessageAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 16:30
 * @desc:
 */
@Slf4j
public class I18nUtil {

    public static String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        log.info("I18nUtil getMessage(String code), {}", locale);
        return getMessage(code, locale);
    }

    public static String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        log.info("I18nUtil getMessage(String code, Object... args), {}", locale);
        return getMessage(code, locale, args);
    }

    public static String getMessage(String code, Locale locale, Object... args) {
        try {
            log.info("I18nUtil getMessage(String code, Locale locale, Object... args), {}", locale);
            Message message = MessageAccessor.getMessage(code, args, locale);
            return message != null ? message.getDesc() : code;
        } catch (NoSuchMessageException e) {
            return code;
        }
    }
}
