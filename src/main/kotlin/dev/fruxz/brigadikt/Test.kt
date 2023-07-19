package dev.fruxz.brigadikt

import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import java.nio.file.attribute.UserPrincipal

fun test() {

    LiteralArgumentBuilder.literal<UserPrincipal>("test")
        .then(
            argument<UserPrincipal?, Int?>("test", integer())
                .then(argument<UserPrincipal?, Int?>("test", integer())))

}