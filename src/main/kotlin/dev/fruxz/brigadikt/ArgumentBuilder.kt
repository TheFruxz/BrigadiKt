@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class VariableArgumentInstruction<T : Any>(
    val raw: ArgumentType<T>,
    val clazz: KClass<T>,
)

sealed interface ArgumentBuilder<T : Any, R : Any> {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Argument<T, R>

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentBuilder<T, R>

}

data class LiteralArgumentBuilder(
    val literal: String,
) : ArgumentBuilder<String, String> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Argument<String, String> {
        println("gv thisRef: $thisRef, property: $property")
        return Argument.literal(literal)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): LiteralArgumentBuilder {
        println("pd thisRef: $thisRef, property: $property")
        return this
    }

}

data class VariableArgumentBuilder<T : Any>(
    val nameFixed: String? = null,
    val instruction: VariableArgumentInstruction<T>,
) : ArgumentBuilder<T, T> {

    lateinit var name: String

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Argument<T, T> {
        println("thisRef: $thisRef, property: $property")
        return Argument.variable(name, instruction.raw, instruction.clazz)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): VariableArgumentBuilder<T> {
        name = when {
            nameFixed != null -> nameFixed
            else -> property.name
        }

        return this
    }

}

data class ResolvableArgumentBuilder<T : ArgumentResolver<R>, R : Any>(
    val nameFixed: String? = null,
    val instruction: VariableArgumentInstruction<out T>,
) : ArgumentBuilder<T, R> {

    lateinit var name: String

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Argument<T, R> {
        return Argument.resolvable(name, instruction.raw, instruction.clazz)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ResolvableArgumentBuilder<T, R> {
        name = when {
            nameFixed != null -> nameFixed
            else -> property.name
        }

        return this
    }

}