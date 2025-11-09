package com.shinranaruto.versioner.mainmenu;

import com.shinranaruto.versioner.Versioner;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "versioner")
public class MainMenuEventHandler {
    private static final MainMenuRenderer renderer = new MainMenuRenderer();

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        try {
            renderer.render(
                    event.getGuiGraphics(),
                    event.getScreen(),
                    (int) event.getMouseX(),
                    (int) event.getMouseY(),
                    event.getPartialTick()
            );
        } catch (Exception e) {
            Versioner.LOGGER.debug("Error rendering version info on main menu", e);
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        try {
            if (renderer.mouseClicked(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
            }
        } catch (Exception e) {
            Versioner.LOGGER.debug("Error handling click on version info", e);
        }
    }
}
