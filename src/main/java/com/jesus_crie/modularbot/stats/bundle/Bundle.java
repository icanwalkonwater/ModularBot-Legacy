package com.jesus_crie.modularbot.stats.bundle;

import com.jesus_crie.modularbot.utils.MiscUtils;
import org.apache.commons.collections4.map.UnmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class Bundle {

    private final Map<Integer, Object> datas;

    public Bundle(HashMap<Integer, Object> datas) {
        MiscUtils.notEmpty(datas, "datas");
        this.datas = UnmodifiableMap.unmodifiableMap(datas);
    }

    public String getString(String key) {
        return getString(key.hashCode());
    }

    public String getString(int key) {
        return (String) datas.get(key);
    }

    public Integer getInteger(String key) {
        return getInteger(key.hashCode());
    }

    public Integer getInteger(int key) {
        return (Integer) datas.get(key);
    }

    public Bundle getSubBundle(String key) {
        return getSubBundle(key.hashCode());
    }

    public Bundle getSubBundle(int key) {
        return (Bundle) datas.get(key);
    }

    public Object getObject(String key) {
        return getObject(key.hashCode());
    }

    public Object getObject(int key) {
        return datas.get(key);
    }

    public Map<Integer, Object> getRaw() {
        return datas;
    }
}
