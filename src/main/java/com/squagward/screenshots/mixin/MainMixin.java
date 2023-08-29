package com.squagward.screenshots.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Main.class)
public class MainMixin {
    static {
        if (!System.getProperty("os.name").startsWith("Mac")) {
            System.out.println("[Screenshots] Setting java.awt.headless to false");
            System.setProperty("java.awt.headless", "false");
        }
    }
}
