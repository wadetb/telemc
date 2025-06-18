package com.wadeb.telemc;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleMCMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("telemc");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello TeleMC!");
	}
}