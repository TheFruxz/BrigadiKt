package dev.fruxz.brigadikt.executor

import dev.fruxz.brigadikt.CommandContext

fun interface CommandExecutor {

    fun CommandContext.execution()

    fun perform(context: CommandContext) = context.execution()

}