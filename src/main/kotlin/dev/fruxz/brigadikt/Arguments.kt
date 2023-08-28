package dev.fruxz.brigadikt

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import dev.fruxz.brigadikt.domain.ActiveCommandArgument
import dev.fruxz.brigadikt.domain.FrontArgumentBuilder
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

private fun <S, T> FrontArgumentBuilder<S, T>.insertArgument(builder: () -> ActiveCommandArgument<S, T>) =
    builder().also(this.arguments::add)

// argument construction

inline fun <S, reified T : Any> FrontArgumentBuilder<S, T>.customArgument(
    name: String,
    noinline argumentParser: (StringReader?) -> T,
    noinline argumentSuggestions: (CommandContext<*>, SuggestionsBuilder) -> CompletableFuture<Suggestions> = { _, _ -> Suggestions.empty() },
    noinline argumentExamples: () -> Collection<String> = { emptyList() },
): ActiveCommandArgument<S, T> = customArgument(name, T::class, argumentParser, argumentSuggestions, argumentExamples)

fun <S, T> FrontArgumentBuilder<S, T>.customArgument(
    name: String,
    argumentClass: KClass<T & Any>,
    argumentParser: (StringReader?) -> T,
    argumentSuggestions: (CommandContext<*>, SuggestionsBuilder) -> CompletableFuture<Suggestions> = { _, _ -> Suggestions.empty() },
    argumentExamples: () -> Collection<String> = { emptyList() },
): ActiveCommandArgument<S, T> = customArgument(name, object : ArgumentType<T> {

    override fun parse(reader: StringReader?): T =
        argumentParser(reader)

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions> =
        argumentSuggestions(context!!, builder!!)

    override fun getExamples(): MutableCollection<String> =
        argumentExamples().toMutableList()

}, argumentClass)

inline fun <S, reified T : Any> FrontArgumentBuilder<S, T>.customArgument(
    name: String,
    argumentType: ArgumentType<T>
): ActiveCommandArgument<S, T> = customArgument(name, argumentType, T::class)

fun <S, T> FrontArgumentBuilder<S, T>.customArgument(
    name: String,
    argumentType: ArgumentType<T>,
    argumentClass: KClass<T & Any>,
): ActiveCommandArgument<S, T> = customArgument(RequiredArgumentBuilder.argument<S, T>(name, argumentType).build(), argumentClass)

// prepared custom-arguments

inline fun <S, reified T : Any> FrontArgumentBuilder<S, T>.customArgument(
    argumentCommandNode: ArgumentCommandNode<S, T>,
): ActiveCommandArgument<S, T> = customArgument(argumentCommandNode, T::class)

fun <S, T> FrontArgumentBuilder<S, T>.customArgument(
    argumentCommandNode: ArgumentCommandNode<S, T>,
    argumentClass: KClass<T & Any>
): ActiveCommandArgument<S, T> = insertArgument {
    ActiveCommandArgument(
        node = argumentCommandNode,
        nodeClass = argumentClass,
    )
}

// Prepared stock-arguments

fun <S> FrontArgumentBuilder<S, Int>.intArgument(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
): ActiveCommandArgument<S, Int> = ActiveCommandArgument<S, Int>(
    node = RequiredArgumentBuilder.argument<S, Int>(name, IntegerArgumentType.integer(min, max)).build(),
    nodeClass = Int::class,
).also(this.arguments::add)

fun <S> FrontArgumentBuilder<S, String>.stringArgument(
    name: String,
): ActiveCommandArgument<S, String> = insertArgument {
    ActiveCommandArgument<S, String>(
        node = RequiredArgumentBuilder.argument<S, String>(name, StringArgumentType.string()).build(),
        nodeClass = String::class,
    )
}

fun <S> FrontArgumentBuilder<S, String>.greedyStringArgument(
    name: String,
): ActiveCommandArgument<S, String> = insertArgument {
    ActiveCommandArgument<S, String>(
        node = RequiredArgumentBuilder.argument<S, String>(name, StringArgumentType.greedyString()).build(),
        nodeClass = String::class,
    )
}

fun <S> FrontArgumentBuilder<S, String>.wordStringArgument(
    name: String,
): ActiveCommandArgument<S, String> = insertArgument {
    ActiveCommandArgument<S, String>(
        node = RequiredArgumentBuilder.argument<S, String>(name, StringArgumentType.word()).build(),
        nodeClass = String::class,
    )
}