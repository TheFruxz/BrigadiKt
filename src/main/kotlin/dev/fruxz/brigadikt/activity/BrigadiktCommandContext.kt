package dev.fruxz.brigadikt.activity

import com.mojang.brigadier.context.CommandContext
import dev.fruxz.ascend.extension.tryOrNull
import dev.fruxz.brigadikt.domain.ActiveCommandArgument
import kotlin.reflect.KClass

/**
 * This class is basically a wrapper for the [CommandContext] provided
 * in the [context] value property.
 * This context allows multiple extension functions to provide a more
 * convenient way to access the context's arguments.
 * @author Fruxz
 * @since 2023.3
 */
data class BrigadiktCommandContext<S>(
    val context: CommandContext<S>
) {

    val child by context::child
    val lastChild by context::lastChild
    val command by context::command
    val source by context::source

    val redirectModifier by context::redirectModifier
    val range by context::range
    val input by context::input
    val rootNode by context::rootNode
    val nodes by context::nodes

    val hasNodes get() = context.hasNodes()
    val isForked get() = context.isForked

    /**
     * Retrieves the value of the argument with the specified name from the context.
     *
     * @param name the name of the argument to retrieve
     * @param type the type of the argument to retrieve
     * @return the value of the argument
     */
    fun <T : Any> getArgument(name: String, type: KClass<T>) =
        context.getArgument(name, type.java)

    /**
     * Retrieves the input value of this [ActiveCommandArgument].
     * @return the input value of this argument
     */
    fun <T : Any> ActiveCommandArgument<S, T>.getArgument(): T =
        context.getArgument(this.node.name, this.nodeClass.java)

    /**
     * Retrieves the input value of this [ActiveCommandArgument] or null if the argument is not present.
     * @return the input value of this argument or null
     */
    fun <T : Any> ActiveCommandArgument<S, T>.getArgumentOrNull(): T? =
        tryOrNull { this.getArgument() }

    /**
     * This is a shorthand for [getArgument]. It is used like <argument-variable>()
     * @return the input value of this argument ([getArgument])
     */
    operator fun <T : Any> ActiveCommandArgument<S, T>.invoke(): T =
        getArgument()

}