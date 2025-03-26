@file:Suppress("UnstableApiUsage")

package dev.fruxz.brigadikt

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.fruxz.ascend.extension.forceCastOrNull
import dev.fruxz.ascend.extension.isNotNull
import dev.fruxz.ascend.extension.logging.getItsLogger
import dev.fruxz.ascend.extension.switch
import dev.fruxz.ascend.extension.tryOrNull
import dev.fruxz.brigadikt.structure.ArgumentProvider
import dev.fruxz.stacked.StackedBuilder
import dev.fruxz.stacked.extension.api.StyledString
import dev.fruxz.stacked.extension.asStyledComponent
import dev.fruxz.stacked.extension.toStackedBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface CommandAccess {

    val sender: CommandSender
    val executor: Entity?
    val audience: CommandSender

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
    val replyRenderer: ReplyChatRenderer?,
    final override val path: List<String>,
): CommandAccess {

    final override val sender = raw.source.sender
    final override val executor = raw.source.executor
    final override val audience = executor ?: sender
    final override val isPlayer = audience is Player

    @BrigadiKtDSL
    operator fun <T : Any> get(argument: ArgumentProvider<*, T>): T =
        argument.resolve(this)

    @BrigadiKtDSL
    operator fun <T : Any> ArgumentProvider<*, T>.invoke(): T =
        this@CommandContext[this]

    /**
     * Useful for checking optional arguments presence
     */
    @BrigadiKtDSL
    val ArgumentProvider<*, *>.isPresent: Boolean get() = tryOrNull { this.resolve(this@CommandContext) }.isNotNull // TODO maybe improve it? Copilot suggested this code but...

    @BrigadiKtDSL
    inline fun <T : Any> ArgumentProvider<*, T>.isPresent(block: (argument: T) -> Unit) {
        if (isPresent) block(this())
    }

    @BrigadiKtDSL
    inline fun <T : Any> ArgumentProvider<*, T>.isNotPresent(block: () -> Unit) {
        if (!isPresent) block()
    }

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
    fun reply(component: ComponentLike, sound: Sound? = null) {
        val message = replyRenderer?.render(sender, executor, component, audience) ?: component

        audience.sendMessage(message.asComponent())
        if (sound != null) audience.playSound(sound, Sound.Emitter.self())
    }

    @BrigadiKtDSL
    fun reply(@StyledString message: String, sound: Sound? = null) =
        this.reply(component = message.asStyledComponent, sound = sound)

    @BrigadiKtDSL
    fun reply(sound: Sound? = null, messageBuilder: StackedBuilder.() -> Unit) =
        this.reply(Component.empty().toStackedBuilder().apply(messageBuilder), sound)

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
    final override val executor = raw.executor
    final override val audience = executor ?: sender
    final override val isPlayer = audience is Player

    override fun CommandSender.hasPathPermission(logResult: Boolean): Boolean =
        hasPermission(path.joinToString("."))
            .also { if (logResult) this@RequirementContext.getItsLogger().info("sender ${this.name} ${it.switch("has", "has not")} permission '${path.joinToString(".")}'") }

    override fun CommandSender.hasPathPermission(suffix: String, logResult: Boolean): Boolean =
        hasPermission((path + suffix).joinToString("."))
            .also { if (logResult) this@RequirementContext.getItsLogger().info("sender ${this.name} ${it.switch("has", "has not")} permission '${path.joinToString(".")}'") }

}