package com.starter.nova.web.model.message;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 从 classpath 下的 message.properties 里获取，使用者可提供默认消息。
 * 该消息存取器里的静态方法返回的 Message 对象不会为 null。
 *
 * @author bojiangzhou 2018/09/15
 * @see MessageSourceAccessor
 */
public class MessageAccessor {

    private static final IMessageSource CUSTOM_MESSAGE_SOURCE;
    private static final ReloadableResourceBundleMessageSource PARENT_MESSAGE_SOURCE;

    private static final List<String> basenames = Arrays.asList(
            "classpath:messages/messages",
            "classpath:messages/messages_core",
            "classpath:messages/messages_redis",
            "classpath:messages/messages_export",
            "classpath:messages/messages_ext",
            "classpath:messages/messages_default",
            "classpath:messages/messages_redisson",
            "classpath:messages/messages_security_auth",
            "classpath:messages/messages_security_resource"
    );

    private MessageAccessor() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    static {
        PARENT_MESSAGE_SOURCE = new ReloadableResourceBundleMessageSource();
        PARENT_MESSAGE_SOURCE.setBasenames(getBasenames());
        PARENT_MESSAGE_SOURCE.setDefaultEncoding("UTF-8");

        Class clazz;
        IMessageSource customMessageSource;
        try {
            clazz = Class.forName("com.freedom.web.message.CustomMessageSource");
            // 依赖了starter
            customMessageSource = (IMessageSource) clazz.newInstance();
        } catch (Exception e) {
            customMessageSource = new IMessageSource() {
                @Override
                public void setParent(MessageSource messageSource) {
                }
            };
        }
        customMessageSource.setParent(PARENT_MESSAGE_SOURCE);
        CUSTOM_MESSAGE_SOURCE = customMessageSource;
    }

    public static String[] getBasenames() {
        return toStringArray(basenames.toArray());
    }

    /**
     * 添加资源文件位置
     *
     * @param names 如 <code>classpath:messages/messages_core</code>
     */
    public static void addBasenames(String... names) {
        PARENT_MESSAGE_SOURCE.addBasenames(names);
    }

    /**
     * 覆盖默认资源文件位置
     *
     * @param names 如 <code>classpath:messages/messages_core</code>
     */
    public static void setBasenames(String... names) {
        PARENT_MESSAGE_SOURCE.setBasenames(names);
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, String defaultMessage) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, String defaultMessage, Locale locale) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, null, defaultMessage, locale);
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, Object[] args, String defaultMessage) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, args, defaultMessage, locale);
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, null, LocaleContextHolder.getLocale());
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, Locale locale) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, null, locale);
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, Object[] args) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, args, LocaleContextHolder.getLocale());
    }

    /**
     * 从本地消息文件获取多语言消息
     */
    public static Message getMessage(String code, Object[] args, Locale locale) {
        return CUSTOM_MESSAGE_SOURCE.resolveMessage(PARENT_MESSAGE_SOURCE, code, args, locale);
    }

    public static String[] toStringArray(final Object[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new String[0];
        }

        final String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].toString();
        }

        return result;
    }
}
