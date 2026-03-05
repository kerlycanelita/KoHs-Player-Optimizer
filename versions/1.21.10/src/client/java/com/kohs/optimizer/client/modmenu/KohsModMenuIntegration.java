package com.kohs.optimizer.client.modmenu;

import com.kohs.optimizer.client.config.OptimizerConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class KohsModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return OptimizerConfigScreen::create;
    }
}
