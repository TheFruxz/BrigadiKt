package dev.fruxz.brigadikt.annotation

/**
 * Because BrigadiKt is directly used inside the native Brigadier API environment,
 * this annotation is used to mark DSL functions, which are provided by BrigadiKt.
 * With this annotation, it is easy to identify the path you want to use.
 * @author Fruxz
 * @since 2023.3
 */
@DslMarker
@MustBeDocumented
annotation class BrigadiktDSL