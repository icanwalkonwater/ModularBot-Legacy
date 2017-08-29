package com.jesus_crie.modularbot.stats.bundle;

import java.util.HashMap;

public class BundleBuilder {

    private final HashMap<Integer, Object> data = new HashMap<>();

    public BundleBuilder append(String key, Object data) {
        this.data.put(key.hashCode(), data);
        return this;
    }

    public BundleBuilder append(int key, Object data) {
        this.data.put(key, data);
        return this;
    }

    public BundleBuilder merge(BundleBuilder builder) {
        data.putAll(builder.data);
        return this;
    }

    public BundleBuilder merge(Bundle bundle) {
        data.putAll(bundle.getRaw());
        return this;
    }

    public Bundle build() {
        return new Bundle(data);
    }
}
