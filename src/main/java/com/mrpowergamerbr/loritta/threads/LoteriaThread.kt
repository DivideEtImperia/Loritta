package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import java.io.File
import java.time.Instant

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class LoteriaThread : Thread("Loteria Thread") {
	companion object {
		var lastWinnerId: String? = null
		var lastWinnerPrize = 0
		var started: Long = System.currentTimeMillis()
		// user ID + locale ID
		var userIds = mutableListOf<Pair<String, String>>()
	}

	override fun run() {
		super.run()

		while (true) {
			try {
				val diff = System.currentTimeMillis() - started

				if (diff > 3600000) {
					handleWin()
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000);
		}
	}

	fun save() {
		val loteriaFile = File(Loritta.FOLDER, "loteria.json")

		val json = JsonObject()

		json["started"] = started
		json["lastWinnerId"] = lastWinnerId
		json["lastWinnerPrize"] = lastWinnerPrize
		json["userIds"] = Loritta.GSON.toJsonTree(userIds)

		loteriaFile.writeText(json.toString())
	}

	fun handleWin() {
		if (userIds.isEmpty()) {
			started = System.currentTimeMillis()
			save()
		} else {
			val winner = userIds[RANDOM.nextInt(userIds.size)]
			val winnerId = winner.first
			lastWinnerId = winnerId

			val money = userIds.size * 250
			lastWinnerPrize = money

			loritta.getLorittaProfileForUser(winnerId) { lorittaProfile ->
				lorittaProfile.dreams += money
				loritta save lorittaProfile
				userIds.clear()

				val user = lorittaShards.getUserById(lastWinnerId)

				if (user != null) {
					try {
						val embed = EmbedBuilder()
						embed.setThumbnail("attachment://loritta_money.png")
						embed.setColor(Constants.LORITTA_AQUA)
						embed.setTitle("\uD83C\uDF89 Parabéns!")
						embed.setDescription("Você ganhou **${lastWinnerPrize} Sonhos** na Loteria! \uD83E\uDD11")
						embed.setTimestamp(Instant.now())
						val message = MessageBuilder().setContent(" ").setEmbed(embed.build()).build()
						user.openPrivateChannel().complete().sendFile(File(Loritta.ASSETS, "loritta_money_discord.png"), "loritta_money.png", message).complete()
					} catch (e: Exception) {
					}
				}
				started = System.currentTimeMillis()
				save()
			}
		}
	}
}