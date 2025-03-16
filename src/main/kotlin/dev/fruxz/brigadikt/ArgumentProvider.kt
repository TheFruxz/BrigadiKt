@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.ArgumentType
import dev.fruxz.ascend.tool.smart.generate.producible.Producible
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class VariableArgumentInstruction<T : Any>(
    val raw: ArgumentType<T>,
    val clazz: KClass<T>,
)

sealed interface ArgumentProvider<T : Any, R : Any> : Producible<PaperArgBuilder> {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<T, R>

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentProvider<T, R> {
        return this
    }

    override fun produce(): PaperArgBuilder

}

data class LiteralArgumentProvider(
    val literal: String,
) : ArgumentProvider<String, String> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<String, String> {
        return ArgumentReference.literal(literal)
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): LiteralArgumentProvider {
        return this
    }

    override fun produce(): PaperArgBuilder {
        return Commands.literal(literal)
    }

}

data class VariableArgumentProvider<T : Any>(
    val nameFixed: String? = null,
    val instruction: VariableArgumentInstruction<T>,
) : ArgumentProvider<T, T> {

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

    override fun produce(): PaperArgBuilder {
        return Commands.argument(name, instruction.raw)
    }

}

data class ResolvableArgumentProvider<T : ArgumentResolver<R>, R : Any>(
    val nameFixed: String? = null,
    val instruction: VariableArgumentInstruction<out T>,
) : ArgumentProvider<T, R> {

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

    override fun produce(): PaperArgBuilder {
        return Commands.argument(name, instruction.raw)
    }

}

data class ArgumentProviderProcessor<T : Any, R : Any, O : Any>(
    val input: ArgumentProvider<T, R>,
    val processor: ArgumentProcessor<R, O>
) : ArgumentProvider<T, O> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): ArgumentReference<T, O> {
        return ProcessedArgumentReference(input.getValue(thisRef, property), processor)
    }

    override fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentProvider<T, O> {
        input.provideDelegate(thisRef, property)
        return this
    }

    override fun produce(): PaperArgBuilder {
        return input.produce()
    }

}