package com.squagward.screenshots.compat

import ca.weblite.objc.Client
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import org.apache.logging.log4j.LogManager

// slightly modified version of https://github.com/comp500/ScreenshotToClipboard/blob/7480e7955beb4657b62432205cefabfd1950aedc/common/src/main/java/link/infra/screenshotclipboard/common/MacOSCompat.java
object MacOSCompat {
    private val LOGGER = LogManager.getLogger("Screenshots-MacOSCompat")

    // macOS requires some ugly hacks to get it to work, because it doesn't allow GLFW and AWT to load at the same time
    fun doCopyMacOS(image: NativeImage) {
        if (!MinecraftClient.IS_SYSTEM_MAC) {
            return
        }

        // thank you DJtheRedstoner :)
        val client = Client.getInstance()
        val data = client.sendProxy("NSData", "dataWithBytes:length:", image.bytes, image.bytes.size) // this might not work
        val pasteboard = client.sendProxy("NSPasteboard", "generalPasteboard")
        pasteboard.send("clearContents")
        val wasSuccessful = pasteboard.sendBoolean("setData:forType:", data, "public.png")
        if (!wasSuccessful) {
            LOGGER.error("Failed to write image to pasteboard!")
        }
    }
}
