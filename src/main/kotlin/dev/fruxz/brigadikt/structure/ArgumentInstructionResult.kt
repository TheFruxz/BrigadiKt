package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.PaperArgBuilder

sealed interface ArgumentInstructionResult<T> {
    val instruction: ArgumentInstruction<*>
    val results: List<PaperArgBuilder>
}

data class MultipleArgumentInstructionResult(
    override val instruction: ArgumentInstruction<*>,
    override val results: List<PaperArgBuilder>
) : ArgumentInstructionResult<List<PaperArgBuilder>>

data class SingleArgumentInstructionResult(
    override val instruction: ArgumentInstruction<*>,
    val result: PaperArgBuilder
) : ArgumentInstructionResult<PaperArgBuilder> {
    override val results: List<PaperArgBuilder> = listOf(result)
}