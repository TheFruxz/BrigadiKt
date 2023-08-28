package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import dev.fruxz.ascend.extension.forceCast
import dev.fruxz.brigadikt.BrigadiktCommandContext

data class FrontArgumentBuilder<S, T>(
    val depth: Int = 0,
    var run: ((BrigadiktCommandContext<S>) -> Unit)? = null,
    val children: MutableSet<ArgumentBuilder<S, *>> = mutableSetOf(),
    val arguments: MutableList<ActiveCommandArgument<S, *>> = mutableListOf(),
) {

    fun executes(process: BrigadiktCommandContext<S>.() -> Unit) {
        this.run = process
    }

    fun construct(): ArgumentBuilder<S, *> {
        if (this.arguments.lastIndex < depth) throw IllegalStateException("No arguments provided on depth $depth!")

        val overflow = this.arguments.lastIndex - depth
        val base = arguments[depth].node.let { node ->
            RequiredArgumentBuilder.argument<S, T>(node.name, node.type.forceCast<ArgumentType<T>>())
        }

        if (overflow > 0) {
            base.then(
                this
                    .copy(depth = depth + 1) // increase the depth
                    .construct()
            )
        }

        children.forEach { child ->
            base.then(child)
        }

        if (overflow == 0 && run != null) {
            base.executes {

                println("Preparing execute at level $depth with args ${base.arguments.joinToString { it.name }}")

                // after preparing the context, run the command
                run!!.invoke(BrigadiktCommandContext(it))

                // do NOT reset the argument context, because every run indeed automatically receives its own context
                return@executes Command.SINGLE_SUCCESS
            }
        }

        return base

    }

}
