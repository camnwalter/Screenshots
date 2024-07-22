package com.squagward.screenshots

import com.mojang.brigadier.Command
import com.squagward.screenshots.compat.MacOSCompat
import com.squagward.screenshots.hud.ScreenshotHud
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.dsl.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
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
import java.io.File
import javax.imageio.ImageIO

@Serializable
data class Config(
    var enabled: Boolean,
    var cropImage: Boolean,
    var pauseGameWhileCropping: Boolean,
    var saveScreenshotFile: Boolean,
    var copyToClipboard: Boolean
) {
    companion object {
        val DEFAULT = Config(
            enabled = true,
            cropImage = true,
            pauseGameWhileCropping = true,
            saveScreenshotFile = true,
            copyToClipboard = true
        )
    }
}

object Screenshots : ClientModInitializer {
    private val LOGGER: Logger = LogManager.getLogger("Screenshots")
    private val json = Json { prettyPrint = true }
    private val configLocation: File = FabricLoader.getInstance().configDir.resolve("screenshots.json").toFile()

    @JvmStatic
    var displayScreenshotHud = false

    @JvmStatic
    var displayScreenshotScreen = false

    @JvmStatic
    lateinit var config: Config
        private set

    private fun load() {
        if (!configLocation.exists()) {
            save(Config.DEFAULT)
        }
        config = json.decodeFromString<Config>(configLocation.readText())
    }

    private fun save(config: Config) {
        configLocation.writeText(json.encodeToString(config))
    }

    fun createScreen(parent: Screen?): Screen = YetAnotherConfigLib("screenshots") {
        save { save(config) }

        val general by categories.registering {
            val enabled by rootOptions.registering {
                description(OptionDescription.of(Text.translatable("screenshots.setting.enable.description")))

                controller = tickBox()
                binding(config::enabled, Config.DEFAULT.enabled)

                listener { _, value: Boolean ->
                    val cropImage = rootOptions.futureRef<Boolean>("cropImage")
                    val pauseWhileCropping = rootOptions.futureRef<Boolean>("pauseWhileCropping")
                    val saveScreenshot = rootOptions.futureRef<Boolean>("saveScreenshot")
                    val copyToClipboard = rootOptions.futureRef<Boolean>("copyToClipboard")

                    cropImage.onReady { crop ->
                        pauseWhileCropping.onReady { it.setAvailable(crop.available()) }
                        crop.setAvailable(value)
                    }
                    saveScreenshot.onReady { it.setAvailable(value) }
                    copyToClipboard.onReady { it.setAvailable(value) }
                }
            }

            val cropImage by rootOptions.registering {
                description(OptionDescription.of(Text.translatable("screenshots.setting.crop.description")))

                controller = tickBox()
                binding(config::cropImage, Config.DEFAULT.cropImage)

                listener { opt, value: Boolean ->
                    val pauseWhileCropping = rootOptions.futureRef<Boolean>("pauseWhileCropping")
                    pauseWhileCropping.onReady { it.setAvailable(opt.available() && value) }
                }
            }

            val pauseWhileCropping by rootOptions.registering {
                description(OptionDescription.of(Text.translatable("screenshots.setting.pause_crop.description")))

                controller = tickBox()
                binding(config::pauseGameWhileCropping, Config.DEFAULT.pauseGameWhileCropping)
            }

            val saveScreenshot by rootOptions.registering {
                description(OptionDescription.of(Text.translatable("screenshots.setting.save_file.description")))

                controller = tickBox()
                binding(config::saveScreenshotFile, Config.DEFAULT.saveScreenshotFile)
            }

            val copyToClipboard by rootOptions.registering {
                description(OptionDescription.of(Text.translatable("screenshots.setting.copy.description")))

                controller = tickBox()
                binding(config::copyToClipboard, Config.DEFAULT.copyToClipboard)
            }
        }
    }.generateScreen(parent)

    override fun onInitializeClient() {
        LOGGER.info("Initialized Screenshots!")

        ScreenshotHud.init()
        load()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("screenshots").executes {
                    val client: MinecraftClient = MinecraftClient.getInstance()

                    client.send {
                        client.setScreen(createScreen(null))
                    }

                    Command.SINGLE_SUCCESS
                }
            )
        }
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
