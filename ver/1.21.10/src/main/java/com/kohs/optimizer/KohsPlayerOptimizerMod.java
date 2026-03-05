package com.kohs.optimizer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KohsPlayerOptimizerMod implements ModInitializer {
    public static final String MOD_ID = "kohs_player_optimizer";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing KoHs Player Optimizer core");
    }
}
