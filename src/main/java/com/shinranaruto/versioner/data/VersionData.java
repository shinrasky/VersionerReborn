package com.shinranaruto.versioner.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.util.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VersionData {
    private final JsonObject jsonObj;
    private String versionName;
    private String versionFormat;
    private ChangelogData changelogs;
    private int versionCode = -1;
    private SponsorData sponsors;
    private boolean isReady = false;
    private JsonObject variables;
    private String updateLink;
    private String welcomeMessage;
    private String sponsorMessage;
    private MainMenuData mainMenu;

    public VersionData(JsonObject jsonObj) {
        this(jsonObj, true);
    }

    public VersionData(JsonObject jsonObj, boolean doInitialize) {
        this.jsonObj = jsonObj;
        if (doInitialize) {
            try {
                initialize();
            } catch (Exception e) {
                Versioner.LOGGER.error("Failed to initialize version data object.", e);
            }
        }
    }

    public void initialize() {
        if (jsonObj.has("versionName")) {
            versionName = jsonObj.get("versionName").getAsString();
        }
        if (jsonObj.has("versionCode")) {
            versionCode = jsonObj.get("versionCode").getAsInt();
        }
        if (jsonObj.has("versionFormat")) {
            versionFormat = jsonObj.get("versionFormat").getAsString();
        }
        if (jsonObj.has("updateLink")) {
            updateLink = jsonObj.get("updateLink").getAsString();
        }
        if (jsonObj.has("welcomeMessage")) {
            welcomeMessage = jsonObj.get("welcomeMessage").toString();
        }
        if (jsonObj.has("sponsorMessage")) {
            sponsorMessage = jsonObj.get("sponsorMessage").toString();
        }
        if (jsonObj.has("changelogs")) {
            var obj = jsonObj.get("changelogs").getAsJsonObject();
            if (obj != null) {
                changelogs = new ChangelogData(obj);
            }
        }
        if (jsonObj.has("mainMenu")) {
            var obj = jsonObj.get("mainMenu").getAsJsonObject();
            if (obj != null) {
                mainMenu = new MainMenuData(obj);
            }
        }
        if (jsonObj.has("sponsors")) {
            var arr = jsonObj.get("sponsors").getAsJsonArray();
            if (arr != null) {
                sponsors = new SponsorData(arr);
            }
        }
        if (jsonObj.has("variables")) {
            variables = jsonObj.get("variables").getAsJsonObject();
        }
        isReady = true;
    }

    public String getEntryString(String key) {
        if (!isReady) return "";
        switch (key) {
            case "currentVersionName":
                return VersionerConfig.CURRENT_VERSION.versionName.get();
            case "currentVersionCode":
                return String.valueOf(VersionerConfig.CURRENT_VERSION.versionCode.get());
            case "versionName":
                return String.valueOf(versionName);
            case "versionFormat":
                return String.valueOf(versionFormat);
            case "versionCode":
                return String.valueOf(versionCode);
            case "sponsors":
                return String.valueOf(sponsors);
            case "changelogs":
                return String.valueOf(changelogs);
            case "updateLink":
                return String.valueOf(updateLink);
            case "welcomeMessage":
                return String.valueOf(welcomeMessage);
            case "sponsorMessage":
                return String.valueOf(sponsorMessage);
            case "modpackName":
                return Util.i18nSafe(VersionerConfig.GENERAL.modpackName.get());
            case "isUpdateAvailable":
                Boolean updateAvailable = isUpdateAvailableOrNull();
                if (updateAvailable == null) {
                    return "§c" + Util.i18nSafe("versioner.variables.update_available.fail");
                } else if (updateAvailable) {
                    return "§b" + Util.i18nSafe("versioner.variables.update_available.true",
                            "§e" + Util.i18nSafe("versioner.variables.update_available.latest",
                                    "§a" + (versionName != null ? versionName : "§cN/A")));
                } else {
                    return "§a" + Util.i18nSafe("versioner.variables.update_available.false");
                }
            default:
                String variable = getVariableString(key);
                return variable != null ? variable : "%" + key + "%";
        }
    }

    public JsonElement getVariable(String name) {
        if (variables != null && variables.has(name)) {
            return variables.get(name);
        }
        return Util.currentVariables.get(name);
    }

    public String getVariableString(String name) {
        JsonElement element = getVariable(name);
        if (element == null) return null;
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else {
            return element.toString();
        }
    }

    public String getFormattedString(String format) {
        String formattedString = format;
        Set<String> possibleKeys = new HashSet<>(Util.currentVariables.keySet());
        if (variables != null) {
            for (var entry : variables.entrySet()) {
                possibleKeys.add(entry.getKey());
            }
        }
        possibleKeys.addAll(Arrays.asList(
                "versionName", "versionCode", "sponsors", "changelogs", "updateLink",
                "welcomeMessage", "sponsorMessage", "isUpdateAvailable", "currentVersionName",
                "currentVersionFormat", "currentVersionCode", "modpackName"
        ));

        for (String key : possibleKeys) {
            formattedString = formattedString.replace("%" + key + "%", getEntryString(key));
        }
        return formattedString;
    }

    public String getFormattedVersionName() {
        return getFormattedString(versionFormat != null ? versionFormat : "");
    }

    public Set<String> variableNamesSet() {
        Set<String> keySet = new HashSet<>();
        if (variables != null) {
            for (var entry : variables.entrySet()) {
                keySet.add(entry.getKey());
            }
        }
        return keySet;
    }

    public boolean isUpdateAvailable() {
        Boolean result = isUpdateAvailableOrNull();
        return result != null && result;
    }

    public Boolean isUpdateAvailableOrNull() {
        if (versionCode >= 0) {
            return versionCode > VersionerConfig.CURRENT_VERSION.versionCode.get();
        } else if (versionName == null) {
            return null;
        } else {
            return Util.compareVersionNames(versionName, VersionerConfig.CURRENT_VERSION.versionName.get()) < 0;
        }
    }

    public Integer getVersionDiff() {
        if (versionCode >= 0) {
            return versionCode - VersionerConfig.CURRENT_VERSION.versionCode.get();
        } else {
            return null;
        }
    }

    public List<String> getCurrentChangelogs() {
        return versionName != null && changelogs != null ? changelogs.get(versionName) : null;
    }

    // Getters
    public JsonObject getJsonObj() { return jsonObj; }
    public String getVersionName() { return versionName; }
    public String getVersionFormat() { return versionFormat; }
    public ChangelogData getChangelogs() { return changelogs; }
    public int getVersionCode() { return versionCode; }
    public SponsorData getSponsors() { return sponsors; }
    public boolean isReady() { return isReady; }
    public JsonObject getVariables() { return variables; }
    public String getUpdateLink() { return updateLink; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public String getSponsorMessage() { return sponsorMessage; }
    public MainMenuData getMainMenu() { return mainMenu; }

    @Override
    public String toString() {
        return jsonObj.toString();
    }
}
