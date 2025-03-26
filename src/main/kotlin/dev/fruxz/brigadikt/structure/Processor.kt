package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.CommandContext

fun interface Processor<I, O> {

    fun process(context: CommandContext, input: I): O

}