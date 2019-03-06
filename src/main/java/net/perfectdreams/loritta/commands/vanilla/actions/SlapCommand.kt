package net.perfectdreams.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class SlapCommand : ActionCommand(arrayOf("kiss", "beijo", "beijar")) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.actions.slap.description"]
    }

    override fun getResponse(locale: BaseLocale, first: User, second: User): String {
        return if (second.id != Loritta.config.clientId) {
            locale["commands.actions.slap.response", first.asMention, second.asMention]
        } else {
            locale["commands.actions.slap.responseAntiIdiot", second.asMention, first.asMention]
        }
    }

    override fun getFolderName(): String {
        return "slap"
    }

    override fun getEmoji(): String {
        return "\uD83D\uDE40"
    }
}