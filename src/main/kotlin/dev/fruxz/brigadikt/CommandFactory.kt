@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import dev.fruxz.ascend.extension.objects.takeIfInstance
import dev.fruxz.brigadikt.structure.ArgumentInstruction
import dev.fruxz.brigadikt.structure.OptionalArgumentInstruction
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import com.mojang.brigadier.builder.ArgumentBuilder as BrigadierBuilderArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder as BrigadierBuilderLiteralArgumentBuilder

typealias PaperArgBuilder = BrigadierBuilderArgumentBuilder<CommandSourceStack, out BrigadierBuilderArgumentBuilder<CommandSourceStack, *>>

object CommandFactory {

    fun renderCommand(commandBranch: CommandBranch<*>): BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack> {
        val raw = Commands.literal(commandBranch.name)

        return renderBranch(raw, commandBranch, commandBranch.arguments).takeIfInstance<BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack>>() ?:
            throw IllegalStateException("Failed to render compatible command '${commandBranch.name}'!")
    }

    fun CommandBranch<*>.render(): BrigadierBuilderLiteralArgumentBuilder<CommandSourceStack> =
        renderCommand(this)

    fun renderBranch(
        entrypoint: PaperArgBuilder,
        branch: Branch,
        queue: List<ArgumentInstruction<*>>,
    ): PaperArgBuilder = entrypoint.apply {
        val requirements = branch.requirements
        val children = branch.children
        val execution = branch.execution
        val nextArgument = queue.firstOrNull()

        if (queue.none { it !is OptionalArgumentInstruction<*> } && execution != null) {

            if (requirements.isNotEmpty()) {
                this.requires {
                    requirements.all { requirement ->
                        requirement.requirement.perform(
                            RequirementContext(
                                raw = it,
                                path = branch.buildNamePath()
                            )
                        )
                    }
                }
            }

            this.executes { context ->
                var resultState = 0

                execution.perform(
                    CommandContext(
                        raw = context,
                        path = branch.buildNamePath(),
                        replyRenderer = branch.chatRenderer,
                        state = { state -> resultState = state; }
                    )
                )

                return@executes resultState
            }
        }

        when(val produce = nextArgument?.produce()) {
            null -> children.forEach {
                child -> renderBranch(this, child, child.arguments)
            }
            else -> produce.results.forEach { result ->
                this.then(
                    renderBranch(result, branch, queue.drop(1))
                        .requires {
                            requirements.all { requirement ->
                                requirement.requirement.perform(
                                    RequirementContext(
                                        raw = it,
                                        path = branch.buildNamePath()
                                    )
                                )
                            }
                        }
                )
            }
        }

    }

}