@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt.structure

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.fruxz.ascend.tool.smart.generate.producible.Producible
import dev.fruxz.brigadikt.CommandContext
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass


fun interface Processor<I, O> {
    fun I.process(context: CommandContext): O
} // TODO move to own class

sealed interface ArgumentInstruction<O> : Producible<ArgumentInstructionResult<*>>, Identified {

    val uuid: UUID

    val displayName: String

    fun resolve(context: CommandContext): O

    override fun identity() = Identity.identity(uuid)

}

data class LiteralArgumentInstruction(
    val literal: String,
    override val uuid: UUID = UUID.randomUUID(),
) : ArgumentInstruction<Nothing> {

    override val displayName: String by ::literal

    override fun resolve(context: CommandContext): Nothing =
        throw IllegalStateException("LiteralArgumentInstruction could not be resolved")

    override fun produce() =
        SingleArgumentInstructionResult(
            instruction = this,
            result = Commands.literal(literal),
        )

}

data class LiteralChainArgumentInstruction(
    val literals: List<LiteralArgumentInstruction>,
    override val uuid: UUID = UUID.randomUUID(),
) : ArgumentInstruction<Nothing> {

    override val displayName: String = literals.joinToString(" ")

    override fun resolve(context: CommandContext): Nothing =
        throw IllegalStateException("LiteralChainArgumentInstruction could not be resolved")

    override fun produce(): SingleArgumentInstructionResult {
        var builder = Commands.literal(literals.first().literal)

        literals.drop(1).forEach { literal ->
            builder = builder.then(Commands.literal(literal.literal))
        }

        return SingleArgumentInstructionResult(
            instruction = this,
            result = builder,
        )
    }

}

data class VariableArgumentInstruction<T : Any>(
    val name: String,
    val type: ArgumentType<T>,
    val clazz: KClass<T>,
    override val uuid: UUID = UUID.randomUUID(),
) : ArgumentInstruction<T> {

    override val displayName: String by ::name

    override fun resolve(context: CommandContext): T =
        context.raw.getArgument(name, clazz.java)

    override fun produce() =
        SingleArgumentInstructionResult(
            instruction = this,
            result = Commands.argument(name, type),
        )

}

data class SwitchArgumentType(val options: Set<String>) : CustomArgumentType<String, String> {

    override fun parse(reader: StringReader): String =
        reader.readString().takeIf { it in options } ?: throw IllegalArgumentException("Invalid switch")

    override fun getNativeType(): StringArgumentType = when {
        options.none { it.contains(" ") } -> StringArgumentType.word()
        else -> StringArgumentType.string()
    }

    override fun <S : Any> listSuggestions(
        context: com.mojang.brigadier.context.CommandContext<S>,
        builder: SuggestionsBuilder
    ) = CompletableFuture.supplyAsync {
        options.forEach {
            if (!it.contains(builder.input.drop(builder.start).split(' ').first(), true)) return@forEach
            builder.suggest(it)
        }
        builder.build()
    }

}

data class SwitchArgumentInstruction(
    val options: Set<String>,
    override val uuid: UUID = UUID.randomUUID(),
) : ArgumentInstruction<String> {

    init {
        require(options.none { it.isEmpty() }) { "Empty options are not allowed" }
    }

    override val displayName: String = options.joinToString("|")

    override fun resolve(context: CommandContext): String =
        context.raw.getArgument(displayName, String::class.java)

    override fun produce() =
        SingleArgumentInstructionResult(
            instruction = this,
            result = Commands.argument(displayName, SwitchArgumentType(options)),
        )

}

data class OptionalArgumentInstruction<O>(
    val instruction: ArgumentInstruction<O>,
    override val uuid: UUID = UUID.randomUUID(),
) : ArgumentInstruction<O> {

    override val displayName: String = "(${instruction.displayName})"

    override fun resolve(context: CommandContext): O =
        instruction.resolve(context)

    override fun produce() = instruction.produce()

}