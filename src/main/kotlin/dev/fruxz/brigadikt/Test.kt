@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Bukkit

val test = command(Bukkit.getPluginManager().plugins.first(), "test") {

    branch("test") {
        val player by argument(ArgumentTypes.players()).first()
        val mode by switch("aktivieren", "deaktivieren")
        val message by argument(StringArgumentType.greedyString()).optional()
        val message2 by message.string { split("_") }

        execute {
            player()
            message()
            message2()
        }

    }

}