@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import dev.fruxz.brigadikt.structure.ArgumentProvider
import dev.fruxz.brigadikt.structure.OptionalArgumentInstruction
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver

fun <I : Any, O> ArgumentProvider<I, O>.optional() = ArgumentProvider(
    lazyArgument = { name -> OptionalArgumentInstruction(this.lazyArgument(name)) },
    name = name,
    processor = processor,
)

fun <O : ArgumentResolver<T>, T : Any> ArgumentProvider<*, O>.resolve() =
    extend { this.resolve(it.raw.source) }

fun <T> ArgumentProvider<*, List<T>>.first() =
    extend { first() }

fun <T> ArgumentProvider<*, List<T>>.last() =
    extend { last() }

fun <T> ArgumentProvider<*, List<T>>.filter(predicate: (T) -> Boolean) =
    extend { filter(predicate) }

fun <T> ArgumentProvider<*, List<T>>.reversed() =
    extend { reversed() }

fun <O> ArgumentProvider<*, String>.string(format: String.() -> O) =
    extend { format(this) }