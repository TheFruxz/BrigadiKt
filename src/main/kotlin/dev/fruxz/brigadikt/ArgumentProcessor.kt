@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver

fun interface ArgumentProcessor<R : Any, O : Any> {
    fun process(context: CommandContext, raw: R): O
}

fun <T : Any, R : Any, O : Any> ArgumentProvider<T, R>.processor(processor: ArgumentProcessor<R, O>) =
    ArgumentProviderProcessor(this, processor)

fun <R : ArgumentResolver<O>, O : Any> ArgumentProvider<*, R>.resolve() =
    processor { context, raw -> raw.resolve(context.raw.source) }

fun <R : Iterable<O>, O : Any> ArgumentProvider<*, R>.first() =
    processor { _, raw -> raw.first() }

fun <R : Iterable<O>, O : Any> ArgumentProvider<*, R>.last() =
    processor { _, raw -> raw.last() }

fun <R : Iterable<O>, O : Any> ArgumentProvider<*, R>.filter(predicate: (O) -> Boolean) =
    processor { _, raw -> raw.filter(predicate) }

fun <R : Iterable<O>, O : Any> ArgumentProvider<*, R>.reversed() =
    processor { _, raw -> raw.reversed() }