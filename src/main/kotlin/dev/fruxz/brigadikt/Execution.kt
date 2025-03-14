@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.fruxz.ascend.extension.forceCastOrNull
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

    @BrigadiKtDSL
    fun CommandSender.hasSubPermission(sub: String): Boolean

}

abstract class CommandContext(
    val raw: CommandContext<CommandSourceStack>,
    val cachedArguments: MutableMap<String, Any>
): CommandAccess {

    final override val sender = raw.source.sender
    override val isPlayer = sender is Player

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
    @BrigadiKtDSL fun fail(process: () -> Unit = { }) = state(0, process)
    @BrigadiKtDSL fun success(process: () -> Unit = { }) = state(Command.SINGLE_SUCCESS, process)

    @BrigadiKtDSL
    fun reply(@StyledString message: String) {
        sender.sendMessage(message.asStyledComponent)
    }

    @BrigadiKtDSL
    fun reply(component: ComponentLike) {
        sender.sendMessage(component)
    }

    override fun CommandSender.hasSubPermission(sub: String): Boolean {
        return hasPermission("brigadikt.$sub") // TODO proper permission generation
    }

}

abstract class RequirementContext(
    val raw: CommandSourceStack,
) : CommandAccess {

    final override val sender = raw.sender
    override val isPlayer = sender is Player

    override fun CommandSender.hasSubPermission(sub: String): Boolean {
        return hasPermission("brigadikt.$sub") // TODO proper permission generation
    }

}