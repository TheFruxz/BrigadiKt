@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class VariableArgumentInstruction<T : Any>(
    val raw: ArgumentType<T>,
    val clazz: KClass<T>,
)

sealed interface ArgumentBuilder<T : Any, R : Any> {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<T, R>

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentBuilder<T, R>

}

data class LiteralArgumentProvider(
    val literal: String,
) : ArgumentBuilder<String, String> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<String, String> {
        return ArgumentReference.literal(literal)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): LiteralArgumentProvider {
        return this
    }

}

data class VariableArgumentProvider<T : Any>(
    val nameFixed: String? = null,
    val instruction: VariableArgumentInstruction<T>,
) : ArgumentBuilder<T, T> {

    lateinit var name: String

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<T, T> {
        return ArgumentReference.variable(name, instruction.raw, instruction.clazz)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): VariableArgumentProvider<T> {
        name = when {
            nameFixed != null -> nameFixed
            else -> property.name
        }

        return this
    }

}

data class ResolvableArgumentProvider<T : ArgumentResolver<R>, R : Any>(
    val nameFixed: String? = null,
    val instruction: VariableArgumentInstruction<out T>,
) : ArgumentBuilder<T, R> {

    lateinit var name: String

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<T, R> {
        return ArgumentReference.resolvable(name, instruction.raw, instruction.clazz)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ResolvableArgumentProvider<T, R> {
        name = when {
            nameFixed != null -> nameFixed
            else -> property.name
        }

        return this
    }

}