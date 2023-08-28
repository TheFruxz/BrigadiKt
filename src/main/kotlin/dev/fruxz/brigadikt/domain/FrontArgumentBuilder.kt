package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import dev.fruxz.brigadikt.activity.BrigadiktCommandContext
import dev.fruxz.brigadikt.annotation.BrigadiktDSL

data class FrontArgumentBuilder<S>(
    val depth: Int = 0,
    var run: ((BrigadiktCommandContext<S>) -> Unit)? = null,
    val requirements: MutableList<((S) -> Boolean)?> = mutableListOf(),
    val children: MutableSet<ArgumentBuilder<S, *>> = mutableSetOf(),
    val arguments: MutableList<ActiveCommandArgument<S, *>> = mutableListOf(),
) {

    @BrigadiktDSL
    fun executes(process: BrigadiktCommandContext<S>.() -> Unit) = apply {
        this.run = process
    }

    @BrigadiktDSL
    fun requires(override: Boolean = false, process: (S) -> Boolean) = apply {
        if (override) requirements.clear()
        requirements.add(process)
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "This method is not supported yet.")
    fun redirect() = apply { TODO("This method is not supported yet.") }

    @Deprecated(level = DeprecationLevel.ERROR, message = "This method is not supported yet.")
    fun fork() = apply { TODO() }

    @Deprecated(level = DeprecationLevel.ERROR, message = "This method is not supported yet.")
    fun forward() = apply { TODO() }

    fun construct(): ArgumentBuilder<S, *> {
        if (this.arguments.lastIndex < depth) throw IllegalStateException("No arguments provided on depth $depth!")

        val overflow = this.arguments.lastIndex - depth
        val base = arguments[depth].produce()

        base.requires { executor ->
            return@requires requirements.all { it?.invoke(executor) ?: false }
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
