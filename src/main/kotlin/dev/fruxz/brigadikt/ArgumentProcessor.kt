@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import dev.fruxz.brigadikt.structure.ArgumentProvider
import dev.fruxz.brigadikt.structure.OptionalArgumentInstruction
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver

fun <I : Any, O> ArgumentProvider<I, O>.optional(default: O? = null) = ArgumentProvider(
    lazyArgument = { name -> OptionalArgumentInstruction(this.lazyArgument(name)) },
    name = name,
    argumentStorage = argumentStorage,
    processor = processor,
    default = default,
)

fun <O : ArgumentResolver<T>, T : Any> ArgumentProvider<*, O>.resolve() =
    extend { context, input -> input.resolve(context.raw.source) }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.first() =
    extend { _, input -> input.first() }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.last() =
    extend { _, input -> input.last() }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.filter(predicate: (O) -> Boolean) =
    extend { _, input -> input.filter(predicate) }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.reversed() =
    extend { _, input -> input.reversed() }

fun <O> ArgumentProvider<*, String>.string(format: String.() -> O) =
    extend { _, input -> format(input) }