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
import dev.fruxz.brigadikt.domain.FrontArgumentBuilder
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

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