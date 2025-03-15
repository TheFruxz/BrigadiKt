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

    fun render(commandBranch: CommandBranch): BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack> {
        val raw = Commands.literal(commandBranch.name)

        return populate(raw, commandBranch, commandBranch.arguments).takeIfInstance<BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack>>() ?: throw IllegalStateException("Failed to render command")
    }

    fun populate(raw: PaperArgBuilder, branch: Branch, queuedArguments: List<ArgumentBuilder<*, *>>): PaperArgBuilder {

        if (branch.requirements.isNotEmpty()) {
            raw.requires { context ->
                println("Checking requirements for ${branch}")
                branch.requirements.all { it.invoke(context) }
            }
        }

        when (queuedArguments.size) {
            0 -> {
                val children = branch.children
                if (children.isNotEmpty()) {
                    children.forEach { child ->
                        populate(raw, child, child.arguments)
                    }
                } else {
                    throw IllegalStateException("Empty path, no queued arguments and no children")
                }

                return raw
            }
            1 -> {
                val path = when (val argument = queuedArguments.first()) {
                    is LiteralArgumentBuilder -> Commands.literal(argument.literal)
                    is VariableArgumentBuilder<*> -> Commands.argument(argument.name, argument.instruction.raw)
                    is ResolvableArgumentBuilder<*, *> -> Commands.argument(argument.name, argument.instruction.raw)
                }

                val children = branch.children
                if (children.isNotEmpty()) {
                    children.forEach { child ->
                        populate(path, child, child.arguments)
                    }
                }

                val execution = branch.execution
                if (execution != null) {
                    path.executes { context ->
                        println("Executing ${branch}")
                        var resultState = 0
                        execution.invoke(object : CommandContext(
                            context,
                            emptyMap()
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
                    is LiteralArgumentBuilder -> Commands.literal(argument.literal)
                    is VariableArgumentBuilder<*> -> Commands.argument(argument.name, argument.instruction.raw)
                    is ResolvableArgumentBuilder<*, *> -> Commands.argument(argument.name, argument.instruction.raw)
                }

                return raw.then(
                    populate(producedBranch, branch, queuedArguments.drop(1))
                )
            }
        }
    }

}