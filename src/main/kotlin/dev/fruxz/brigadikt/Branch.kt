@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface Branch {
    val arguments: List<ArgumentBuilder<out Any, out Any>>
    val requirements: List<RequirementContext.() -> Boolean>
    val children: List<Branch>
    val execution: (CommandContext.() -> Unit)?

    fun toMutable(): MutableBranch {
        return MutableBranch(arguments.toMutableList(), requirements.toMutableList(), children.toMutableList(), execution)
    }

    companion object {

        operator fun invoke(
            arguments: List<ArgumentBuilder<out Any, out Any>> = emptyList(),
            requirements: List<RequirementContext.() -> Boolean> = emptyList(),
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
    override val requirements: MutableList<RequirementContext.() -> Boolean> = mutableListOf(),
    override val children: MutableList<Branch> = mutableListOf(),
    override var execution: (CommandContext.() -> Unit)? = null
) : Branch {

    @BrigadiKtDSL
    fun execute(execution: CommandContext.() -> Unit) {
        this.execution = execution
    }

    // requirements

    @BrigadiKtDSL
    fun requires(requirement: RequirementContext.() -> Boolean) {
        requirements.add(requirement)
    }

    // branches

    @BrigadiKtDSL
    fun branch(builder: MutableBranch.() -> Unit) {
        MutableBranch().apply(builder).also(children::add)
    }

    @BrigadiKtDSL
    fun branch(literal: String, builder: MutableBranch.() -> Unit) {
        MutableBranch(arguments = mutableListOf(LiteralArgumentProvider(literal)))
            .apply(builder).also(children::add)
    }

    // arguments - literal

    @BrigadiKtDSL
    fun literal(literal: String): LiteralArgumentProvider {
        return LiteralArgumentProvider(literal).also(arguments::add)
    }

    // arguments - argument

    @BrigadiKtDSL
    fun <T : Any> argument(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null): VariableArgumentProvider<T> {
        return VariableArgumentProvider(name, VariableArgumentInstruction(type, clazz)).also(arguments::add)
    }

    @BrigadiKtDSL
    inline fun <reified T : Any> argument(type: ArgumentType<T>, name: String? = null): VariableArgumentProvider<T> =
        argument(type, T::class, name)

    @BrigadiKtDSL
    inline fun <reified T : ArgumentType<I>, reified I : Any> argument(name: String? = null): VariableArgumentProvider<I> {
        return argument(T::class.primaryConstructor?.call() ?: throw IllegalArgumentException("Supplied ArgumentType has no valid primary constructor - ${I::class.qualifiedName}"), I::class, name)
    }

    // arguments - resolvable argument

    @BrigadiKtDSL
    fun <T : ArgumentResolver<R>, R : Any> resolvable(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null): ResolvableArgumentProvider<T, R> {
        return ResolvableArgumentProvider(name, VariableArgumentInstruction(type, clazz)).also(arguments::add)
    }

    @BrigadiKtDSL
    inline fun <reified T : ArgumentResolver<R>, reified R : Any> resolvable(type: ArgumentType<T>, name: String? = null): ResolvableArgumentProvider<T, R> =
        resolvable(type, T::class, name)

    @BrigadiKtDSL
    inline fun <reified I : ArgumentType<T>, reified T : ArgumentResolver<R>, reified R : Any> resolvable(name: String? = null): ResolvableArgumentProvider<T, R> =
        resolvable(I::class.primaryConstructor?.call() ?: throw IllegalArgumentException("Supplied ArgumentType has no valid primary constructor - ${I::class.qualifiedName}"), T::class, name)

}

data class CommandBranch(
    var name: String,
    var description: String = "",
    val aliases: MutableList<String> = mutableListOf(),
    override val arguments: MutableList<ArgumentBuilder<out Any, out Any>> = mutableListOf(),
    override val requirements: MutableList<RequirementContext.() -> Boolean> = mutableListOf(),
    override val children: MutableList<Branch> = mutableListOf(),
    override var execution: (CommandContext.() -> Unit)? = { },
) : MutableBranch(arguments.toMutableList(), requirements.toMutableList(), children.toMutableList(), execution) {

    @BrigadiKtDSL
    fun name(name: String) {
        this.name = name
    }

    @BrigadiKtDSL
    fun description(description: String) {
        this.description = description
    }

    @BrigadiKtDSL
    fun alias(vararg alias: String) {
        aliases.addAll(alias)
    }

}

@BrigadiKtDSL
fun command(name: String, builder: CommandBranch.() -> Unit) =
    CommandBranch(name).apply(builder)
