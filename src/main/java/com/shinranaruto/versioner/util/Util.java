package com.shinranaruto.versioner.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.commands.CommandHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.players.PlayerList;

import java.util.*;

public class Util {

    public static final Map<String, JsonElement> currentVariables = new HashMap<>();
    public static final String CT_NAMESPACE = "mods.versioner.";

    public static List<String> getAllPlayerNames(PlayerList playerList) {
        List<String> names = new ArrayList<>();
        playerList.getPlayers().forEach(player -> names.add(player.getName().getString()));
        return names;
    }

    public static MutableComponent i18n(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static int compareVersionNames(String a, String b) {
        if (a == null || b == null) {
            return 0;
        }
        String separator = "[^\\d]+";
        String[] arrayA = a.split(separator);
        String[] arrayB = b.split(separator);
        int i = 0;
        while (i < arrayA.length || i < arrayB.length) {
            String seqA = i < arrayA.length ? arrayA[i] : "0";
            String seqB = i < arrayB.length ? arrayB[i] : "0";
            try {
                int numA = Integer.parseInt(seqA);
                int numB = Integer.parseInt(seqB);
                if (numA > numB) return 1;
                if (numA < numB) return -1;
            } catch (NumberFormatException ignored) {
                // 如果无法解析为数字，则按字符串比较
                int cmp = seqA.compareTo(seqB);
                if (cmp != 0) return cmp;
            }
            i++;
        }
        return 0;
    }

    public static String getCurrentEntryString(String key) {
        switch (key) {
            case "currentVersionName":
                return VersionerConfig.CURRENT_VERSION.versionName.get();
            case "currentVersionCode":
                return String.valueOf(VersionerConfig.CURRENT_VERSION.versionCode.get());
            case "isUpdateAvailable":
                return "§c" + i18nSafe("versioner.variables.update_available.fail");
            case "modpackName":
                return i18nSafe(VersionerConfig.GENERAL.modpackName.get());
            default:
                String variable = getCurrentVariableString(key);
                return variable != null ? variable : "%" + key + "%";
        }
    }

    public static JsonElement getCurrentVariable(String key) {
        return currentVariables.get(key);
    }

    public static String getCurrentVariableString(String key) {
        JsonElement element = getCurrentVariable(key);
        if (element == null) return null;
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        } else {
            return element.toString();
        }
    }

    public static String i18nSafe(String key, Object... objects) {
        return Versioner.PROXY.i18nSafe(key, objects);
    }

    public static void saveVariables() {
        for (String entry : VersionerConfig.CURRENT_VERSION.variables.get()) {
            int equalsIndex = entry.indexOf('=');
            if (equalsIndex > 0 && equalsIndex < entry.length() - 1) {
                String key = entry.substring(0, equalsIndex).trim();
                String value = entry.substring(equalsIndex + 1).trim();
                try {
                    currentVariables.put(key, JsonParser.parseString(value));
                } catch (JsonSyntaxException e) {
                    Versioner.LOGGER.error("{} is not a valid JSON element!", value, e);
                }
            }
        }
    }

    public static String getFormattedString(String format) {
        if (Versioner.versionData != null && Versioner.versionData.isReady()) {
            return Versioner.versionData.getFormattedString(format);
        } else {
            return getCurrentFormattedString(format);
        }
    }

    public static String getCurrentFormattedString(String format) {
        String formattedString = format;
        Set<String> possibleKeys = new HashSet<>(currentVariables.keySet());
        possibleKeys.addAll(Arrays.asList(
                "currentVersionName", "currentVersionCode", "isUpdateAvailable", "modpackName"
        ));

        for (String key : possibleKeys) {
            formattedString = formattedString.replace("%" + key + "%", getCurrentEntryString(key));
        }
        return formattedString;
    }

    public static String getCurrentFormattedVersion() {
        return getCurrentFormattedString(VersionerConfig.CURRENT_VERSION.versionFormat.get());
    }

    public static String getUpdateLink() {
        if (Versioner.versionData != null && Versioner.versionData.getUpdateLink() != null) {
            return Versioner.versionData.getUpdateLink();
        }
        return VersionerConfig.GENERAL.updateURL.get();
    }

    public static List<Component> getUpdateChatMessage() {
        List<Component> components = new ArrayList<>();

        Component delimiter = Component.literal(VersionerConfig.GENERAL.delimiter.get())
                .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE));

        MutableComponent currentVersion = Component.translatable("versioner.variables.current_version")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD))
                .append(Component.literal(" "))
                .append(Component.literal(getCurrentFormattedVersion())
                        .withStyle(style -> style.withColor(ChatFormatting.YELLOW)));

        Integer diff = Versioner.versionData != null ? Versioner.versionData.getVersionDiff() : null;

        MutableComponent latestVersion = Component.translatable("versioner.variables.latest_version")
                .withStyle(style -> style.withColor(ChatFormatting.DARK_GREEN))
                .append(Component.literal(" "))
                .append(Component.literal(
                        Versioner.versionData != null ? Versioner.versionData.getFormattedVersionName() : "N/A"
                ).withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                .append(Component.literal(" "));

        if (diff != null) {
            latestVersion.append(Component.translatable("versioner.variables.n_versions_newer", diff.toString())
                    .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE)));
        }

        MutableComponent changelogs = Component.literal("");
        List<String> changelogList = Versioner.versionData != null ? Versioner.versionData.getCurrentChangelogs() : null;
        if (changelogList != null) {
            changelogs.append(Component.translatable("versioner.variables.changelogs")
                            .withStyle(style -> style.withColor(ChatFormatting.DARK_AQUA).withBold(true)))
                    .append(Component.literal("\n"));

            for (int i = 0; i < changelogList.size(); i++) {
                String entry = changelogList.get(i);
                if (entry != null) {
                    changelogs.append(Component.literal(VersionerConfig.GENERAL.changelogPrefix.get())
                                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)))
                            .append(Component.literal(entry)
                                    .withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
                }
                if (i < changelogList.size() - 1) {
                    changelogs.append(Component.literal("\n"));
                }
            }
            changelogs.append(Component.literal("\n"));
        }

        MutableComponent updateLink = Component.translatable("versioner.variables.update_link")
                .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                        .withBold(true)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, getUpdateLink()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("versioner.variables.update_link_tooltip")
                                        .withStyle(style.withColor(ChatFormatting.YELLOW)))));

        if (Versioner.versionData != null && Versioner.versionData.getSponsors() != null) {
            updateLink.append(Component.literal("§r    "))
                    .append(Component.translatable("versioner.variables.sponsors_list")
                            .withStyle(style -> style.withColor(ChatFormatting.RED)
                                    .withBold(true)
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/" + CommandHandler.SponsorsCommand.NAME + " " + CommandHandler.SponsorsCommand.ARG_LIST))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.translatable("versioner.variables.sponsors_list_tooltip")
                                                    .withStyle(style.withColor(ChatFormatting.YELLOW))))));
        }

        components.add(delimiter);
        components.add(Component.translatable("versioner.variables.update_chat_message_title")
                .withStyle(style -> style.withBold(true).withColor(ChatFormatting.AQUA)));
        components.add(currentVersion);
        components.add(latestVersion);
        components.add(changelogs);
        components.add(updateLink);
        components.add(delimiter);

        return components;
    }

    public static Component getTextComponentFromJSON(String msg) {
        try {
            HolderLookup.Provider registries = getRegistryAccess();
            Component component = Component.Serializer.fromJson(msg, registries);
            if (component != null) {
                return component;
            }
        } catch (Exception ignored) {
        }
        return Component.literal(msg);
    }

    private static HolderLookup.Provider getRegistryAccess() {
        try {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null) {
                return minecraft.level.registryAccess();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List<String> getMainMenuTexts() {
        List<String> list;
        if (Versioner.versionData != null && Versioner.versionData.getMainMenu() != null &&
                Versioner.versionData.getMainMenu().getText() != null) {
            list = new ArrayList<>(Versioner.versionData.getMainMenu().getText());
        } else {
            list = new ArrayList<>(VersionerConfig.MAIN_MENU.textLines.get());
        }

        for (int i = 0; i < list.size(); i++) {
            list.set(i, getFormattedString(list.get(i)));
        }
        return list;
    }

    public static List<String> getMainMenuTooltipTexts() {
        List<String> list;
        if (Versioner.versionData != null && Versioner.versionData.getMainMenu() != null &&
                Versioner.versionData.getMainMenu().getTooltipText() != null) {
            list = new ArrayList<>(Versioner.versionData.getMainMenu().getTooltipText());
        } else {
            list = new ArrayList<>(VersionerConfig.MAIN_MENU.tooltipText.get());
        }

        for (int i = 0; i < list.size(); i++) {
            list.set(i, getFormattedString(list.get(i)));
        }
        return list;
    }

    public static String getClickLink() {
        String link = "";
        if (Versioner.versionData != null && Versioner.versionData.getMainMenu() != null) {
            link = Versioner.versionData.getMainMenu().getClickLink();
        }
        if (link == null || link.isEmpty()) {
            link = VersionerConfig.MAIN_MENU.clickLink.get();
        }
        if (link.isEmpty()) {
            return getUpdateLink();
        }
        return link;
    }

    public static List<String> jsonStringArrayToList(JsonArray array) {
        if (array != null) {
            List<String> list = new ArrayList<>();
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    list.add(element.getAsString());
                }
            }
            return list;
        }
        return null;
    }

    public static boolean isValidMenuPosition(String position) {
        if (position == null) return false;

        String upper = position.toUpperCase();
        return upper.equals("TOP_LEFT") ||
                upper.equals("TOP_RIGHT") ||
                upper.equals("BOTTOM_LEFT") ||
                upper.equals("BOTTOM_RIGHT") ||
                upper.equals("TOP_CENTER") ||
                upper.equals("BOTTOM_CENTER") ||
                upper.equals("CENTER") ||
                upper.equals("CENTER_LEFT") ||
                upper.equals("CENTER_RIGHT");
    }
}
