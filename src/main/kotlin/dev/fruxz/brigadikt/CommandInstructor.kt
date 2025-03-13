@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import com.mojang.brigadier.builder.ArgumentBuilder as BrigadierArgumentBuilder


typealias RawArgBuilder = BrigadierArgumentBuilder<CommandSourceStack, out BrigadierArgumentBuilder<CommandSourceStack, *>>

object CommandInstructor {

    fun decorate(origin: RawArgBuilder, branch: Branch) = origin.apply {
        val requirements = branch.requirements
        val children = branch.children
        val execution = branch.execution

        if (requirements.isNotEmpty()) {
            requires { context ->
                println("Checking requirements for ${branch}")
                requirements.all { true } // TODO: Implement requirements
            }
        }

        execution?.let { execution ->
            executes { context ->
                var resultState = 0
                println("Executing ${branch}")

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

        children.forEach { child ->
            then(produce(origin, child.arguments.first(), child, child.arguments.drop(1)))
        }

    }

    fun produce(origin: RawArgBuilder, argument: ArgumentBuilder<*, *>, branch: Branch, queuedArguments: List<ArgumentBuilder<*, *>>): com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, *> {
        val producedBranch = when (argument) {
            is LiteralArgumentBuilder -> Commands.literal(argument.literal)
            is VariableArgumentBuilder<*> -> Commands.argument(argument.name, argument.instruction.raw)
            is ResolvableArgumentBuilder<*, *> -> Commands.argument(argument.name, argument.instruction.raw)
        }

        val followUp = when (queuedArguments.size) {
            0 -> producedBranch
            else -> producedBranch.then(produce(origin, queuedArguments.first(), branch, queuedArguments.drop(1)))
        }

        val processed = decorate(followUp, branch)

        return origin.then(processed)
    }

    fun compute(command: CommandBranch): com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, *> {
        val root = Commands.literal(command.name)

        val arguments = command.arguments

        return produce(root, arguments.first(), command, arguments.drop(1))

    }

}