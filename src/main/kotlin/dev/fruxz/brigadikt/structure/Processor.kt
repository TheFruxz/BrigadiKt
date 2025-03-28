package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.CommandContext

fun interface Processor<I, O> {

    fun I.processor(context: CommandContext): O

    fun perform(context: CommandContext, input: I): O = input.processor(context)

}