package com.starter.nova.mybatis.extend;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.springframework.core.Ordered;

/**
 * @author tql
 * @date: 2025/11/18
 * @time: 13:32
 * @desc:
 */
public interface NovaMybatisPlusPlugin extends Ordered {

    InnerInterceptor getInterceptor();

    @Override
    default int getOrder() {
        return 0;
    }
}
