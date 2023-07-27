package dev.fruxz.brigadikt

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {

    override fun onEnable() {

        getCommand("testcommand")?.setExecutor { sender, _, label, args ->

            sender.sendMessage("try...")

            try {
                val command = buildUniversalCommand<CommandSender>("testcommand") {
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