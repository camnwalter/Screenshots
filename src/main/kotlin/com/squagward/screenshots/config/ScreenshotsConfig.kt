package com.squagward.screenshots.config

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class ScreenshotsConfig {
    @SerialEntry
    var enabled = true

    @SerialEntry
    var cropImage = true

    @SerialEntry
    var pauseGameWhileCropping = true

    @SerialEntry
    var saveScreenshotFile = true

    @SerialEntry
    var copyToClipboard = true

    companion object {
        @JvmField
        val CONFIG: ConfigClassHandler<ScreenshotsConfig> = ConfigClassHandler.createBuilder(ScreenshotsConfig::class.java)
            .serializer {
                GsonConfigSerializerBuilder.create(it)
                    .setPath(FabricLoader.getInstance().configDir.resolve("screenshots.json"))
                    .build()
            }
            .build()

        fun createScreen(parent: Screen): Screen {
            return YetAnotherConfigLib.create(CONFIG) { defaults: ScreenshotsConfig, config: ScreenshotsConfig, builder: YetAnotherConfigLib.Builder ->
                val pauseGameWhileCroppingOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.translatable("screenshots.setting.pause_crop.title"))
                    .description(OptionDescription.of(Text.translatable("screenshots.setting.pause_crop.description")))
                    .binding(defaults.pauseGameWhileCropping, { config.pauseGameWhileCropping }) { config.pauseGameWhileCropping = it }
                    .controller(TickBoxControllerBuilder::create)
                    .build()

                val cropImageOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.translatable("screenshots.setting.crop.title"))
                    .description(OptionDescription.of(Text.translatable("screenshots.setting.crop.description")))
                    .binding(defaults.cropImage, { config.cropImage }) { config.cropImage = it }
                    .controller(TickBoxControllerBuilder::create)
                    .listener { _, value: Boolean ->
                        pauseGameWhileCroppingOption.setAvailable(value)
                    }
                    .build()

                val saveScreenshotOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.translatable("screenshots.setting.save_file.title"))
                    .description(OptionDescription.of(Text.translatable("screenshots.setting.save_file.description")))
                    .binding(defaults.saveScreenshotFile, { config.saveScreenshotFile }) { config.saveScreenshotFile = it }
                    .controller(TickBoxControllerBuilder::create)
                    .build()

                val copyToClipboardOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.translatable("screenshots.setting.copy.title"))
                    .description(OptionDescription.of(Text.translatable("screenshots.setting.copy.description")))
                    .binding(defaults.copyToClipboard, { config.copyToClipboard }) { config.copyToClipboard = it }
                    .controller(TickBoxControllerBuilder::create)
                    .build()

                val enabledOption: Option<Boolean> = Option.createBuilder<Boolean>()
                    .name(Text.translatable("screenshots.setting.enable.title"))
                    .description(OptionDescription.of(Text.translatable("screenshots.setting.enable.description")))
                    .binding(defaults.enabled, { config.enabled }) { config.enabled = it }
                    .controller(TickBoxControllerBuilder::create)
                    .listener { _, value: Boolean ->
                        cropImageOption.setAvailable(value)
                        pauseGameWhileCroppingOption.setAvailable(value)
                        saveScreenshotOption.setAvailable(value)
                        copyToClipboardOption.setAvailable(value)
                    }
                    .build()

                builder
                    .title(Text.translatable("screenshots.setting.title"))
                    .category(
                        ConfigCategory.createBuilder()
                            .name(Text.translatable("screenshots.setting.general"))
                            .option(enabledOption)
                            .option(cropImageOption)
                            .option(pauseGameWhileCroppingOption)
                            .option(saveScreenshotOption)
                            .option(copyToClipboardOption)
                            .build()
                    )
            }
                .generateScreen(parent)
        }
    }
}
