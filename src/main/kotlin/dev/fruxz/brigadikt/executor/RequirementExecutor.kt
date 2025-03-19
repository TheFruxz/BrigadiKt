package dev.fruxz.brigadikt.executor

import dev.fruxz.brigadikt.RequirementContext

fun interface RequirementExecutor {
    fun RequirementContext.requirement(): Boolean
}