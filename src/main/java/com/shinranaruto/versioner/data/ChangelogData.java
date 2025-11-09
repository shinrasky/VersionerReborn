package com.shinranaruto.versioner.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shinranaruto.versioner.VersionerConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChangelogData {
    private final JsonObject jsonObj;
    private final Map<String, List<String>> map = new LinkedHashMap<>();

    public ChangelogData(JsonObject jsonObj) {
        this.jsonObj = jsonObj;
        initialize();
    }

    private void initialize() {
        for (var entry : jsonObj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            JsonArray array = value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();

            List<String> versionChangelog = new ArrayList<>();
            for (var changelogLine : array) {
                versionChangelog.add(changelogLine.getAsString());
            }
            map.put(key, versionChangelog);
        }
    }

    public List<String> get(String version) {
        return map.get(version);
    }

    public JsonObject getJsonObj() { return jsonObj; }
    public Map<String, List<String>> getMap() { return map; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (var entry : map.entrySet()) {
            builder.append(entry.getKey()).append(":\n");
            for (String line : entry.getValue()) {
                builder.append(VersionerConfig.GENERAL.changelogPrefix.get()).append(line).append("\n");
            }
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
