@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import dev.fruxz.ascend.extension.objects.takeIfInstance
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import com.mojang.brigadier.builder.ArgumentBuilder as BrigadierBuilderArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder as BrigadierBuilderLiteralArgumentBuilder

typealias PaperArgBuilder = BrigadierBuilderArgumentBuilder<CommandSourceStack, out BrigadierBuilderArgumentBuilder<CommandSourceStack, *>>

// v2 attempt
object CommandFactory {

    fun renderCommand(commandBranch: CommandBranch): BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack> {
        val raw = Commands.literal(commandBranch.name)

        return renderBranch(raw, commandBranch, commandBranch.arguments).takeIfInstance<BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack>>() ?: throw IllegalStateException("Failed to render command")
    }

    fun renderBranch(raw: PaperArgBuilder, branch: Branch, queuedArguments: List<ArgumentBuilder<*, *>>): PaperArgBuilder {

        if (branch.requirements.isNotEmpty()) {
            raw.requires { context ->
                branch.requirements.all { it.invoke(object : RequirementContext(context) {}) }
            }
        }

        when (queuedArguments.size) {
            0 -> {
                val children = branch.children
                if (children.isNotEmpty()) {
                    children.forEach { child ->
                        renderBranch(raw, child, child.arguments)
                    }
                } else {
                    throw IllegalStateException("Empty path, no queued arguments and no children")
                }

                return raw
            }
            1 -> {
                val path = when (val argument = queuedArguments.first()) {
                    is LiteralArgumentProvider -> Commands.literal(argument.literal)
                    is VariableArgumentProvider<*> -> Commands.argument(argument.name, argument.instruction.raw)
                    is ResolvableArgumentProvider<*, *> -> Commands.argument(argument.name, argument.instruction.raw)
                }

                val children = branch.children
                if (children.isNotEmpty()) {
                    children.forEach { child ->
                        renderBranch(path, child, child.arguments)
                    }
                }

                val execution = branch.execution
                if (execution != null) {
                    path.executes { context ->
                        println("Executing ${branch}")
                        var resultState = 0
                        execution.invoke(object : CommandContext(
                            context,
                            mutableMapOf()
                        ) {
                            override fun state(state: Int, process: () -> Unit) {
                                resultState = state
                                process()
                            }
                        })
                        return@executes resultState
                    }
                }

                return raw.then(path)
            }
            else -> {
                val argument = queuedArguments.first()

                val producedBranch = when (argument) {
                    is LiteralArgumentProvider -> Commands.literal(argument.literal)
                    is VariableArgumentProvider<*> -> Commands.argument(argument.name, argument.instruction.raw)
                    is ResolvableArgumentProvider<*, *> -> Commands.argument(argument.name, argument.instruction.raw)
                }

                return raw.then(
                    renderBranch(producedBranch, branch, queuedArguments.drop(1))
                )
            }
        }
    }

}