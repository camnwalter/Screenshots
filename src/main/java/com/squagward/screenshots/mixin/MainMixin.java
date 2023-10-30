package com.squagward.screenshots.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Locale;

@Mixin(Main.class)
public class MainMixin {
    static {
        if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac")) {
            System.out.println("[Screenshots] Setting java.awt.headless to false");
            System.setProperty("java.awt.headless", "false");
        }
    }
}
