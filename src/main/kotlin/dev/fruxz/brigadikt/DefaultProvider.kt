package dev.fruxz.brigadikt

fun interface DefaultProvider<T> {

    fun CommandContext.provider(): T

    fun provide(context: CommandContext): T = context.provider()

}

