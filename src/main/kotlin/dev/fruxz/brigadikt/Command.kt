package dev.fruxz.brigadikt

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder

/**
 * This function builds a platform independent command, which can be used
 * on Paper, Spigot, BungeeCord, Velocity, etc.
 * @author Fruxz
 * @sample 2023.3
 */
fun <S> buildUniversalCommand(name: String, builder: ArgumentBuilder<S, *>.() -> Unit): LiteralArgumentBuilder<S> =
    LiteralArgumentBuilder.literal<S>(name).apply(builder)