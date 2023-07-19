package dev.fruxz.brigadikt

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.fruxz.brigadikt.tree.buildCommand
import dev.fruxz.brigadikt.tree.commandPath
import dev.fruxz.brigadikt.tree.getValue
import dev.fruxz.brigadikt.tree.intArgument
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {

    override fun onEnable() {

        getCommand("testcommand")?.setExecutor { sender, command, label, args ->

            sender.sendMessage("try...")

            try {
                val command = buildCommand<CommandSender>("testcommand") {
                    commandPath {
                        val test by intArgument("amogus")
//                        val test2 by intArgument("amogus2")

                        executes {
                            it.source.sendMessage("amogus $test")
                        }

                    }
                }

                val dispatcher = CommandDispatcher<CommandSender>()
                dispatcher.register(command)

                dispatcher.execute(args.joinToString(" "), sender)
            } catch (e: CommandSyntaxException) {
                sender.sendMessage("error: ${e.message}")
            } finally {
                sender.sendMessage("done!")
            }

            true
        }

    }

}