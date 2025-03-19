package dev.fruxz.brigadikt

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity

fun interface ReplyChatRenderer {

    fun render(
        sender: CommandSender, // sender of the command
        executor: Entity?, // registered executor of the command
        message: ComponentLike,
        viewer: Audience,
    ): ComponentLike

}