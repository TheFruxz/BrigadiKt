package dev.fruxz.brigadikt.structure

import dev.fruxz.ascend.extension.tryOrNull
import dev.fruxz.brigadikt.CommandContext
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

open class ArgumentProvider<I : Any, O>(
    val lazyArgument: (name: String) -> ArgumentInstruction<I>,
    var name: String?,
    val argumentStorage: KMutableProperty<List<ArgumentInstruction<out Any>>>,
    val default: O? = null,
    val processor: Processor<I, O>,
) {

    open fun resolve(context: CommandContext): O =
        tryOrNull {
            processor.process(
                context = context,
                input = lazyArgument(name ?: throw IllegalStateException("name not yet present in ArgumentProvider")).resolve(context)
            )
        } ?: default ?: throw IllegalStateException("Failed to resolve ArgumentProvider")

    /**
     * Drops the default again, since its now not matching anymore
     */
    fun <T> extend(processor: Processor<O, T>) = ArgumentProvider(
        lazyArgument = lazyArgument,
        name = name,
        argumentStorage = argumentStorage,
        processor = futureProvider@{ context, input ->
            processor.process(
                context = context,
                input = this@ArgumentProvider.processor.process(
                    context = context,
                    input = input
                ),
            )
        },
        default = null
    )

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentProvider<I, O> {
        println("delegate for ${property.name} is ${this.javaClass.simpleName}")
        if (name == null) name = property.name
        argumentStorage.setter.call(argumentStorage.getter.call() + lazyArgument(name!!))

        return this
    }

    companion object {

        fun <T : Any> create(
            name: String? = null,
            argument: (name: String) -> ArgumentInstruction<T>,
            argumentStorage: KMutableProperty<List<ArgumentInstruction<out Any>>>,
            default: T? = null,
        ) = ArgumentProvider(argument, name, argumentStorage, default) { _, input -> input }

    }

}