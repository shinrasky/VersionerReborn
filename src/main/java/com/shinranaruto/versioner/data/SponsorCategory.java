package com.shinranaruto.versioner.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SponsorCategory {
    private final JsonObject jsonObj;
    private String name;
    private String displayName;
    private final List<Sponsor> sponsors = new ArrayList<>();

    public SponsorCategory(JsonObject jsonObj) {
        this.jsonObj = jsonObj;
        initialize();
    }

    private void initialize() {
        if (jsonObj.has("name")) {
            name = jsonObj.get("name").getAsString();
        }
        if (jsonObj.has("displayName")) {
            displayName = jsonObj.get("displayName").getAsString();
        }
        if (jsonObj.has("sponsors")) {
            JsonArray sponsorArray = jsonObj.get("sponsors").getAsJsonArray();
            for (JsonElement element : sponsorArray) {
                if (element.isJsonObject()) {
                    JsonObject sponsorObj = element.getAsJsonObject();
                    String sponsorName = null;
                    UUID sponsorUuid = null;

                    if (sponsorObj.has("name")) {
                        sponsorName = sponsorObj.get("name").getAsString();
                    }
                    if (sponsorObj.has("uuid")) {
                        try {
                            sponsorUuid = UUID.fromString(sponsorObj.get("uuid").getAsString());
                        } catch (IllegalArgumentException e) {
                            // UUID格式无效，跳过
                        }
                    }

                    if (sponsorName != null || sponsorUuid != null) {
                        sponsors.add(new Sponsor(sponsorName, sponsorUuid));
                    }
                }
            }
        }
    }

    public boolean containsPlayer(Player player) {
        for (Sponsor sponsor : sponsors) {
            if (sponsor.matchesPlayer(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBad() {
        return name == null || sponsors.isEmpty();
    }

    public List<Component> getFormattedText() {
        List<Component> components = new ArrayList<>();

        components.add(getFormattedName());

        for (int i = 0; i < sponsors.size(); i++) {
            Sponsor sponsor = sponsors.get(i);
            Component sponsorText = sponsor.getFormattedText(Style.EMPTY.withColor(ChatFormatting.WHITE));

            if (i < sponsors.size() - 1) {
                sponsorText = sponsorText.copy().append(Component.literal(", "));
            }

            components.add(sponsorText);
        }

        components.add(Component.literal(""));

        return components;
    }

    public Component getFormattedName() {
        String display = displayName != null ? displayName : name;
        if (display == null) display = "Unknown";

        return Component.literal(display).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withBold(true));
    }

    // Getters
    public JsonObject getJsonObj() { return jsonObj; }
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public List<Sponsor> getSponsors() { return sponsors; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (name != null) {
            builder.append("Category: ").append(name).append("\n");
        }
        if (displayName != null) {
            builder.append("Display: ").append(displayName).append("\n");
        }
        builder.append("Sponsors: ");
        for (int i = 0; i < sponsors.size(); i++) {
            builder.append(sponsors.get(i).toString());
            if (i < sponsors.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
