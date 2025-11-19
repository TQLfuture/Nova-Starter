package com.stater.nova.storage.plugin;

/**
 *
 * @author tql
 * @date: 2025/11/19
 * @time: 17:39
 * @desc:
 */
public class DefaultStoreCachePlugin implements IStoreCachePlugin {

    public static final DefaultStoreCachePlugin DEFAULT_STORE_CACHE_PLUGIN = new DefaultStoreCachePlugin();

    @Override
    public String cache(String key, String value) {
        return null;
    }

    @Override
    public String fromCache(String key) {
        return null;
    }
}
