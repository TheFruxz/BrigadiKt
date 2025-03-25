@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import dev.fruxz.ascend.extension.objects.takeIfInstance
import dev.fruxz.brigadikt.structure.ArgumentInstruction
import dev.fruxz.brigadikt.structure.MultipleArgumentInstructionResult
import dev.fruxz.brigadikt.structure.SingleArgumentInstructionResult
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import com.mojang.brigadier.builder.ArgumentBuilder as BrigadierBuilderArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder as BrigadierBuilderLiteralArgumentBuilder

typealias PaperArgBuilder = BrigadierBuilderArgumentBuilder<CommandSourceStack, out BrigadierBuilderArgumentBuilder<CommandSourceStack, *>>

// v2 attempt
object CommandFactory {

    fun renderCommand(commandBranch: CommandBranch<*>): BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack> {
        val raw = Commands.literal(commandBranch.name)

        return renderBranch(raw, commandBranch, commandBranch.arguments).takeIfInstance<BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack>>() ?: throw IllegalStateException("Failed to render command")
    }

    fun CommandBranch<*>.render(): BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack> =
        renderCommand(this)

    fun renderBranch(raw: PaperArgBuilder, branch: Branch, queuedArguments: List<ArgumentInstruction<*>>): List<PaperArgBuilder> {

        if (branch.requirements.isNotEmpty()) {
            raw.requires { context ->
                branch.requirements.all { requirement ->
                    with(requirement.requirement) {
                        val requirementContext = object : RequirementContext(
                            raw = context,
                            path = branch.buildNamePath()
                        ) {}

                        return@all requirementContext.requirement()
                    }
                }
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

                return listOf(raw)
            }
            1 -> {
                val path = queuedArguments.first().produce()
                val results = when (path) {
                    is SingleArgumentInstructionResult -> listOf(path.result)
                    is MultipleArgumentInstructionResult -> path.results
                }
                val produced = results.map { result ->

                    val children = branch.children
                    if (children.isNotEmpty()) {
                        children.forEach { child ->
                            renderBranch(result, child, child.arguments)
                        }
                    }

                    val execution = branch.execution
                    if (execution != null) {
                        result.executes { context ->
                            var resultState = 0
                            val commandContext = object : CommandContext(
                                raw = context,
                                path = branch.buildNamePath(),
                                replyRenderer = branch.chatRenderer,
                            ) {
                                override fun state(state: Int, process: () -> Unit) {
                                    resultState = state
                                    process()
                                }
                            }

                            with(execution) { commandContext.execution() }

                            return@executes resultState
                        }
                    }

                    return@map raw.then(result)
                }

                return produced
            }
            else -> {
                val argument = queuedArguments.first()
                return when (val producedBranch = argument.produce()) {
                    is SingleArgumentInstructionResult -> listOf(raw.then(producedBranch.result))
                    is MultipleArgumentInstructionResult -> {
                        producedBranch.results.map { result -> // TODO maybe foreach instead, since it should only return the branch base`?
                            raw.then(result)
                        }
                    }
                }
            }
        }
    }

}