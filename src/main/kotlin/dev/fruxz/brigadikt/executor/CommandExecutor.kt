package dev.fruxz.brigadikt.executor

import dev.fruxz.brigadikt.CommandContext

fun interface CommandExecutor {
    fun CommandContext.execution()
}