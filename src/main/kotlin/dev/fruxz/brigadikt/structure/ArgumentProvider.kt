package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.Branch
import dev.fruxz.brigadikt.CommandContext
import kotlin.reflect.KProperty

open class ArgumentProvider<I : Any, O>(
    val lazyArgument: (name: String) -> ArgumentInstruction<I>,
    var name: String?,
    val processor: Processor<I, O>,
) {

    open fun resolve(context: CommandContext): O = with(processor) {
        lazyArgument(name ?: throw IllegalStateException("name is not yet present in ArgumentProvider")).resolve(context).process(context)
    }

    fun <T> extend(processor: Processor<O, T>) = ArgumentProvider(lazyArgument, name) futureProvider@{
        with(processor) { (with(this@ArgumentProvider.processor) { process(it) }).process(it) }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ArgumentProvider<I, O> {
        if (name == null) name = property.name
        if (thisRef is Branch) thisRef.arguments += this.lazyArgument(name!!)

        return this
    }

//    private fun toReference() = ArgumentReference(name ?: throw IllegalStateException("name is not yet present in ArgumentProvider"), lazyArgument(name!!), processor)

    companion object {

        fun <T : Any> create(
            name: String? = null,
            argument: (name: String) -> ArgumentInstruction<T>,
        ) = ArgumentProvider(argument, name) { this }

    }

}