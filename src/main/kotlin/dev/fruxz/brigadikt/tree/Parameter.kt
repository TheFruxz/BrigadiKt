package dev.fruxz.brigadikt.tree

import com.mojang.brigadier.arguments.ArgumentType

data class Parameter<T>(
    val name: String,
    val type: ArgumentType<T>
) {

    fun ExecutionContext.get() {
        this.getParameterContent(name)
    }

}
