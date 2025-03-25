@file:Suppress("UnstableApiUsage", "OPT_IN_USAGE_FUTURE_ERROR")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import dev.fruxz.brigadikt.executor.CommandExecutor
import dev.fruxz.brigadikt.executor.RequirementExecutor
import dev.fruxz.brigadikt.structure.*
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
    var arguments: List<ArgumentInstruction<out Any>> = listOf(),
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
            addAll(0, current.arguments.map(ArgumentInstruction<out Any>::displayName))
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
            arguments = when (literals.size) {
                0 -> emptyList()
                1 -> listOf(LiteralArgumentInstruction(literals.first()))
                else -> listOf(LiteralChainArgumentInstruction(
                    literals.map(::LiteralArgumentInstruction)
                ))
            },
            requirements = this.requirements.filter { it.target == BranchRequirement.BranchRequirementTarget.PASS_THROUGH }, // pass through requirements
        ).apply(builder)
    }

    // arguments - argument

    @BrigadiKtDSL
    fun <T : Any> argument(type: ArgumentType<T>, clazz: KClass<T>, name: String? = null) = ArgumentProvider.create(
        name = name,
        argument = { VariableArgumentInstruction(it, type, clazz) }
    )

    @BrigadiKtDSL
    inline fun <reified T : Any> argument(type: ArgumentType<T>, name: String? = null): ArgumentProvider<*, T> =
        argument(type, T::class, name)


    // arguments - resolvable argument

    @BrigadiKtDSL
    @JvmName("argumentResolvable")
    fun <T : ArgumentResolver<R>, R : Any> argument(resolvableType: ArgumentType<T>, clazz: KClass<T>, name: String? = null) =
        argument(type = resolvableType, clazz = clazz, name = name).resolve()

    @BrigadiKtDSL
    @JvmName("argumentResolvable")
    inline fun <reified T : ArgumentResolver<R>, reified R : Any> argument(resolvableType: ArgumentType<T>, name: String? = null) =
        argument(type = resolvableType, T::class, name).resolve()

    // switch

    fun switch(vararg options: String) = ArgumentProvider.create(null) { SwitchArgumentInstruction(options.toSet()) }

}

class CommandBranch<T : Plugin>(
    val plugin: T,
    var name: String,
    var description: String = "",
    var aliases: List<String> = emptyList(),
    chatRenderer: ReplyChatRenderer? = null,
    arguments: List<ArgumentInstruction<out Any>> = listOf(),
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
