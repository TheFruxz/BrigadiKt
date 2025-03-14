@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

abstract class CommandContext(
    val raw: BrigadierCommandContext<CommandSourceStack>,
    val cachedArguments: Map<String, Any>
) {

    val sender = raw.source.sender
    val isPlayer = sender is Player

    operator fun <T : Any, R : Any> get(argument: Argument<T, R>) = argument.resolve(this) // TODO cache missing

    operator fun <T : Any, R : Any> Argument<T, R>.invoke(): R =
        this@CommandContext[this]

    // state modification
    abstract fun state(state: Int, process: () -> Unit = { })
    fun fail(process: () -> Unit = { }) = state(0, process)
    fun success(process: () -> Unit = { }) = state(Command.SINGLE_SUCCESS, process)

    fun reply(message: String) {
        sender.sendMessage(message)
    }

    fun reply(component: Component) {
        sender.sendMessage(component)
    }

    fun CommandSender.hasSubPermission(sub: String): Boolean {
        return hasPermission("brigadikt.$sub") // TODO proper permission generation
    }

}

interface Branch {
    val arguments: List<ArgumentBuilder<out Any, out Any>>
    val requirements: List<CommandContext.() -> Boolean>
    val children: List<Branch>
    val execution: (CommandContext.() -> Unit)?

    fun toMutable(): MutableBranch {
        return MutableBranch(arguments.toMutableList(), requirements.toMutableList(), children.toMutableList(), execution)
    }

    companion object {

        operator fun invoke(
            arguments: List<ArgumentBuilder<out Any, out Any>> = emptyList(),
            requirements: List<CommandContext.() -> Boolean> = emptyList(),
            children: List<Branch> = emptyList(),
            execution: (CommandContext.() -> Unit)? = null
        ) = object : Branch {
            override val arguments = arguments
            override val requirements = requirements
            override val children = children
            override val execution = execution
        }

    }

}

open class MutableBranch(
    override val arguments: MutableList<ArgumentBuilder<out Any, out Any>> = mutableListOf(),
    override val requirements: MutableList<CommandContext.() -> Boolean> = mutableListOf(),
    override val children: MutableList<Branch> = mutableListOf(),
    override var execution: (CommandContext.() -> Unit)? = null
) : Branch {

    fun execute(execution: CommandContext.() -> Unit) {
        this.execution = execution
    }

    // requirements

    fun requires(requirement: CommandContext.() -> Boolean) {
        requirements.add(requirement)
    }

    // branches

    fun branch(builder: MutableBranch.() -> Unit) {
        MutableBranch().apply(builder).also(children::add)
    }

    fun branch(literal: String, builder: MutableBranch.() -> Unit) {
        MutableBranch().apply(builder).also { children.add(it) } // TODO literal missing
    }

    // arguments - literal

    fun literal(literal: String): LiteralArgumentBuilder {
        return LiteralArgumentBuilder(literal).also(arguments::add)
    }

    // arguments - argument

    fun <T : Any> argument(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null): VariableArgumentBuilder<T> {
        return VariableArgumentBuilder(name, VariableArgumentInstruction(type, clazz)).also(arguments::add)
    }

    inline fun <reified T : Any> argument(type: ArgumentType<T>, name: String? = null): VariableArgumentBuilder<T> =
        argument(type, T::class, name)

    inline fun <reified T : ArgumentType<I>, reified I : Any> argument(name: String? = null): VariableArgumentBuilder<I> {
        return argument(T::class.primaryConstructor!!.call(), I::class, name)
    }

    // arguments - resolvable argument

    fun <T : ArgumentResolver<R>, R : Any> resolvable(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null): ResolvableArgumentBuilder<T, R> {
        return ResolvableArgumentBuilder<T, R>(name, VariableArgumentInstruction(type, clazz)).also(arguments::add)
    }

    inline fun <reified T : ArgumentResolver<R>, reified R : Any> resolvable(type: ArgumentType<T>, name: String? = null): ResolvableArgumentBuilder<T, R> =
        resolvable(type, T::class, name)

    inline fun <reified I : ArgumentType<T>, reified T : ArgumentResolver<R>, reified R : Any> resolvable(name: String? = null): ResolvableArgumentBuilder<T, R> =
        resolvable(I::class.primaryConstructor!!.call(), T::class, name)

}

data class CommandBranch(
    var name: String,
    var description: String = "",
    val aliases: MutableList<String> = mutableListOf(),
    override val arguments: MutableList<ArgumentBuilder<out Any, out Any>> = mutableListOf(),
    override val requirements: MutableList<CommandContext.() -> Boolean> = mutableListOf(),
    override val children: MutableList<Branch> = mutableListOf(),
    override var execution: (CommandContext.() -> Unit)? = { },
) : MutableBranch(arguments.toMutableList(), requirements.toMutableList(), children.toMutableList(), execution) {

    fun name(name: String) {
        this.name = name
    }

    fun description(description: String) {
        this.description = description
    }

    fun alias(vararg alias: String) {
        aliases.addAll(alias)
    }

}

fun command(name: String, builder: CommandBranch.() -> Unit) =
    CommandBranch(name).apply(builder)

fun main() {

    command("test") {
        val test by literal("test")
        val test2 by argument(ArgumentTypes.player())
        val test3 by resolvable(ArgumentTypes.playerProfiles())

        execute {
            println(test())
            println(test2())
            println(test3())

            // test2 is a string :eyes:
        }

        branch("test") {

        }

    }

}