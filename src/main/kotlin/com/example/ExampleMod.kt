package com.example

import net.fabricmc.api.ClientModInitializer

object ExampleMod : ClientModInitializer {

    override fun onInitializeClient() {
        println("initialized ExampleMod!")
    }
}
