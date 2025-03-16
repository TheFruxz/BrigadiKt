@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import dev.fruxz.ascend.extension.data.randomTag
import dev.fruxz.ascend.extension.forceCastOrNull
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import kotlin.reflect.KClass

sealed interface ArgumentReference<T : Any, R : Any> {

    val name: String

    fun resolve(context: CommandContext): R

    companion object {

        fun literal(literal: String) = LiteralArgumentReference(literal)

        fun <T : Any> variable(name: String, raw: ArgumentType<T>, clazz: KClass<T>) = VariableArgumentReference(name, raw, clazz)

        fun <T : ArgumentResolver<R>, R : Any> resolvable(name: String, raw: ArgumentType<out T>, clazz: KClass<out T>) = ResolvableArgumentReference(name, raw, clazz)

    }

}

data class LiteralArgumentReference(val literal: String) : ArgumentReference<String, String> {

    override val name = randomTag()

    override fun resolve(context: CommandContext): String {
        return literal
    }

}

data class VariableArgumentReference<T : Any>(
    override val name: String,
    val raw: ArgumentType<T>,
    val clazz: KClass<T>,
) : ArgumentReference<T, T> {

    override fun resolve(context: CommandContext): T {
        return context.cachedArguments[name].forceCastOrNull() ?: context.raw.getArgument(name, clazz.java)
    }

}

data class ResolvableArgumentReference<T : ArgumentResolver<R>, R : Any>(
    override val name: String,
    val raw: ArgumentType<out T>,
    val clazz: KClass<out T>,
) : ArgumentReference<T, R> {

    override fun resolve(context: CommandContext): R {
        val result = context.cachedArguments[name].forceCastOrNull() ?: context.raw.getArgument(name, clazz.java)
        if (result !is ArgumentResolver<R>) throw IllegalArgumentException("Argument $name is not an ArgumentResolver")

        return result.resolve(context.raw.source)
    }

}

data class ProcessedArgumentReference<T : Any, R : Any, O : Any>(
    val original: ArgumentReference<T, R>,
    val processor: ArgumentProcessor<R, O>,
) : ArgumentReference<T, O> {

    override val name: String = original.name

    override fun resolve(context: CommandContext): O {
        return context.cachedArguments[name].forceCastOrNull<O>() ?: processor.process(context, original.resolve(context))
    }

}