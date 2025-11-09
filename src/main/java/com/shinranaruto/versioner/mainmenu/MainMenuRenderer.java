package com.shinranaruto.versioner.mainmenu;

import com.shinranaruto.versioner.Versioner;
import com.shinranaruto.versioner.VersionerConfig;
import com.shinranaruto.versioner.util.Coords;
import com.shinranaruto.versioner.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class MainMenuRenderer {

    private static final int STRING_HEIGHT = 10;
    private static long lastClickTime = 0;

    private final Minecraft minecraft;
    private Font font = getFont();

    public MainMenuRenderer() {
        this.minecraft = Minecraft.getInstance();
    }

    public void render(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY, float partialTicks) {
        if (!shouldRender(screen)) {
            return;
        }

        if (font == null) {
            font = getFont();
            return;
        }

        List<String> texts = Util.getMainMenuTexts();
        if (texts.isEmpty()) {
            return;
        }

        Coords position = calculatePosition(screen, texts);
        int color = VersionerConfig.MAIN_MENU.textColor.get();

        for (int i = 0; i < texts.size(); i++) {
            String line = texts.get(i);
            int yPos = position.y + (i * STRING_HEIGHT);
            guiGraphics.drawString(font, line, position.x, yPos, color, false);
        }

        renderTooltip(guiGraphics, screen, mouseX, mouseY, position, texts);
    }

    public boolean mouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        if (!shouldRender(screen) || button != 0) {
            return false;
        }

        if (font == null) {
            font = getFont();
            return false;
        }

        List<String> texts = Util.getMainMenuTexts();
        if (texts.isEmpty()) {
            return false;
        }

        Coords position = calculatePosition(screen, texts);
        int textWidth = getLongestLineWidth(texts);
        int textHeight = texts.size() * STRING_HEIGHT;

        if (isMouseOver((int) mouseX, (int) mouseY, position, textWidth, textHeight)) {
            handleClick();
            return true;
        }

        return false;
    }

    private Font getFont() {
        try {
            if (minecraft != null) {
                return minecraft.font;
            }
            return null;
        } catch (Exception e) {
            Versioner.LOGGER.debug("Failed to get font", e);
            return null;
        }
    }

    private boolean shouldRender(Screen screen) {
        if (!VersionerConfig.MAIN_MENU.enableMainMenu.get()) {
            return false;
        }

        return isMainMenu(screen);
    }

    private boolean isMainMenu(Screen screen) {
        return screen instanceof TitleScreen;
    }

    private Coords calculatePosition(Screen screen, List<String> texts) {
        int screenWidth = screen.width;
        int screenHeight = screen.height;
        int textWidth = getLongestLineWidth(texts);
        int textHeight = texts.size() * STRING_HEIGHT;

        String position = VersionerConfig.MAIN_MENU.menuTextPosition.get();
        int marginH = VersionerConfig.MAIN_MENU.marginHorizontal.get();
        int marginV = VersionerConfig.MAIN_MENU.marginVertical.get();

        return switch (position.toUpperCase()) {
            case "TOP_LEFT" -> new Coords(marginH, marginV);
            case "TOP_RIGHT" -> new Coords(screenWidth - textWidth - marginH, marginV);
            case "BOTTOM_LEFT" -> new Coords(marginH, screenHeight - textHeight - marginV);
            case "BOTTOM_RIGHT" -> new Coords(screenWidth - textWidth - marginH, screenHeight - textHeight - marginV);
            case "TOP_CENTER" -> new Coords((screenWidth - textWidth) / 2, marginV);
            case "BOTTOM_CENTER" -> new Coords((screenWidth - textWidth) / 2, screenHeight - textHeight - marginV);
            case "CENTER" -> new Coords((screenWidth - textWidth) / 2, (screenHeight - textHeight) / 2);
            case "CENTER_LEFT" -> new Coords(marginH, (screenHeight - textHeight) / 2);
            case "CENTER_RIGHT" -> new Coords(screenWidth - textWidth - marginH, (screenHeight - textHeight) / 2);
            default -> new Coords(marginH, marginV);
        };
    }

    private void renderTooltip(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY, Coords position, List<String> texts) {
        int textWidth = getLongestLineWidth(texts);
        int textHeight = texts.size() * STRING_HEIGHT;

        if (isMouseOver(mouseX, mouseY, position, textWidth, textHeight)) {
            List<String> tooltips = Util.getMainMenuTooltipTexts();
            if (!tooltips.isEmpty()) {
                List<FormattedCharSequence> tooltipComponents = tooltips.stream()
                        .map(Component::literal)
                        .map(MutableComponent::getVisualOrderText)
                        .collect(Collectors.toList());
                guiGraphics.renderTooltip(font, tooltipComponents, mouseX, mouseY);
            }
        }
    }

    private int getLongestLineWidth(List<String> texts) {
        int maxWidth = 0;
        for (String line : texts) {
            String cleanLine = line.replaceAll("§.", "");
            int width = font.width(cleanLine);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    private boolean isMouseOver(int mouseX, int mouseY, Coords position, int width, int height) {
        return mouseX >= position.x &&
                mouseX <= position.x + width &&
                mouseY >= position.y &&
                mouseY <= position.y + height;
    }

    private void handleClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 500) {
            return;
        }
        lastClickTime = currentTime;

        String clickLink = Util.getClickLink();
        if (clickLink.equals("null") || clickLink.isEmpty()) {
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(clickLink));
        } catch (Exception e) {
            Versioner.LOGGER.error("Failed to open link: {}", clickLink, e);
        }
    }
}
