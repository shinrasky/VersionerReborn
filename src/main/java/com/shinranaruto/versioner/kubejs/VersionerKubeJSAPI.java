package com.shinranaruto.versioner.kubejs;

import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.data.SponsorCategory;
import com.shinranaruto.versioner.util.Util;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* We encourage modders using KubeJS to call the methods in this class!
 * 我们鼓励使用 KubeJS 的魔改作者调用此类中的方法！
 */

public class VersionerKubeJSAPI {

    // ==================== Methods related to version information ====================

    public static String getVersionName() {
        return Versioner.versionData != null ? Versioner.versionData.getVersionName() : null;
    }

    public static int getVersionCode() {
        return Versioner.versionData != null ? Versioner.versionData.getVersionCode() : -1;
    }

    public static String getVersionFormat() {
        return Versioner.versionData != null ? Versioner.versionData.getVersionFormat() : null;
    }

    public static String getFormattedString(String format) {
        if (Versioner.versionData != null) {
            return Versioner.versionData.getFormattedString(format);
        }
        return format;
    }

    public static Map<String, List<String>> getChangelogsMap() {
        return Versioner.versionData != null && Versioner.versionData.getChangelogs() != null
                ? Versioner.versionData.getChangelogs().getMap()
                : null;
    }

    public static String getWelcomeMessage() {
        return Versioner.versionData != null ? Versioner.versionData.getWelcomeMessage() : null;
    }

    public static String getSponsorMessage() {
        return Versioner.versionData != null ? Versioner.versionData.getSponsorMessage() : null;
    }

    public static String getUpdateLink() {
        if (Versioner.versionData != null && Versioner.versionData.getUpdateLink() != null) {
            return Versioner.versionData.getUpdateLink();
        }
        return VersionerConfig.GENERAL.updateURL.get();
    }

    public static boolean isUpdateAvailable() {
        return Versioner.versionData != null && Versioner.versionData.isUpdateAvailable();
    }

    public static Object getVersionData() {
        return Versioner.versionData != null ? Versioner.versionData.getJsonObj() : null;
    }

    public static Object getVariableData() {
        return Versioner.versionData != null && Versioner.versionData.getVariables() != null
                ? Versioner.versionData.getVariables()
                : null;
    }

    public static boolean isReady() {
        return Versioner.versionData != null && Versioner.versionData.isReady();
    }

    public static String getModpackName() {
        return VersionerConfig.GENERAL.modpackName.get();
    }

    public static List<String> getMainMenuText() {
        return Util.getMainMenuTexts();
    }

    public static List<String> getMainMenuTextTooltip() {
        return Util.getMainMenuTooltipTexts();
    }

    public static boolean hasVariable(String name) {
        if (Versioner.versionData != null && Versioner.versionData.getVariables() != null) {
            return Versioner.versionData.getVariables().has(name);
        }
        return false;
    }

    public static Object getVariable(String name) {
        if (Versioner.versionData != null) {
            var variable = Versioner.versionData.getVariable(name);
            if (variable != null) {
                return variable;
            }
        }
        return null;
    }

    // Get Sponsor API
    public static SponsorsAPI getSponsors() {
        return new SponsorsAPI();
    }

    // ==================== Sponsors API ====================

    public static class SponsorsAPI {

        public boolean isPlayerInCategory(Player player, String category) {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return false;
            }
            return Versioner.versionData.getSponsors().isPlayerInCategory(player, category);
        }

        public boolean isSponsor(Player player) {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return false;
            }
            return Versioner.versionData.getSponsors().isSponsor(player);
        }

        public String getPlayerCategory(Player player) {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return null;
            }
            SponsorCategory category = Versioner.versionData.getSponsors().checkPlayer(player);
            return category != null ? category.getName() : null;
        }

        public String getPlayerCategoryDisplayName(Player player) {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return null;
            }
            SponsorCategory category = Versioner.versionData.getSponsors().checkPlayer(player);
            return category != null ? category.getDisplayName() : null;
        }

        public List<String> getCategories() {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return List.of();
            }
            return Versioner.versionData.getSponsors().getCategoryList().stream()
                    .map(SponsorCategory::getName)
                    .collect(Collectors.toList());
        }

        public String getCategoryDisplayName(String category) {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return null;
            }
            SponsorCategory cat = Versioner.versionData.getSponsors().getCategory(category);
            return cat != null ? cat.getDisplayName() : null;
        }

        public List<String> getSponsorNames(String category) {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return List.of();
            }
            SponsorCategory cat = Versioner.versionData.getSponsors().getCategory(category);
            if (cat != null) {
                return cat.getSponsors().stream()
                        .map(sponsor -> sponsor.getName() != null ? sponsor.getName() : sponsor.getUuid().toString())
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        public Object getAsData() {
            if (Versioner.versionData == null || Versioner.versionData.getSponsors() == null) {
                return null;
            }
            return Versioner.versionData.getSponsors().getJsonArray();
        }
    }
}
