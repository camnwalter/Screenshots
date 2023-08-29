package com.squagward.screenshots.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.screen.Screen

fun interface ScreenDragCallback {
    fun drag(screen: Screen, mx: Double, my: Double, dx: Double, dy: Double)
}

@JvmField
val ScreenDragEvent = make<ScreenDragCallback> { listeners ->
    ScreenDragCallback { screen, mx, my, dx, dy ->
        listeners.forEach {
            it.drag(screen, mx, my, dx, dy)
        }
    }
}

private inline fun <reified T> make(noinline reducer: (Array<T>) -> T): Event<T> {
    return EventFactory.createArrayBacked(T::class.java, reducer)
}
