package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.PaperArgBuilder

sealed interface ArgumentInstructionResult {
    val instruction: ArgumentInstruction<*>
    val results: List<PaperArgBuilder>
}

data class MultipleArgumentInstructionResult(
    override val instruction: ArgumentInstruction<*>,
    override val results: List<PaperArgBuilder>
) : ArgumentInstructionResult

data class SingleArgumentInstructionResult(
    override val instruction: ArgumentInstruction<*>,
    val result: PaperArgBuilder
) : ArgumentInstructionResult {
    override val results: List<PaperArgBuilder> = listOf(result)
}