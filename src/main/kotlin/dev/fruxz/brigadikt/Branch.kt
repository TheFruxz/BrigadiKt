@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import dev.fruxz.brigadikt.executor.CommandExecutor
import dev.fruxz.brigadikt.executor.RequirementExecutor
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

data class BranchRequirement(
    val requirement: RequirementExecutor,
    val target: BranchRequirementTarget = BranchRequirementTarget.THIS,
) {

    enum class BranchRequirementTarget {
        THIS,
        PASS_THROUGH,
    }

}

open class Branch(
    val parent: Branch? = null,
    var chatRenderer: ReplyChatRenderer? = null,
    var arguments: List<ArgumentProvider<out Any, out Any>> = listOf(),
    var requirements: List<BranchRequirement> = listOf(),
    var children: List<Branch> = listOf(),
    var execution: CommandExecutor? = null
) {

    fun buildNamePath() = buildList {
        var current: Branch? = this@Branch
        while (current != null) {
            if (current is CommandBranch<*>) {
                addAll(0, listOf(current.plugin.name.lowercase(), "command", current.name))
            }
            addAll(0, current.arguments.map(ArgumentProvider<*, *>::name))
            current = current.parent
        }
    }

    @BrigadiKtDSL
    fun execute(execution: CommandExecutor?) {
        this.execution = execution
    }

    // requirements

    @BrigadiKtDSL
    fun requires(requirement: RequirementExecutor) {
        this.requirements += BranchRequirement(requirement, target = BranchRequirement.BranchRequirementTarget.THIS)
    }

    @BrigadiKtDSL
    fun requires(
        target: BranchRequirement.BranchRequirementTarget,
        requirement: RequirementExecutor
    ) {
        this.requirements += BranchRequirement(requirement, target = target)
    }

    // formatter

    @BrigadiKtDSL
    fun formatter(replyChatRenderer: ReplyChatRenderer) {
        this.chatRenderer = replyChatRenderer
    }

    // branches

    @BrigadiKtDSL
    fun branch(builder: Branch.() -> Unit) {
        children += Branch(
            parent = this,
            chatRenderer = this.chatRenderer,
            requirements = this.requirements.filter { it.target == BranchRequirement.BranchRequirementTarget.PASS_THROUGH } // pass through requirements
        ).apply(builder)
    }

    @BrigadiKtDSL
    fun branch(vararg literals: String, builder: Branch.() -> Unit) {
        children += Branch(
            parent = this,
            chatRenderer = this.chatRenderer,
            arguments = literals.map { LiteralArgumentProvider(it) },
            requirements = this.requirements.filter { it.target == BranchRequirement.BranchRequirementTarget.PASS_THROUGH }, // pass through requirements
        ).apply(builder)
    }

    // arguments - argument

    @BrigadiKtDSL
    fun <T : Any> argument(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null): VariableArgumentProvider<T> {
        return VariableArgumentProvider(name, VariableArgumentInstruction(type, clazz)).also { arguments += it }
    }

    @BrigadiKtDSL
    inline fun <reified T : Any> argument(type: ArgumentType<T>, name: String? = null): VariableArgumentProvider<T> =
        argument(type, T::class, name)


    // arguments - resolvable argument

    @BrigadiKtDSL
    fun <T : ArgumentResolver<R>, R : Any> argument(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null): ResolvableArgumentProvider<T, R> {
        return ResolvableArgumentProvider(name, VariableArgumentInstruction(type, clazz)).also { arguments += it }
    }

    @BrigadiKtDSL
    inline fun <reified T : ArgumentResolver<R>, reified R : Any> argument(type: ArgumentType<T>, name: String? = null): ResolvableArgumentProvider<T, R> =
        argument(type, T::class, name)

}

class CommandBranch<T : Plugin>(
    val plugin: T,
    var name: String,
    var description: String = "",
    var aliases: List<String> = emptyList(),
    chatRenderer: ReplyChatRenderer? = null,
    arguments: List<ArgumentProvider<out Any, out Any>> = listOf(),
    requirements: List<BranchRequirement> = listOf(),
    children: List<Branch> = listOf(),
    execution: CommandExecutor? = null,
) : Branch(
    parent = null,
    chatRenderer = chatRenderer,
    arguments = arguments,
    requirements = requirements,
    children = children,
    execution = execution
) {

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
        aliases += alias
    }

}

@BrigadiKtDSL
fun <T : Plugin> command(plugin: T, name: String, builder: CommandBranch<T>.() -> Unit) =
    CommandBranch(plugin, name).apply(builder)
