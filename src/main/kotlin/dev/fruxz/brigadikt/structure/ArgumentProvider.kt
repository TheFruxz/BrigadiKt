package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.CommandContext
import dev.fruxz.brigadikt.DefaultProvider
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

open class ArgumentProvider<I : Any, O>(
    val lazyArgument: (name: String) -> ArgumentInstruction<I>,
    var name: String?,
    val argumentStorage: KMutableProperty<List<ArgumentInstruction<out Any>>>,
    val default: DefaultProvider<out O>? = null,
    val processor: Processor<I, O>,
) {

    open fun resolve(context: CommandContext): O = try {
        processor.perform(
            context = context,
            input = lazyArgument(name ?: throw IllegalStateException("name (of the argument) not yet present in ArgumentProvider")).resolve(context)
        )
    } catch (e: Exception) {
        default?.provide(context) ?: throw e
    }

    /**
     * Drops the default again, since its now not matching anymore
     */
    fun <T> extend(processor: Processor<O, T>) = ArgumentProvider(
        lazyArgument = lazyArgument,
        name = name,
        argumentStorage = argumentStorage,
        processor = futureProvider@{ context ->
            processor.perform(
                context = context,
                input = this@ArgumentProvider.processor.perform(
                    context = context,
                    input = this
                ),
            )
        },
        default = when (default) {
            null -> null
            else -> DefaultProvider {
                processor.perform(
                    context = this,
                    input = this@ArgumentProvider.default.provide(this)
                )
            }
        }
    )

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentProvider<I, O> {
        if (name == null) name = property.name
        argumentStorage.setter.call(argumentStorage.getter.call() + lazyArgument(name!!)) // TODO rewrite to avoid reflect calls which are notfound in classpath

        return this
    }

    companion object {

        fun <T : Any> create(
            name: String? = null,
            argument: (name: String) -> ArgumentInstruction<T>,
            argumentStorage: KMutableProperty<List<ArgumentInstruction<out Any>>>,
            default: DefaultProvider<T>? = null,
        ) = ArgumentProvider(argument, name, argumentStorage, default) { this }

    }

}