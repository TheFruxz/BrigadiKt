package dev.fruxz.brigadikt.domain

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.fruxz.ascend.extension.forceCast
import dev.fruxz.ascend.tool.smart.generate.producible.Producible
import dev.fruxz.brigadikt.annotation.BrigadiktDSL
import java.util.concurrent.CompletableFuture

data class ArgumentTypeBuilder<T, S>(
    var parser: ArgumentParser<T>? = null,
    var suggestions: SuggestionProvider<S> = SuggestionProvider { _, _ -> Suggestions.empty() },
    var examples: ExampleProvider = ExampleProvider { emptyList() },
) : Producible<ArgumentType<T>> {

    @Throws(CommandSyntaxException::class)
    @BrigadiktDSL
    fun parse(builder: (reader: StringReader) -> T) {
        this.parser = ArgumentParser(builder)
    }

    @BrigadiktDSL
    fun suggest(builder: (context: CommandContext<S>, builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
        this.suggestions = SuggestionProvider(builder)
    }

    @BrigadiktDSL
    fun examples(builder: () -> Collection<String>) {
        this.examples = ExampleProvider(builder)
    }

    override fun produce(): ArgumentType<T> {
        assert(parser != null) { "Argument parser must be set" }

        return object : ArgumentType<T> {

            override fun parse(reader: StringReader): T =
                this@ArgumentTypeBuilder.parser!!.builder.invoke(reader)

            override fun <S> listSuggestions(
                context: CommandContext<S>,
                builder: SuggestionsBuilder
            ): CompletableFuture<Suggestions> =
                this@ArgumentTypeBuilder.suggestions.process.invoke(context.forceCast(), builder)

            override fun getExamples(): Collection<String> =
                this@ArgumentTypeBuilder.examples.builder.invoke()

        }
    }

    @JvmInline
    value class ArgumentParser<T>(val builder: (reader: StringReader) -> T)

    @JvmInline
    value class SuggestionProvider<S>(val process: (context: CommandContext<S>, builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>)

    @JvmInline
    value class ExampleProvider(val builder: () -> Collection<String>)

}