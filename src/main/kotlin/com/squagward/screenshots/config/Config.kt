package com.squagward.screenshots.config

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.config.ConfigEntry
import dev.isxander.yacl3.config.GsonConfigInstance
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.nio.file.Path

class Config {
    @ConfigEntry
    var enabled = true

    @ConfigEntry
    var cropImage = true

    @ConfigEntry
    var saveScreenshotFile = true

    @ConfigEntry
    var copyToClipboard = true

    companion object {
        @JvmField
        val INSTANCE: GsonConfigInstance<Config> = GsonConfigInstance.createBuilder(Config::class.java)
            .setPath(Path.of("config", "screenshots.json"))
            .build()

        fun createScreen(parent: Screen): Screen {
            return YetAnotherConfigLib.create(INSTANCE) { defaults: Config, config: Config, builder: YetAnotherConfigLib.Builder ->
                val cropImageOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.literal("Crop Images"))
                    .description(
                        OptionDescription.of(Text.literal("With this setting enabled, a snipping-tool like window will appear. " +
                                "Drag the selected area you want to screenshot and only that part will be screenshotted."))
                    )
                    .binding(defaults.cropImage, { config.cropImage }) { config.cropImage = it }
                    .controller(TickBoxControllerBuilder::create)
                    .build()

                val saveScreenshotOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.literal("Save Screenshot File"))
                    .description(
                        OptionDescription.of(Text.literal("With this setting enabled, the screenshot file will be created like usual."))
                    )
                    .binding(defaults.saveScreenshotFile, { config.saveScreenshotFile }) { config.saveScreenshotFile = it }
                    .controller(TickBoxControllerBuilder::create)
                    .build()

                val copyToClipboardOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.literal("Copy Screenshot to Clipboard"))
                    .description(
                        OptionDescription.of(Text.literal("With this setting enabled, the screenshot will be copied to your clipboard."))
                    )
                    .binding(defaults.copyToClipboard, { config.copyToClipboard }) { config.copyToClipboard = it }
                    .controller(TickBoxControllerBuilder::create)
                    .build()

                val enabledOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.literal("Enable Mod"))
                    .description(
                        OptionDescription.of(Text.literal("Disabling this setting completely disables the mod, with all functionality " +
                                "returning to normal."))
                    )
                    .binding(defaults.enabled, { config.enabled }) { config.enabled = it }
                    .controller(TickBoxControllerBuilder::create)
                    .listener { _, value: Boolean ->
                        cropImageOption.setAvailable(value)
                        saveScreenshotOption.setAvailable(value)
                        copyToClipboardOption.setAvailable(value)
                    }
                    .build()

                builder
                    .title(Text.literal("Screenshots Config"))
                    .category(
                        ConfigCategory.createBuilder()
                            .name(Text.literal("General"))
                            .option(enabledOption)
                            .option(cropImageOption)
                            .option(saveScreenshotOption)
                            .option(copyToClipboardOption)
                            .build()
                    )
            }
                .generateScreen(parent)
        }
    }
}
