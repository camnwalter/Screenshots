package com.squagward.screenshots.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.screen.Screen

fun interface ScreenDragCallback {
    fun drag(screen: Screen, mx: Double, my: Double, dx: Double, dy: Double)

    companion object {
        @JvmField
        val EVENT: Event<ScreenDragCallback> = EventFactory.createArrayBacked(ScreenDragCallback::class.java) { listeners ->
            ScreenDragCallback { screen, mx, my, dx, dy ->
                listeners.forEach {
                    it.drag(screen, mx, my, dx, dy)
                }
            }
        }
    }
}
