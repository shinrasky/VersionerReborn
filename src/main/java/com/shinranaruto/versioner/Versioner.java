package com.shinranaruto.versioner;

import com.shinranaruto.versioner.commands.CommandHandler;
import com.shinranaruto.versioner.data.VersionData;
import com.shinranaruto.versioner.proxy.ClientProxy;
import com.shinranaruto.versioner.proxy.CommonProxy;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Versioner.MODID)
public class Versioner {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "versioner";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static VersionData versionData = null;
    public static boolean isUpdateMessageShown = false;

    public static final CommonProxy PROXY = FMLEnvironment.dist.isClient() ? new ClientProxy() : new CommonProxy();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Versioner(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, VersionerConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);

        NeoForge.EVENT_BUS.register(this);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            LOGGER.info("Versioner configuration loaded");
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(MODID)) {
            LOGGER.info("Versioner configuration reloaded");
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Performing common setup for {}", MODID);
        event.enqueueWork(() -> {
            LOGGER.info("Starting Versioner initialization...");
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    LOGGER.info("Initializing version checker after config load...");
                    PROXY.initializeVersionChecker();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Version checker initialization interrupted", e);
                }
            }).start();
            LOGGER.info("Versioner initialization scheduled");
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        try {
            event.getDispatcher().register(CommandHandler.SponsorsCommand.build());
            LOGGER.info("Successfully registered /sponsors command");
        } catch (Exception e) {
            LOGGER.error("Failed to register commands", e);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (!PROXY.isInitialized()) {
            LOGGER.warn("Versioner was not initialized properly, initializing now...");

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    PROXY.initializeVersionChecker();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("Version checker initialization interrupted", e);
                }
            }).start();
        }
    }

    public static VersionData getVersionData() {
        return versionData;
    }

    public static void setVersionData(VersionData data) {
        versionData = data;
    }

    public static boolean isUpdateAvailable() {
        return versionData != null && versionData.isUpdateAvailable();
    }
}
