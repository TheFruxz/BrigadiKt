package dev.fruxz.brigadikt

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {

    fun schedule(code: () -> Unit) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, code, 20*10)
    }

    override fun onEnable() {

        val command = buildUniversalCommand<CommandSender>("testcommand") {
            path {
                val test = intArgument("test")

                executes {
                    source.sendMessage("test: $test")

                    schedule { source.sendMessage("test: $test again") }

                }

                path {
                    val test2 = stringArgument("test2")

                    executes {
                        source.sendMessage("test2: ${test2} and test: $test")

                        schedule { source.sendMessage("test2: $test2 and test: $test again") }

                    }

                }

            }
        }

        val dispatcher = CommandDispatcher<CommandSender>()
        dispatcher.register(command)

        getCommand("testcommand")?.setExecutor { sender, _, label, args ->

            sender.sendMessage("try...")

            try {

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