package com.squagward.screenshotutils

import net.fabricmc.api.ClientModInitializer

object ScreenshotUtils : ClientModInitializer {
    var displayScreenshotHud = false

    override fun onInitializeClient() {
        println("initialized ScreenshotUtils!")
    }
}
