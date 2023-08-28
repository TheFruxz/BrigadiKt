package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.tree.ArgumentCommandNode
import kotlin.reflect.KClass

/**
 * A representation of a command argument, which can be
 * easily accessed from a command context.
 * @author Fruxz
 * @since 2023.3
 */
data class ActiveCommandArgument<S, T>(
    val node: ArgumentCommandNode<S, T>,
    val nodeClass: KClass<T & Any>,
)