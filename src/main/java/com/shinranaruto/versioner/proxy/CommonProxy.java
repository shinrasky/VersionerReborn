package com.shinranaruto.versioner.proxy;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.data.VersionData;
import com.shinranaruto.versioner.util.NetworkHandler;
import com.shinranaruto.versioner.util.Util;

import java.io.IOException;

public class CommonProxy {
    private boolean initialized = false;

    public void initializeVersionChecker() {
        if (initialized) {
            Versioner.LOGGER.debug("Versioner already initialized, skipping");
            return;
        }

        Versioner.LOGGER.info("Initializing Versioner version checker");

        if (!isConfigLoaded()) {
            Versioner.LOGGER.warn("Configuration not loaded yet, delaying version checker initialization");
            return;
        }

        initialized = true;

        if (isVersionCheckingEnabled()) {
            String versionDataURL = getVersionDataURL();
            if (versionDataURL != null && !versionDataURL.isEmpty()) {
                Versioner.LOGGER.info("Version checking enabled, URL: {}", versionDataURL);
                startVersionCheckThread(versionDataURL);
            } else {
                Versioner.LOGGER.warn("Version checking is enabled but no version data URL is configured");
            }
        } else {
            Versioner.LOGGER.info("Version checking is disabled");
        }

        Util.saveVariables();
    }

    private boolean isConfigLoaded() {
        try {
            VersionerConfig.GENERAL.enableVersionChecking.get();
            return true;
        } catch (IllegalStateException e) {
            Versioner.LOGGER.debug("Configuration not loaded yet: {}", e.getMessage());
            return false;
        }
    }

    private boolean isVersionCheckingEnabled() {
        try {
            return VersionerConfig.GENERAL.enableVersionChecking.get();
        } catch (IllegalStateException e) {
            Versioner.LOGGER.warn("Cannot access version checking config: {}", e.getMessage());
            return false;
        }
    }

    private String getVersionDataURL() {
        try {
            return VersionerConfig.GENERAL.versionDataURL.get();
        } catch (IllegalStateException e) {
            Versioner.LOGGER.warn("Cannot access version data URL config: {}", e.getMessage());
            return null;
        }
    }

    private void startVersionCheckThread(String versionDataURL) {
        Thread networkThread = new Thread(() -> {
            try {
                Versioner.LOGGER.info("Starting to fetch version data from {}", versionDataURL);
                String jsonString = NetworkHandler.readToString(versionDataURL);
                Versioner.LOGGER.info("Successfully fetched {} characters of JSON data", jsonString.length());

                var jsonObj = JsonParser.parseString(jsonString).getAsJsonObject();
                Versioner.versionData = new VersionData(jsonObj);
                Versioner.LOGGER.info("Successfully parsed version data");

                if (Versioner.versionData.isReady()) {
                    String currentVersion = getCurrentVersionName();
                    Versioner.LOGGER.info("Current version: {}, Latest version: {}",
                            currentVersion,
                            Versioner.versionData.getVersionName());

                    if (Versioner.versionData.isUpdateAvailable()) {
                        Versioner.LOGGER.info("Update available! Current: {}, Latest: {}",
                                currentVersion,
                                Versioner.versionData.getVersionName());
                    } else {
                        Versioner.LOGGER.info("Modpack is up to date");
                    }
                } else {
                    Versioner.LOGGER.warn("Version data parsed but not ready");
                }
            } catch (JsonSyntaxException e) {
                Versioner.LOGGER.error("Version data JSON syntax error: {}", e.getMessage(), e);
            } catch (IOException e) {
                Versioner.LOGGER.error("Failed to fetch version data: {}", e.getMessage(), e);
                Versioner.LOGGER.error("Please check your network connection and the version data URL");
            } catch (Exception e) {
                Versioner.LOGGER.error("Unexpected error while fetching version data: {}", e.getMessage(), e);
            }
        });
        networkThread.setName("Versioner Network Thread");
        networkThread.setDaemon(true);
        networkThread.start();
    }

    private String getCurrentVersionName() {
        try {
            return VersionerConfig.CURRENT_VERSION.versionName.get();
        } catch (IllegalStateException e) {
            Versioner.LOGGER.warn("Cannot access current version name config: {}", e.getMessage());
            return "Unknown";
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void preInit() {
    }

    public void init() {
        if (VersionerConfig.GENERAL.enableVersionChecking.get()) {
            Thread networkThread = new Thread(() -> {
                try {
                    String versionDataURL = VersionerConfig.GENERAL.versionDataURL.get();
                    Versioner.LOGGER.info("Starting to fetch version data from {}", versionDataURL);
                    String jsonString = NetworkHandler.readToString(versionDataURL);
                    var jsonObj = JsonParser.parseString(jsonString).getAsJsonObject();
                    Versioner.versionData = new VersionData(jsonObj);
                    Versioner.LOGGER.info("Successfully fetched version data: {}", Versioner.versionData);
                } catch (JsonSyntaxException e) {
                    Versioner.LOGGER.error("Version data JSON syntax error!", e);
                } catch (IOException e) {
                    Versioner.LOGGER.error("Failed to fetch version data. Check your network connection," +
                            " if you believe it's not your problem, report this to the modpack author.", e);
                } catch (Exception e) {
                    Versioner.LOGGER.error("Unexpected error while fetching version data", e);
                }
            });
            networkThread.setName("Versioner Network Thread");
            networkThread.start();
            Util.saveVariables();
        }
    }

    public String i18nSafe(String key, Object... objects) {
        return key;
    }
}
