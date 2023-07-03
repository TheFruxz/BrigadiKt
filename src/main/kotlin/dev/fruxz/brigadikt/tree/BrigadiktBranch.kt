package dev.fruxz.brigadikt.tree

open class BrigadiktBranch(
    val parent: BrigadiktBranch?,
    val accessibleParameters: MutableSet<Parameter<*>>,
)
