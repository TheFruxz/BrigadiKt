package dev.fruxz.brigadikt

import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import com.mojang.brigadier.builder.ArgumentBuilder as BrigadierBuilderArgumentBuilder

typealias PaperArgBuilder = BrigadierBuilderArgumentBuilder<CommandSourceStack, out BrigadierBuilderArgumentBuilder<CommandSourceStack, *>>

// v2 attempt
object CommandFactory {

    fun render(commandBranch: CommandBranch): com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> {
        val raw = Commands.literal(commandBranch.name)

        return populate(raw, commandBranch, commandBranch.arguments) as com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
    }

    fun populate(raw: PaperArgBuilder, branch: Branch, queuedArguments: List<ArgumentBuilder<*, *>>): PaperArgBuilder {
        when (queuedArguments.size) {
            0 -> {
                throw IllegalStateException("No more arguments to populate")
            }
            1 -> {
                val path = when (val argument = queuedArguments.first()) {
                    is LiteralArgumentBuilder -> Commands.literal(argument.literal)
                    is VariableArgumentBuilder<*> -> Commands.argument(argument.name, argument.instruction.raw)
                    is ResolvableArgumentBuilder<*, *> -> Commands.argument(argument.name, argument.instruction.raw)
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