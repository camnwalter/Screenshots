package com.squagward.screenshots

import com.squagward.screenshots.compat.MacOSCompat
import com.squagward.screenshots.config.ScreenshotsConfig
import com.squagward.screenshots.hud.ScreenshotHud
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

object Screenshots : ClientModInitializer {
    var displayScreenshotHud = false
    var displayScreenshotScreen = false
    private val LOGGER: Logger = LogManager.getLogger("Screenshots")

    override fun onInitializeClient() {
        LOGGER.info("Initialized Screenshots!")

        ScreenshotHud.init()
        ScreenshotsConfig.CONFIG.load()
    }

    fun copyToClipboard(image: NativeImage) {
        if (MinecraftClient.IS_SYSTEM_MAC) {
            MacOSCompat.doCopyMacOS(image)
            return
        }

        try {
            val bufferedImage: BufferedImage = ImageIO.read(ByteArrayInputStream(image.bytes))

            Toolkit.getDefaultToolkit()
                .systemClipboard
                .setContents(TransferableImage(rgbaToRgb(bufferedImage)), null)
        } catch (e: Exception) {
            MinecraftClient.getInstance().inGameHud.chatHud.addMessage(
                Text.translatable("screenshots.error.copy").formatted(Formatting.RED)
            )
        }
    }

    private fun rgbaToRgb(image: BufferedImage): BufferedImage {
        val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        newImage.createGraphics().apply {
            drawImage(image, 0, 0, image.width, image.height, null)
            dispose()
        }

        return newImage
    }
}

private class TransferableImage(private val image: Image) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(DataFlavor.imageFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
        return DataFlavor.imageFlavor == flavor
    }

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (isDataFlavorSupported(flavor)) {
            return image
        }

        throw UnsupportedFlavorException(flavor)
    }
}
