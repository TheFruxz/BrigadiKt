package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import dev.fruxz.ascend.tool.smart.generate.producible.Producible
import kotlin.reflect.KClass

/**
 * A representation of a required command argument, which
 * can be easily accessed from a command context.
 * @author Fruxz
 * @since 2023.3
 */
data class ActiveCommandArgument<S, T>(
    val node: ArgumentCommandNode<S, T>,
    val nodeClass: KClass<T & Any>,
) : Producible<RequiredArgumentBuilder<S, T>> {

    override fun produce(): RequiredArgumentBuilder<S, T> =
        RequiredArgumentBuilder.argument(node.name, node.type)

}