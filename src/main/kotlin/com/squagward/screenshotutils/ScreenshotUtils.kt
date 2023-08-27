package com.squagward.screenshotutils

import com.squagward.screenshotutils.hud.ScreenshotHud
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ScreenshotUtils : ClientModInitializer {
    var displayScreenshotHud = false
    val LOGGER: Logger = LogManager.getLogger("ScreenshotUtils")

    override fun onInitializeClient() {
        LOGGER.info("Initialized ScreenshotUtils!")

        ScreenshotHud.init()
    }
}
