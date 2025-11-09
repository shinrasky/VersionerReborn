package com.shinranaruto.versioner.data;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

public class Sponsor {
    private final String name;
    private final UUID uuid;

    public Sponsor(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public Sponsor(String name) {
        this(name, null);
    }

    public Sponsor(UUID uuid) {
        this(null, uuid);
    }

    public boolean matchesPlayer(Player player) {
        if (uuid != null && player.getUUID().equals(uuid)) {
            return true;
        }
        if (uuid == null && name != null && player.getName().getString().equals(name)) {
            return true;
        }
        return false;
    }

    public Component getFormattedText(Style style) {
        String displayName = name != null ? name : (uuid != null ? uuid.toString() : "null");
        return Component.literal(displayName).withStyle(style);
    }

    public Component getFormattedText() {
        return getFormattedText(Style.EMPTY);
    }

    // Getters
    public String getName() { return name; }
    public UUID getUuid() { return uuid; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sponsor sponsor = (Sponsor) obj;
        return Objects.equals(name, sponsor.name) && Objects.equals(uuid, sponsor.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid);
    }

    @Override
    public String toString() {
        return name != null ? name : (uuid != null ? uuid.toString() : "null");
    }
}
