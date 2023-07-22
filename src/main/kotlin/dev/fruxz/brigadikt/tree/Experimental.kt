package dev.fruxz.brigadikt.tree

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.ArgumentCommandNode
import dev.fruxz.ascend.extension.forceCast
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class ActiveCommandArgument<S, T>(
    val node: ArgumentCommandNode<S, T>,
    val nodeClass: KClass<T & Any>,
    val host: FrontArgumentBuilder<S, *>,
) {

    var currentContext: CommandContext<S>? = null

}

operator fun <S, T> ActiveCommandArgument<S, T>.getValue(thisRef: Any?, property: KProperty<*>): T =
    this.currentContext?.getArgument(node.name, nodeClass.java.forceCast<Class<T>>())
        ?: throw IllegalStateException("No command context available for ${node.name} at depth ${host.depth}!")



data class FrontArgumentBuilder<S, T>(
    val depth: Int = 0,
    var run: ((CommandContext<S>) -> Unit)? = null,
    val children: MutableSet<ArgumentBuilder<S, *>> = mutableSetOf(),
    val arguments: MutableList<ActiveCommandArgument<S, *>> = mutableListOf(),
) {

    fun executes(process: (CommandContext<S>) -> Unit) {
        this.run = process
    }

    fun construct(): ArgumentBuilder<S, *> {
        if (this.arguments.lastIndex < depth) throw IllegalStateException("No arguments provided on depth $depth!")

        val overflow = this.arguments.lastIndex - depth
        val base = arguments[depth].node.let { node ->
            argument<S, T>(node.name, node.type.forceCast<ArgumentType<T>>())
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
                arguments.forEach { arg ->
                    arg.currentContext = it
                }

                println("Preparing execute at level $depth with args ${base.arguments.joinToString { it.name }}")

                // after preparing the context, run the command
                run!!.invoke(it)

                // do NOT reset the argument context, because every run indeed automatically receives its own context
                return@executes Command.SINGLE_SUCCESS
            }
        }

        return base

    }

}

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
fun <S, O> FrontArgumentBuilder<S, *>.path(builder: FrontArgumentBuilder<S, O>.() -> Unit) {
    this.children.add(FrontArgumentBuilder<S, O>().apply(builder).construct())
}

/**
 * Entry point for the command tree.
 */
fun <S, T> ArgumentBuilder<S, *>.path(builder: FrontArgumentBuilder<S, T>.() -> Unit) {
    this.then(FrontArgumentBuilder<S, T>().apply(builder).construct())
}

fun <S> buildCommand(name: String, builder: ArgumentBuilder<S, *>.() -> Unit): LiteralArgumentBuilder<S> =
    LiteralArgumentBuilder.literal<S>(name).apply(builder)
