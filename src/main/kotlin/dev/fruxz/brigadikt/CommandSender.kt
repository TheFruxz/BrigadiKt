@file:OptIn(ExperimentalContracts::class)

package dev.fruxz.brigadikt

import dev.fruxz.ascend.extension.objects.takeIfInstance
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun CommandSender.asPlayerOrNull(): Player? {
    contract {
        returnsNotNull() implies (this@asPlayerOrNull is Player)
    }

    return this.takeIfInstance<Player>()
}

fun CommandSender.asPlayer(): Player {
    contract {
        returns() implies (this@asPlayer is Player)
    }

    return this as Player
}

fun CommandSender.isPlayer(): Boolean {
    contract {
        returns(true) implies (this@isPlayer is Player)
    }

    return this is Player
}