# BrigadiKt

Welcome to BrigadiKt, a Kotlin library for the Brigadier API of Minecraft. This project is an in-dev attempt to make this API more accessible, dev-friendly and Kotlin-like.
Currently, this project is not really ready for production use, but if you want to help, feel free to contribute!

While the previous attempt, making BrigadiKt reality, focused on crossplattform compatibility, the new PaperMC/Paper brigadier API is now the main focus of this project.

## Schema

Currently, the proposed idea of how this API could look like, is something like this:

```kotlin
    branch("test") {
      format(formatpack) // defines e.g. prefix, idk. stuff like this for reply
      val name = createArgument<StringArgument>("test") // name should default to val name instead, or replaced by manual name input, only if wanted!
    
      require(alsoSubCommands = true) {
        sender.isPlayer && sender.hasSubPermission(".self") // automatically create command permissions
      }
      
      execute {
        if (isPlayer) return; // no return value should be required
        sender.sendMessage(name()) // fun UnresolvedArgument.invoke(...) should return the arg with context(CommandContext)
      }
    
      branch { // no name allowed, if argument is available inside
        val target = argument(ArgumentType.players(), "target") // name again optional
    
        execute {
          if (target.isOp) return fail() // fail is unit, so should be okay, also fail() set internal state = 0, while it defaults to 1
          if (demo.size > 1) return state(demo.size) // custom return state, like fail
          
          target.sendMiniMessage("wow!") // upcoming Stacked feature
          reply("wow!") // do the same, minimessage or component like
        }
      }
      
    }
```
