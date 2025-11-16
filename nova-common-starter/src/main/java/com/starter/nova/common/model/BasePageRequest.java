package com.starter.nova.common.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 19:49
 * @desc:
 */
@Data
public class BasePageRequest {

    private long page = 1L;

    private  long size = 10L;

    public <T> Page<T> pageRequest() {
        return new Page(this.page, this.size);
    }
}
