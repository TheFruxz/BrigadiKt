package dev.fruxz.brigadikt.tree

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.arguments.StringArgumentType.StringType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import dev.fruxz.brigadikt.annotation.ExperimentalBrigadiktAPI
import dev.fruxz.brigadikt.domain.ActiveCommandArgument
import dev.fruxz.brigadikt.domain.ArgumentTypeBuilder
import dev.fruxz.brigadikt.domain.FrontArgumentBuilder
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

fun <S, T> buildUniversalArgumentType(builder: ArgumentTypeBuilder<T, S>.() -> Unit): ArgumentType<T> =
    ArgumentTypeBuilder<T, S>().apply(builder).produce()

fun <S, T> FrontArgumentBuilder<S>.insertArgument(builder: () -> ActiveCommandArgument<S, T>) =
    builder().also(this.arguments::add)

// general

inline fun <S, reified T : Any> FrontArgumentBuilder<S>.argument(
    name: String,
    type: ArgumentType<T>,
): ActiveCommandArgument<S, T> = argument(RequiredArgumentBuilder.argument<S, T>(name, type).build(), T::class)

fun <S, T> FrontArgumentBuilder<S>.argument(
    name: String,
    argumentType: ArgumentType<T>,
    argumentClass: KClass<T & Any>,
): ActiveCommandArgument<S, T> = argument(RequiredArgumentBuilder.argument<S, T>(name, argumentType).build(), argumentClass)

inline fun <S, reified T : Any> FrontArgumentBuilder<S>.argument(
    argumentCommandNode: ArgumentCommandNode<S, T>,
): ActiveCommandArgument<S, T> = argument(argumentCommandNode, T::class)

fun <S, T> FrontArgumentBuilder<S>.argument(
    argumentCommandNode: ArgumentCommandNode<S, T>,
    argumentClass: KClass<T & Any>
): ActiveCommandArgument<S, T> = insertArgument {
    ActiveCommandArgument(
        node = argumentCommandNode,
        nodeClass = argumentClass,
    )
}

// static

inline fun <S, reified T : Any> FrontArgumentBuilder<S>.argumentStatic(
    name: String,
    value: T,
    crossinline stringify: (T) -> String = { it.toString() },
) = argument<S, T>(
    name = name,
    type = buildUniversalArgumentType<S, T> {
        suggest { _, builder -> builder.suggest(stringify(value)).buildFuture() }
        parse { reader ->
            val static = stringify(value)
            val input = reader.readString()

            if (input != static) throw SimpleCommandExceptionType { "Only '$static' is possible here!" }.createWithContext(reader)

            return@parse value
        }
    }
)

inline fun <S, reified T : Any> FrontArgumentBuilder<S>.argumentListEntry(
    name: String,
    value: Iterable<T>,
    crossinline stringify: (T) -> String = { it.toString() },
) = argument<S, T>(
    name = name,
    type = buildUniversalArgumentType<S, T> {
        suggest { _, builder ->
            value.forEach { builder.suggest(stringify(it)) }
            builder.buildFuture()
        }
        parse { reader ->
            val input = reader.readString()

            value.forEach { entry ->
                if (input == stringify(entry)) return@parse entry
            }

            throw SimpleCommandExceptionType { "Only one of ${value.joinToString { stringify(it) }} is possible here!" }.createWithContext(reader)
        }
    }
)

fun <S, T> FrontArgumentBuilder<S>.argumentListIndex(
    name: String,
    value: Iterable<T>,
    stringify: (T) -> String = { it.toString() },
) = argument<S, Int>(
    name = name,
    type = buildUniversalArgumentType<S, Int> {
        suggest { _, builder ->
            value.forEach { builder.suggest(stringify(it)) }
            builder.buildFuture()
        }
        parse { reader ->
            val input = reader.readString()

            value.withIndex().forEach { (index, entry) ->
                if (input == stringify(entry)) return@parse index
            }

            throw SimpleCommandExceptionType { "Only one of ${value.joinToString { stringify(it) }} is possible here!" }.createWithContext(reader)
        }
    }
)

// string

fun <S> FrontArgumentBuilder<S>.argumentString(name: String) = argument(
    name = name,
    type = StringArgumentType.string()
)

fun <S> FrontArgumentBuilder<S>.argumentWord(name: String) = argument(
    name = name,
    type = StringArgumentType.word()
)

fun <S> FrontArgumentBuilder<S>.argumentGreedyString(name: String) = argument(
    name = name,
    type = StringArgumentType.greedyString()
)

@ExperimentalBrigadiktAPI
fun <S> FrontArgumentBuilder<S>.argumentStringRegex(
    name: String,
    regex: Regex,
    type: StringType = StringType.SINGLE_WORD,
) = argument(
    name = name,
    type = when (type) {
        StringType.SINGLE_WORD -> StringArgumentType.word()
        StringType.QUOTABLE_PHRASE -> StringArgumentType.string()
        StringType.GREEDY_PHRASE -> StringArgumentType.greedyString()
    }.let { base ->
        return@let object : ArgumentType<String> {

            @Throws(CommandSyntaxException::class)
            override fun parse(reader: StringReader): String {
                val input = base.parse(reader)

                when (regex.matches(input)) {
                    true -> return input
                    false -> throw CommandSyntaxException(SimpleCommandExceptionType { "Input does not match regex: $regex" }) { "Input does not match regex: $regex" }
                }

            }

            override fun getExamples(): Collection<String> =
                base.examples

            override fun <S : Any?> listSuggestions(
                context: CommandContext<S>,
                builder: SuggestionsBuilder,
            ): CompletableFuture<Suggestions> =
                base.listSuggestions(context, builder)

        }
    }
)

// numbers

fun <S> FrontArgumentBuilder<S>.argumentInteger(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
) = argument(
    name = name,
    type = IntegerArgumentType.integer(min, max)
)

fun <S> FrontArgumentBuilder<S>.argumentDouble(
    name: String,
    min: Double = Double.MIN_VALUE,
    max: Double = Double.MAX_VALUE,
) = argument(
    name = name,
    type = DoubleArgumentType.doubleArg(min, max)
)

fun <S> FrontArgumentBuilder<S>.argumentFloat(
    name: String,
    min: Float = Float.MIN_VALUE,
    max: Float = Float.MAX_VALUE,
) = argument(
    name = name,
    type = FloatArgumentType.floatArg(min, max)
)

fun <S> FrontArgumentBuilder<S>.argumentLong(
    name: String,
    min: Long = Long.MIN_VALUE,
    max: Long = Long.MAX_VALUE,
) = argument(
    name = name,
    type = LongArgumentType.longArg(min, max)
)

// bool

fun <S> FrontArgumentBuilder<S>.argumentBool(
    name: String,
) = argument(
    name = name,
    type = BoolArgumentType.bool()
)