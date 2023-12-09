package com.squagward.screenshots.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.squagward.screenshots.Screenshots;
import com.squagward.screenshots.config.ScreenshotsConfig;
import com.squagward.screenshots.hud.ScreenshotHud;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

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

    @Inject(method = "method_1661", at = @At("HEAD"), cancellable = true)
    private static void screenshots$shouldWriteToFile(NativeImage nativeImage, File file, Consumer<Text> consumer, CallbackInfo ci) {
        ScreenshotsConfig config = ScreenshotsConfig.CONFIG.instance();
        if (config.getEnabled() && !config.getSaveScreenshotFile()) {
            nativeImage.close();
            ci.cancel();
        }
    }
}
