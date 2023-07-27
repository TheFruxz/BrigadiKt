package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.ArgumentCommandNode
import kotlin.reflect.KClass

data class ActiveCommandArgument<S, T>(
    val node: ArgumentCommandNode<S, T>,
    val nodeClass: KClass<T & Any>,
    val host: FrontArgumentBuilder<S, *>,
) {

    var currentContext: CommandContext<S>? = null

}
