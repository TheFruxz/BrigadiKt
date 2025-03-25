package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.PaperArgBuilder

sealed interface ArgumentInstructionResult<T>

data class MultipleArgumentInstructionResult(
    val instruction: ArgumentInstruction<*>,
    val results: List<PaperArgBuilder>
) : ArgumentInstructionResult<List<PaperArgBuilder>>

data class SingleArgumentInstructionResult(
    val instruction: ArgumentInstruction<*>,
    val result: PaperArgBuilder
) : ArgumentInstructionResult<PaperArgBuilder>