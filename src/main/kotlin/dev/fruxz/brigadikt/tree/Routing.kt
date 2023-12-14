package dev.fruxz.brigadikt.tree

import com.mojang.brigadier.builder.ArgumentBuilder
import dev.fruxz.brigadikt.annotation.BrigadiktDSL
import dev.fruxz.brigadikt.domain.FrontArgumentBuilder

/**
 * Adds a path to the current command construction
 * @param S The type of the command sender
 * @author Fruxz
 * @sample 2023.3
 */
@BrigadiktDSL
fun <S> FrontArgumentBuilder<S>.route(builder: FrontArgumentBuilder<S>.() -> Unit) {
    this.children.add(FrontArgumentBuilder<S>().apply(builder).construct())
}

/**
 * Adds a path with a predefined literal to the current command construction
 * @param S The type of the command sender
 * @param literal the routes literal
 * @author Fruxz
 * @since 2023.3
 */
@BrigadiktDSL
fun <S> FrontArgumentBuilder<S>.route(literal: String, builder: FrontArgumentBuilder<S>.() -> Unit) {
    this.children.add(FrontArgumentBuilder<S>().apply(builder).constructOnLiteral(literal))
}

/**
 * Adds a path to the current command construction
 * @param S The type of the command sender
 * @author Fruxz
 * @since 2023.3
 */
@BrigadiktDSL
fun <S> ArgumentBuilder<S, *>.route(builder: FrontArgumentBuilder<S>.() -> Unit) {
    this.then(FrontArgumentBuilder<S>().apply(builder).construct())
}

/**
 * Adds a path with a predefined literal to the current command construction
 * @param S The type of the command sender
 * @param literal the routes literal
 * @author Fruxz
 * @since 2023.3
 */
@BrigadiktDSL
fun <S> ArgumentBuilder<S, *>.route(literal: String, builder: FrontArgumentBuilder<S>.() -> Unit) {
    this.then(FrontArgumentBuilder<S>().apply(builder).constructOnLiteral(literal))
}