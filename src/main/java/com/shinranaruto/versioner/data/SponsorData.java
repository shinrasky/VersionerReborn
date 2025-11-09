package com.shinranaruto.versioner.data;

import com.google.gson.JsonArray;
import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SponsorData {
    private final JsonArray jsonArray;
    private final List<SponsorCategory> categoryList = new ArrayList<>();

    public SponsorData(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
        initialize();
    }

    private void initialize() {
        for (var entry : jsonArray) {
            if (entry.isJsonObject()) {
                SponsorCategory category = new SponsorCategory(entry.getAsJsonObject());
                if (!category.isBad()) {
                    categoryList.add(category);
                }
            }
        }
    }

    public List<Component> getFormattedText() {
        List<Component> components = new ArrayList<>();
        String delimiter = VersionerConfig.GENERAL.delimiter.get();

        components.add(Component.literal(delimiter).withStyle(style ->
                style.withColor(net.minecraft.ChatFormatting.YELLOW)));
        components.add(Component.translatable("versioner.variables.sponsors").withStyle(style ->
                style.withColor(net.minecraft.ChatFormatting.LIGHT_PURPLE).withBold(true)));

        for (var category : categoryList) {
            components.addAll(category.getFormattedText());
        }

        if (Versioner.versionData != null && Versioner.versionData.getSponsorMessage() != null) {
            components.add(Util.getTextComponentFromJSON(Versioner.versionData.getSponsorMessage()));
        }

        components.add(Component.literal(delimiter).withStyle(style ->
                style.withColor(net.minecraft.ChatFormatting.YELLOW)));

        return components;
    }

    public SponsorCategory checkPlayer(Player player) {
        for (var category : categoryList) {
            if (category.containsPlayer(player)) {
                return category;
            }
        }
        return null;
    }

    public boolean isPlayerInCategory(Player player, String categoryName) {
        for (var category : categoryList) {
            if (category.containsPlayer(player) && categoryName.equals(category.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSponsor(Player player) {
        return checkPlayer(player) != null;
    }

    public SponsorCategory getCategory(String categoryName) {
        for (var category : categoryList) {
            if (categoryName.equals(category.getName())) {
                return category;
            }
        }
        return null;
    }

    // Getters
    public JsonArray getJsonArray() { return jsonArray; }
    public List<SponsorCategory> getCategoryList() { return categoryList; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (var category : categoryList) {
            builder.append(category.toString());
        }
        return builder.toString();
    }
}