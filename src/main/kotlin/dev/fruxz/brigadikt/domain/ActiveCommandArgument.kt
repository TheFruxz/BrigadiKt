package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.ArgumentCommandNode
import dev.fruxz.ascend.extension.forceCast
import dev.fruxz.ascend.extension.forceCastOrNull
import dev.fruxz.ascend.extension.tryOrNull
import kotlinx.coroutines.currentCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A representation of a command argument, which can be
 * easily accessed from a command context.
 * @author Fruxz
 * @since 2023.3
 */
data class ActiveCommandArgument<S, T>(
    val node: ArgumentCommandNode<S, T>,
    val nodeClass: KClass<T & Any>,
) {

    /**
     * Gets the content provided for this argument inside this command context
     */
    context(CommandContext<S>)
    fun getArgument(): T =
        getArgument(this@ActiveCommandArgument.node.name, this@ActiveCommandArgument.nodeClass.java)

    /**
     * Gets the content provided for this argument inside this command context
     * using the [getArgument] function, or null, if it fails (for example, if the argument is not present)
     */
    context(CommandContext<S>)
    fun getArgumentOrNull(): T? =
        tryOrNull { this.getArgument() }

    /**
     * Gets the content provided for this argument inside this command context
     * using the [getArgument] function.
     */
    context(CommandContext<S>)
    operator fun invoke(): T =
        getArgument()

}
