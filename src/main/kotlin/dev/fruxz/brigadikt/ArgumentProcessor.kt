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
    extend { context -> resolve(context.raw.source) }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.first() =
    extend { first() }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.last() =
    extend { last() }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.filter(predicate: (O) -> Boolean) =
    extend { filter(predicate) }

fun <O, I : Iterable<O>> ArgumentProvider<*, I>.reversed() =
    extend { reversed() }

fun <O> ArgumentProvider<*, String>.string(format: String.() -> O) =
    extend { format(this) }