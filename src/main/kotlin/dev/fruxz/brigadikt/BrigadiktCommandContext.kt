package dev.fruxz.brigadikt

import com.mojang.brigadier.context.CommandContext
import dev.fruxz.ascend.extension.tryOrNull
import dev.fruxz.brigadikt.domain.ActiveCommandArgument
import kotlin.reflect.KClass

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

    fun <T : Any> getArgument(name: String, type: KClass<T>) =
        context.getArgument(name, type.java)

    fun <T : Any> ActiveCommandArgument<S, T>.getArgument(): T =
        context.getArgument(this.node.name, this.nodeClass.java)

    fun <T : Any> ActiveCommandArgument<S, T>.getArgumentOrNull(): T? =
        tryOrNull { this.getArgument() }

    operator fun <T : Any> ActiveCommandArgument<S, T>.invoke(): T =
        getArgument()

}