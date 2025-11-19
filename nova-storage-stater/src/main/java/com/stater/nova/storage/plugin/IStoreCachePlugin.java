package com.stater.nova.storage.plugin;

/**
 * @author tql
 * @date: 2025/11/19
 * @time: 17:34
 * @desc:
 */
public interface IStoreCachePlugin {

    default String cachePrefix(String objectKey) {
        return "store:cache:plugin:";
    }

    /**
     * 缓存数据
     *
     * @param key   缓存key
     * @param value 缓存数据
     * @return 缓存数据
     */
    String cache(String key, String value);

    /**
     * 从缓存中获取数据
     *
     * @param key 缓存key
     * @return 缓存数据
     */
    String fromCache(String key);
}
