package dev.fruxz.brigadikt.tree

data class BrigadiktCommand<SENDER>(
    val label: String,
) : BrigadiktBranch(null, mutableSetOf())