package com.starter.nova.common.model;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 19:59
 * @desc:
 */
@Data
public class BasePage<T> implements Serializable {

    private List<T> records;
    private boolean first;
    private boolean last;
    private long page;
    private long size;
    private long totalPages;
    private long totalElements;

    public BasePage() {
    }

    public BasePage(List<T> data, boolean first, boolean last, long page, long size, long totalPages, long totalElements) {
        this.records = data;
        this.first = first;
        this.last = last;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public BasePage(Page<T> page) {
        this.records = page.getRecords();
        this.first = !page.hasPrevious();
        this.last = !page.hasNext();
        this.page = page.getCurrent();
        this.size = page.getSize();
        this.totalPages = page.getPages();
        this.totalElements = page.getTotal();
    }

    public <R> BasePage(Page<R> page, List<T> records) {
        this.records = records;
        this.first = !page.hasPrevious();
        this.last = !page.hasNext();
        this.page = page.getCurrent();
        this.size = page.getSize();
        this.totalPages = page.getPages();
        this.totalElements = page.getTotal();
    }

    public <R> BasePage(BasePage<R> basePage, List<T> records) {
        this.records = records;
        this.first = basePage.first;
        this.last = basePage.last;
        this.page = basePage.page;
        this.size = basePage.size;
        this.totalPages = basePage.totalPages;
        this.totalElements = basePage.totalElements;
    }

    public static <T, R> BasePage<T> convert(Page<R> page, Function<List<R>, List<T>> function) {
        List<T> list = (List) function.apply(page.getRecords());
        return new BasePage<T>(page, list);
    }

    public static <T, R> BasePage<T> simpleConvert(Page<R> page, Function<R, T> function) {
        List<R> records = (List<R>) (!CollectionUtils.isEmpty(page.getRecords()) ? page.getRecords() : new ArrayList());
        List<T> list = (List) records.stream().map(function).collect(Collectors.toList());
        return new BasePage<T>(page, list);
    }

    public static <T, R> BasePage<T> convert(BasePage<R> basePage, Function<List<R>, List<T>> function) {
        List<T> list = (List) function.apply(basePage.getRecords());
        return new BasePage<T>(basePage, list);
    }

    public static <T, R> BasePage<T> simpleConvert(BasePage<R> basePage, Function<R, T> function) {
        List<R> records = (List<R>) (!CollectionUtils.isEmpty(basePage.getRecords()) ? basePage.getRecords() : new ArrayList());
        List<T> list = (List) records.stream().map(function).collect(Collectors.toList());
        return new BasePage<T>(basePage, list);
    }

    public static <T> BasePage<T> dynamicPage(List<T> records, BasePageRequest pageRequest) {
        List<T> record = null;
        Object var7;
        if (!CollectionUtils.isEmpty(records)) {
            var7 = (List) records.stream().skip((pageRequest.getPage() - 1L) * pageRequest.getSize()).limit(pageRequest.getSize()).collect(Collectors.toList());
        } else {
            var7 = new ArrayList();
        }

        long totalPages = (long) records.size() / pageRequest.getSize() + (long) ((long) records.size() % pageRequest.getSize() == 0L ? 0 : 1);
        boolean first = pageRequest.getPage() == 1L;
        boolean last = totalPages == 0L || pageRequest.getPage() == totalPages;
        return new BasePage<T>((List) var7, first, last, pageRequest.getPage(), pageRequest.getSize(), totalPages, (long) records.size());
    }
}
