package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.getOrCreateWebhook
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage

class SimsimiCommand : AbstractCommand("simsimi", category = CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String = locale["SIMSIMI_DESCRIPTION"]

	override fun getExample(): List<String> = listOf("Como vai você?")

	override fun hasCommandFeedback(): Boolean = false

	var currentProxy: Pair<String, Int>? = null

	override fun run(context: CommandContext, locale: BaseLocale) {
		val webhook = getOrCreateWebhook(context.event.textChannel, "Simsimi")
		context.sendMessage(webhook, DiscordMessage.builder()
				.username(context.locale["SIMSIMI_NAME"])
				.content(context.getAsMention(true) + "`+simsimi` foi removido devido a limitações da API do Simsimi, desculpe pela inconveniência. / `+simsimi` was removed due to Simsimi's API limitations, sorry for the inconvenience. :(\n\nVocê pode usar como alternativa o `+cleverbot` (para um chat bot mais sério) ou o `+gabriela` (para algo igual ao SimSimi)")
				.avatarUrl("https://loritta.website/assets/img/simsimi_face.png?v=3")
				.build())
		return
	}
}