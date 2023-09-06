package dev.fruxz.brigadikt.tree

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.fruxz.brigadikt.domain.FrontArgumentBuilder

/**
 * This function builds a platform independent command, which can be used
 * on Paper, Spigot, BungeeCord, Velocity, etc.
 * So the end result is a normal, independent, brigadier command/structure.
 * @author Fruxz
 * @sample 2023.3
 */
fun <S> buildUniversalCommand(name: String, builder: FrontArgumentBuilder<S>.() -> Unit): LiteralArgumentBuilder<S> =
    FrontArgumentBuilder<S>().apply(builder).constructFoundation(name)