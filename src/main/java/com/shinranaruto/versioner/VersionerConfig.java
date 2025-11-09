package com.shinranaruto.versioner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.shinranaruto.versioner.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class VersionerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SPEC;
    public static final General GENERAL;
    public static final Notifications NOTIFICATIONS;
    public static final CurrentVersion CURRENT_VERSION;
    public static final MainMenu MAIN_MENU;

    static {
        GENERAL = new General(BUILDER);
        NOTIFICATIONS = new Notifications(BUILDER);
        CURRENT_VERSION = new CurrentVersion(BUILDER);
        MAIN_MENU = new MainMenu(BUILDER);
        SPEC = BUILDER.build();
    }

    public static class General {
        public final ModConfigSpec.BooleanValue enableVersionChecking;
        public final ModConfigSpec.ConfigValue<String> changelogPrefix;
        public final ModConfigSpec.IntValue versionCheckerReadTimeout;
        public final ModConfigSpec.IntValue versionCheckerConnectTimeout;
        public final ModConfigSpec.ConfigValue<String> modpackName;
        public final ModConfigSpec.ConfigValue<String> versionDataURL;
        public final ModConfigSpec.ConfigValue<String> updateURL;
        public final ModConfigSpec.ConfigValue<String> delimiter;

        public General(ModConfigSpec.Builder builder) {
            builder.push("general");

            enableVersionChecking = builder
                    .comment("If this is set to false, the mod will not try to fetch the version data at all.")
                    .define("enableVersionChecking", true);

            changelogPrefix = builder
                    .comment("Extra text added before each line of changelog")
                    .define("changelogPrefix", " - ");

            versionCheckerReadTimeout = builder
                    .comment("How much time to read before the connection closes with a timeout error.")
                    .defineInRange("versionCheckerReadTimeout", 5000, 1000, 30000);

            versionCheckerConnectTimeout = builder
                    .comment("How much time to connect to the URL before the connection closes with a timeout error.")
                    .defineInRange("versionCheckerConnectTimeout", 5000, 1000, 30000);

            modpackName = builder
                    .comment("The name of your modpack")
                    .define("modpackName", "");

            versionDataURL = builder
                    .comment("Where the version data JSON will be fetched from, KEEP http:// or https://")
                    .define("versionDataURL", "");

            updateURL = builder
                    .comment("The url that is opened when the user clicks on update button, KEEP http:// or https://")
                    .define("updateURL", "");

            delimiter = builder
                    .comment("The border of large messages, makes them look pretty")
                    .define("delimiter", "========================================");

            builder.pop();
        }
    }

    public static class Notifications {
        public final ModConfigSpec.BooleanValue showLoginChatUpdateNotification;
        public final ModConfigSpec.BooleanValue showWelcomeMessage;
        public final ModConfigSpec.BooleanValue showUpdateCheckFailedMessage;

        public Notifications(ModConfigSpec.Builder builder) {
            builder.push("notifications");

            showLoginChatUpdateNotification = builder
                    .comment("Display a chat message when logging into a world for the first time if an update is available?")
                    .define("showLoginChatUpdateNotification", true);

            showWelcomeMessage = builder
                    .comment("If 'welcomeMessage' key is defined in the JSON, display the message in chat when logging into a world")
                    .define("showWelcomeMessage", true);

            showUpdateCheckFailedMessage = builder
                    .comment("If true, displays an error message in chat if this mod is unable to fetch all update data.")
                    .define("showUpdateCheckFailedMessage", true);

            builder.pop();
        }
    }

    public static class CurrentVersion {
        public final ModConfigSpec.ConfigValue<String> versionName;
        public final ModConfigSpec.IntValue versionCode;
        public final ModConfigSpec.ConfigValue<String> versionFormat;
        public final ModConfigSpec.ConfigValue<List<String>> variables;

        public CurrentVersion(ModConfigSpec.Builder builder) {
            builder.push("currentVersion");

            versionName = builder
                    .comment("Version name of the current version")
                    .define("versionName", "1.0.0");

            versionCode = builder
                    .comment("Version code of the current version, must not be negative.")
                    .defineInRange("versionCode", 0, 0, Integer.MAX_VALUE);

            versionFormat = builder
                    .comment("How to output formatted version name. Only used for displaying current version.")
                    .define("versionFormat", "%currentVersionName%");

            variables = builder
                    .comment(
                            "Variables to use when formatting current version. One entry per line, the format is key=value",
                            "Values must follow JSON element format, for example strings must be double quoted"
                    )
                    .define("variables", new ArrayList<>());

            builder.pop();
        }
    }

    public static class MainMenu {
        public final ModConfigSpec.BooleanValue enableMainMenu;
        public final ModConfigSpec.ConfigValue<String> menuTextPosition;
        public final ModConfigSpec.IntValue marginHorizontal;
        public final ModConfigSpec.IntValue marginVertical;
        public final ModConfigSpec.IntValue textColor;
        public final ModConfigSpec.ConfigValue<List<String>> textLines;
        public final ModConfigSpec.ConfigValue<List<String>> tooltipText;
        public final ModConfigSpec.ConfigValue<String> clickLink;

        public MainMenu(ModConfigSpec.Builder builder) {
            builder.push("mainMenu");

            enableMainMenu = builder
                    .comment("Whether to enable main menu rendering added by this mod at all or not.")
                    .define("enableMainMenu", true);

            menuTextPosition = builder
                    .comment("Where to place the main menu text. Must be one of: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_CENTER, BOTTOM_CENTER, CENTER_LEFT, CENTER_RIGHT, CENTER")
                    .define("menuTextPosition", "BOTTOM_LEFT");

            marginHorizontal = builder
                    .comment("How much space between main menu text and the border of the screen. (Horizontal)")
                    .defineInRange("marginHorizontal", 2, 0, 100);

            marginVertical = builder
                    .comment("How much space between main menu text and the border of the screen. (Vertical)")
                    .defineInRange("marginVertical", 2, 0, 100);

            textColor = builder
                    .comment("Default color of the main menu text. Must be converted into a decimal integer. (0xffffff -> 16777215)")
                    .defineInRange("textColor", 0xFFFFFF, 0, 0xFFFFFF);

            textLines = builder
                    .comment("Text to display on the main menu. You can use variables like %versionName% in the string.")
                    .define("textLines", Arrays.asList(
                            "§eVersion§f: §9%currentVersionName%",
                            "%isUpdateAvailable%"
                    ));

            tooltipText = builder
                    .comment("Text to display when hovering mouse over the text, as tooltips.")
                    .define("tooltipText", Arrays.asList(
                            "§eCurrent Version§f: §6%currentVersionName% (%currentVersionCode%)",
                            "§2Latest Version§f: §2%versionName% (%versionCode%)",
                            "§9§nClick for update link!"
                    ));

            clickLink = builder
                    .comment("When the user click on the text, they will be sent to this link.")
                    .define("clickLink", "");

            builder.pop();
        }
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener((ModConfigEvent.Loading event) -> onConfigLoad());
        eventBus.addListener((ModConfigEvent.Reloading event) -> onConfigReload());
    }

    private static void onConfigLoad() {
        String menuPosition = MAIN_MENU.menuTextPosition.get();
        if (!Util.isValidMenuPosition(menuPosition)) {
            Versioner.LOGGER.warn("Invalid menu position '{}', using default 'BOTTOM_LEFT'", menuPosition);
        }
    }

    private static void onConfigReload() {
    }
}
