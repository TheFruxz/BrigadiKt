package dev.fruxz.brigadikt

import com.mojang.brigadier.builder.LiteralArgumentBuilder

fun <SENDER> command(label: String, builder: LiteralArgumentBuilder<SENDER>.() -> Unit = { }): LiteralArgumentBuilder<SENDER> =
    LiteralArgumentBuilder.literal<SENDER>(label).apply(builder)