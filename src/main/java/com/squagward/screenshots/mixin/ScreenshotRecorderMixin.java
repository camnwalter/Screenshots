package com.squagward.screenshots.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.squagward.screenshots.Screenshots;
import com.squagward.screenshots.config.ScreenshotsConfig;
import com.squagward.screenshots.hud.ScreenshotHud;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotRecorderMixin {
    @ModifyVariable(method = "saveScreenshotInner", at = @At("STORE"))
    private static NativeImage screenshots$cropImage(NativeImage original) {
        ScreenshotsConfig config = ScreenshotsConfig.CONFIG.instance();
        if (!config.getEnabled()) {
            return original;
        }

        NativeImage image = original;
        if (config.getCropImage()) {
            image = ScreenshotHud.INSTANCE.cropImage(original);
            original.close();
        }

        if (config.getCopyToClipboard()) {
            Screenshots.INSTANCE.copyToClipboard(image);
        }

        return image;
    }

    @WrapWithCondition(method = "method_1661", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;writeTo(Ljava/io/File;)V"))
    private static boolean screenshots$shouldWriteToFile(NativeImage image, File file) {
        ScreenshotsConfig config = ScreenshotsConfig.CONFIG.instance();
        return !config.getEnabled() || (config.getEnabled() && config.getSaveScreenshotFile());
    }
}
