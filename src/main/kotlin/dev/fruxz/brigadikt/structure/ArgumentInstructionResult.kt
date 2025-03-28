package dev.fruxz.brigadikt.structure

import dev.fruxz.brigadikt.PaperArgBuilder

data class ArgumentInstructionResult(
    val instruction: ArgumentInstruction<*>,
    val results: List<PaperArgBuilder>,
)