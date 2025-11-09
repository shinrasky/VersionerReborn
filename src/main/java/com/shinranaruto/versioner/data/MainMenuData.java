package com.shinranaruto.versioner.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shinranaruto.versioner.util.Util;

import java.util.List;

public class MainMenuData {
    private List<String> text;
    private List<String> tooltipText;
    private String clickLink;

    public MainMenuData(JsonObject jsonObj) {
        initialize(jsonObj);
    }

    private void initialize(JsonObject jsonObj) {
        if (jsonObj.has("text")) {
            JsonArray arr = jsonObj.get("text").getAsJsonArray();
            text = Util.jsonStringArrayToList(arr);
        }
        if (jsonObj.has("tooltipText")) {
            JsonArray arr = jsonObj.get("tooltipText").getAsJsonArray();
            tooltipText = Util.jsonStringArrayToList(arr);
        }
        if (jsonObj.has("clickLink")) {
            clickLink = jsonObj.get("clickLink").getAsString();
        }
    }

    public List<String> getText() { return text; }
    public List<String> getTooltipText() { return tooltipText; }
    public String getClickLink() { return clickLink; }
}
