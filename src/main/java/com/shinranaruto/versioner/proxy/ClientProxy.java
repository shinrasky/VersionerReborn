package com.shinranaruto.versioner.proxy;

import com.shinranaruto.versioner.Versioner;
import net.minecraft.client.resources.language.I18n;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
        Versioner.LOGGER.info("Initializing Versioner client features");
    }

    public void init() {
        super.init();
    }

    @Override
    public String i18nSafe(String key, Object... objects) {
        if (!I18n.exists(key)) {
            return key;
        }
        return I18n.get(key, objects);
    }
}
