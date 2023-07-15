package dev.fruxz.brigadikt.tree

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.ArgumentCommandNode
import dev.fruxz.ascend.extension.forceCast

data class ActiveCommandArgument<S, T>(
    val node: ArgumentCommandNode<S, T>,
) {



}

//fun <S> string() = ActiveCommandArgument<S, String>(string(""))

data class FrontArgumentBuilder<S, T : ArgumentBuilder<S, T>>(
    val arguments: MutableList<ArgumentCommandNode<S, *>> = mutableListOf(),
    var run: ((CommandContext<S>) -> Unit)? = null,
    val children: MutableSet<ArgumentBuilder<S, *>> = mutableSetOf(),
) {

    fun subPath(builder: FrontArgumentBuilder<S, *>.() -> Unit) {
        this.children.add(FrontArgumentBuilder<S, T>().apply(builder).construct())
    }

    fun construct(): ArgumentBuilder<S, *> {

        val base = argument<S, T>(arguments.first().name, arguments.first().type.forceCast<ArgumentType<T>>())

        base.then(FrontArgumentBuilder<S, T>(arguments = arguments.drop(1).toMutableList(), run = run).construct())

        children.forEach { child ->
            base.then(child)
        }

        if (arguments.size <= 1 && run != null) {
            base.executes {
                run!!.invoke(it)
                return@executes Command.SINGLE_SUCCESS
            }
        }

        return base
    }

}

fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.subPath(builder: FrontArgumentBuilder<S, *>.() -> Unit) {
    this.then(FrontArgumentBuilder<S, T>().apply(builder).construct())
}

fun <S> buildCommand(name: String, builder: ArgumentBuilder<S, *>.() -> Unit): ArgumentBuilder<S, *> =
    LiteralArgumentBuilder.literal<S>(name).apply(builder)

fun test() {

    buildCommand<Int>("test") {
        this.subPath {

        }
    }

    LiteralArgumentBuilder.literal<Int>("test")
        .then(argument("test", integer()))

    LiteralArgumentBuilder.literal<Collection<Int>>("test")
        .subPath {
            this.subPath {

            }
        }

}