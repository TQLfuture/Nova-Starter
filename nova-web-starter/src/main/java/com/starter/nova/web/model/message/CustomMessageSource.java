package com.starter.nova.web.model.message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

public class CustomMessageSource extends AbstractMessageSource implements IMessageSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMessageSource.class);

    @Override
    public void setParent(MessageSource messageSource) {
        this.setParentMessageSource(messageSource);
        this.setAlwaysUseMessageFormat(true);
    }

    @Override
    public Message resolveMessage(ReloadableResourceBundleMessageSource parentMessageSource, String code, Object[] args, Locale locale) {
        return this.resolveMessage(parentMessageSource, code, args, null, locale);
    }

    @Override
    public Message resolveMessage(ReloadableResourceBundleMessageSource parentMessageSource, String code, Object[] args, String defaultMessage, Locale locale) {
        String desc = null;
        try {
            desc = Objects.requireNonNull(this.getParentMessageSource()).getMessage(code, null, locale);
            if(desc == null){
                // 如果为空，则默认取简体中文对的国际化
                desc = Objects.requireNonNull(this.getParentMessageSource()).getMessage(code, null, Locale.SIMPLIFIED_CHINESE);
            }
        } catch (NoSuchMessageException e) {
            LOGGER.warn("resolveMessage not found message for code={}", code);
        }

        if (StringUtils.isBlank(desc) && StringUtils.isNotBlank(defaultMessage)) {
            desc = defaultMessage;
        }

        if (StringUtils.isNotBlank(desc) &&(args != null && args.length > 0)) {
            desc = this.createMessageFormat(desc, locale).format(args);
        }

        if (StringUtils.isBlank(desc)) {
            desc = code;
        }
        Message message = new Message(code, desc);
        LOGGER.debug("resolve message: code={}, message={}, language={}", code, message, locale);
        return message;
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String msg = null;
        try {
            msg = Objects.requireNonNull(this.getParentMessageSource()).getMessage(code, null, locale);
        } catch (NoSuchMessageException var6) {
            LOGGER.warn("resolveCode not found message for code={}", code);
        }

        return StringUtils.isNotBlank(msg) ? this.createMessageFormat(msg, locale) : null;
    }
}
