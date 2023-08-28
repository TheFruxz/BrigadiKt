package dev.fruxz.brigadikt

import com.mojang.brigadier.builder.ArgumentBuilder
import dev.fruxz.brigadikt.domain.FrontArgumentBuilder

/**
 * Adds a path to the current front argument builder.
 *
 * This method allows you to specify a path within the front argument builder. The path is added as a child to the current front argument builder.
 *
 * @param builder A lambda function that provides a DSL-like syntax for constructing a front argument builder. This lambda function is responsible for configuring the child front argument builder.
 *
 * @param S The type of the source object.
 * @param O The type of the constructed object.
 *
 * @throws IllegalStateException if the provided builder fails to construct a valid object.
 */
fun <S> FrontArgumentBuilder<S>.path(builder: FrontArgumentBuilder<S>.() -> Unit) {
    this.children.add(FrontArgumentBuilder<S>().apply(builder).construct())
}

/**
 * Entry point for the command tree.
 */
fun <S> ArgumentBuilder<S, *>.path(builder: FrontArgumentBuilder<S>.() -> Unit) {
    this.then(FrontArgumentBuilder<S>().apply(builder).construct())
}