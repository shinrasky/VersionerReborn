package com.shinranaruto.versioner.events;

import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Versioner.MODID)
public class EventHandler {

    private static final List<UUID> recognizedPlayers = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerLogin(EntityJoinLevelEvent event) {
        if (!VersionerConfig.NOTIFICATIONS.showLoginChatUpdateNotification.get()) {
            return;
        }

        if (event.getLevel().isClientSide() && event.getEntity() instanceof Player player) {
            if (player.getUUID().equals(getClientPlayerUUID())) {
                if (!Versioner.isUpdateMessageShown) {
                    if (Versioner.versionData != null && Versioner.versionData.isReady()) {
                        if (Versioner.versionData.isUpdateAvailable()) {
                            for (var msg : Util.getUpdateChatMessage()) {
                                player.sendSystemMessage(msg);
                            }
                        }
                        if (VersionerConfig.NOTIFICATIONS.showWelcomeMessage.get()) {
                            var welcomeMsg = Versioner.versionData.getWelcomeMessage();
                            if (welcomeMsg != null) {
                                player.sendSystemMessage(Util.getTextComponentFromJSON(welcomeMsg));
                            }
                        }
                    } else {
                        if (VersionerConfig.NOTIFICATIONS.showUpdateCheckFailedMessage.get()) {
                            player.sendSystemMessage(
                                    Component.translatable("versioner.variables.update_check_failed")
                                            .withStyle(style -> style.withColor(ChatFormatting.RED))
                            );
                        }
                    }
                    Versioner.isUpdateMessageShown = true;
                }
            }
            recognizedPlayers.add(player.getUUID());
        }
    }

    private static UUID getClientPlayerUUID() {
        try {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.player != null) {
                return minecraft.player.getUUID();
            }
        } catch (Exception e) {
        }
        return UUID.randomUUID();
    }
}
