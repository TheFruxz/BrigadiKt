package dev.fruxz.brigadikt

import dev.fruxz.ascend.extension.forceCast
import dev.fruxz.brigadikt.domain.ActiveCommandArgument
import kotlin.reflect.KProperty

operator fun <S, T> ActiveCommandArgument<S, T>.getValue(thisRef: Any?, property: KProperty<*>): T =
    this.currentContext?.getArgument(node.name, nodeClass.java.forceCast<Class<T>>())
        ?: throw IllegalStateException("No command context available for ${node.name} at depth ${host.depth}!")