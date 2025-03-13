@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes

fun main() {

    command("test") {
        // format()
        val name by argument(StringArgumentType.word())

        requires {
            isPlayer && sender.hasSubPermission(".self")
        }

        execute {
            if (!isPlayer) return@execute fail()

            reply("AMOGUS! ${name()}")

        }

        branch {
            val target by resolvable(ArgumentTypes.player())

            execute {
                if (target().first().isOp) return@execute fail {
                    reply("You can't target an operator!")
                }

                reply("AMAZING!")
            }
        }
    }.also { println(CommandInstructor.compute(it)) }

}