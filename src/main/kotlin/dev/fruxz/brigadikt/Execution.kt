@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.fruxz.ascend.extension.forceCastOrNull
import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.ascend.extension.switch
import dev.fruxz.stacked.extension.api.StyledString
import dev.fruxz.stacked.extension.asStyledComponent
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

interface CommandAccess {

    val sender: CommandSender
    val isPlayer: Boolean
    val isConsole: Boolean get() = !isPlayer
    val path: List<String>

    @BrigadiKtDSL
    fun CommandSender.hasPathPermission(logResult: Boolean = false): Boolean

    @BrigadiKtDSL
    fun CommandSender.hasPathPermission(suffix: String, logResult: Boolean = false): Boolean

}

abstract class CommandContext(
    val raw: CommandContext<CommandSourceStack>,
    val cachedArguments: MutableMap<String, Any>,
    final override val path: List<String>,
): CommandAccess {

    final override val sender = raw.source.sender
    final override val isPlayer = sender is Player

    @BrigadiKtDSL
    operator fun <T : Any, R : Any> get(argumentReference: ArgumentReference<T, R>): R {
        return cachedArguments.getOrPut(argumentReference.name) {
            argumentReference.resolve(this)
        }.forceCastOrNull<R>() ?: throw IllegalArgumentException("Argument ${argumentReference.name} is not of type ${argumentReference::class.simpleName}")
    }

    @BrigadiKtDSL
    operator fun <T : Any, R : Any> ArgumentReference<T, R>.invoke(): R =
        this@CommandContext[this]

    // state modification
    @BrigadiKtDSL abstract fun state(state: Int, process: () -> Unit = { })
    @BrigadiKtDSL fun state(state: Int, message: ComponentLike) = state(state) { reply(message) }
    @BrigadiKtDSL fun state(state: Int, @StyledString message: String) = state(state, message.asStyledComponent)

    @BrigadiKtDSL fun fail(process: () -> Unit = { }) = state(0, process)
    @BrigadiKtDSL fun fail(message: ComponentLike) = state(0, message)
    @BrigadiKtDSL fun fail(@StyledString message: String) = state(0, message)

    @BrigadiKtDSL fun success(process: () -> Unit = { }) = state(Command.SINGLE_SUCCESS, process)
    @BrigadiKtDSL fun success(message: ComponentLike) = state(Command.SINGLE_SUCCESS, message)
    @BrigadiKtDSL fun success(@StyledString message: String) = state(Command.SINGLE_SUCCESS, message)

    @BrigadiKtDSL
    fun reply(@StyledString message: String) {
        sender.sendMessage(message.asStyledComponent)
    }

    @BrigadiKtDSL
    fun reply(component: ComponentLike) {
        sender.sendMessage(component)
    }

    override fun CommandSender.hasPathPermission(logResult: Boolean): Boolean =
        hasPermission(path.joinToString("."))
            .also { if (logResult) this@CommandContext.getItsLogger().info("sender ${this.name} ${it.switch("has", "has not")} permission '${path.joinToString(".")}'") }

    override fun CommandSender.hasPathPermission(suffix: String, logResult: Boolean): Boolean =
        hasPermission((path + suffix).joinToString("."))
            .also { if (logResult) this@CommandContext.getItsLogger().info("sender ${this.name} ${it.switch("has", "has not")} permission '${path.joinToString(".")}'") }

}

abstract class RequirementContext(
    val raw: CommandSourceStack,
    final override val path: List<String>,
) : CommandAccess {

    final override val sender = raw.sender
    final override val isPlayer = sender is Player

    override fun CommandSender.hasPathPermission(logResult: Boolean): Boolean =
        hasPermission(path.joinToString("."))
            .also { if (logResult) this@RequirementContext.getItsLogger().info("sender ${this.name} ${it.switch("has", "has not")} permission '${path.joinToString(".")}'") }

    override fun CommandSender.hasPathPermission(suffix: String, logResult: Boolean): Boolean =
        hasPermission((path + suffix).joinToString("."))
            .also { if (logResult) this@RequirementContext.getItsLogger().info("sender ${this.name} ${it.switch("has", "has not")} permission '${path.joinToString(".")}'") }

}