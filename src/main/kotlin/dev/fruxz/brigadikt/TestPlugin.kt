package dev.fruxz.brigadikt

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.fruxz.brigadikt.arguments.intArgument
import dev.fruxz.brigadikt.arguments.stringArgument
import dev.fruxz.brigadikt.tree.buildCommand
import dev.fruxz.brigadikt.tree.path
import dev.fruxz.brigadikt.tree.getValue
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {

    override fun onEnable() {

        getCommand("testcommand")?.setExecutor { sender, _, label, args ->

            sender.sendMessage("try...")

            try {
                val command = buildCommand<CommandSender>("testcommand") {
                    path {
                        val test by intArgument("test")

                        executes {
                            sender.sendMessage("test: $test")
                        }

                        path {
                            val test2 by stringArgument("test2")

                            executes {
                                sender.sendMessage("test2: $test2 and test: $test")
                            }

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