package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonArray
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.profile.DefaultProfileCreator
import com.mrpowergamerbr.loritta.utils.profile.MSNProfileCreator
import com.mrpowergamerbr.loritta.utils.profile.NostalgiaProfileCreator
import com.mrpowergamerbr.loritta.utils.profile.OrkutProfileCreator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.net.SocketTimeoutException
import javax.imageio.ImageIO

class PerfilCommand : AbstractCommand("perfil", listOf("profile"), CommandCategory.SOCIAL) {
	companion object {
		var ID_ARRAY: JsonArray? = null
		var lastQuery = 0L
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["PERFIL_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var userProfile = context.lorittaUser.profile

		val contextUser = LorittaUtils.getUserFromContext(context, 0)
		val user = if (contextUser != null) contextUser else context.userHandle

		val callback: (LorittaProfile) -> (Unit) = callback@ {
			if (userProfile.isBanned) {
				context.reply(
						LoriReply(
								"${contextUser.asMention} está **banido**",
								"\uD83D\uDE45"
						),
						LoriReply(
								"**Motivo:** `${userProfile.banReason}`",
								"✍"
						)
				)
				return@callback
			}
			// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
			var member = lorittaShards.getMutualGuilds(user).firstOrNull()?.getMember(user)

			try {
				// biscord bots
				if (System.currentTimeMillis() - lastQuery > 60000) {
					val discordBotsResponse = HttpRequest.get("https://discordbots.org/api/bots/${Loritta.config.clientId}/votes?onlyids=1")
							.authorization(Loritta.config.discordBotsOrgKey)
							.body()

					lastQuery = System.currentTimeMillis()
					ID_ARRAY = JSON_PARSER.parse(discordBotsResponse).array
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}


			var upvotedOnDiscordBots = if (ID_ARRAY != null) {
				ID_ARRAY!!.any { it.string == user.id }
			} else {
				false
			}

			val lorittaGuild = lorittaShards.getGuildById("297732013006389252")
			var hasNotifyMeRole = if (lorittaGuild != null) {
				if (lorittaGuild.isMember(user)) {
					val member = lorittaGuild.getMember(user)
					val role = lorittaGuild.getRoleById("334734175531696128")
					member.roles.contains(role)
				} else {
					false
				}
			} else {
				false
			}
			var usesPocketDreamsRichPresence = if (member != null) {
				val game = member.game
				if (game != null && game.isRich) {
					game.asRichPresence().applicationId == "415617983411388428"
				} else {
					false
				}
			} else {
				false
			}
			val pocketDreamsGuild = lorittaShards.getGuildById("320248230917046282")
			var isPocketDreamsStaff = if (pocketDreamsGuild != null) {
				if (pocketDreamsGuild.isMember(user)) {
					val member = pocketDreamsGuild.getMember(user)
					val role = pocketDreamsGuild.getRoleById("332650495522897920")
					member.roles.contains(role)
				} else {
					false
				}
			} else {
				false
			}

			val badges = mutableListOf<BufferedImage>()
			if (user.patreon || user.id == Loritta.config.ownerId) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush.png"))
			if (user.supervisor) badges += ImageIO.read(File(Loritta.ASSETS + "supervisor.png"))
			if (isPocketDreamsStaff) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_staff.png"))
			if (user.support) badges += ImageIO.read(File(Loritta.ASSETS + "support.png"))
			if (user.donator) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush2.png"))
			if (user.artist) badges += ImageIO.read(File(Loritta.ASSETS + "artist_badge.png"))
			if (hasNotifyMeRole) badges += ImageIO.read(File(Loritta.ASSETS + "notify_me.png"))
			if (usesPocketDreamsRichPresence) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_rp.png"))
			if (user.id == Loritta.config.clientId) badges += ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
			if (user.isBot) badges += ImageIO.read(File(Loritta.ASSETS + "robot_badge.png"))
			if (upvotedOnDiscordBots) badges += ImageIO.read(File(Loritta.ASSETS + "upvoted_badge.png"))

			val file = File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")

			val polluxDocument: Document by lazy {
				Jsoup.connect("http://www.pollux.fun/profile/${userProfile.userId}").get()
			}

			var aboutMe: String? = null

			if (userProfile.userId == Loritta.config.clientId) {
				aboutMe = locale["PERFIL_LORITTA_DESCRIPTION"]
			}

			if (userProfile.userId == "390927821997998081") {
				aboutMe = "Olá, eu me chamo Pantufa, sou da equipe do PocketDreams (e eu sou a melhor ajudante de lá! :3), e, é claro, a melhor amiga da Lori!"
			}

			if (userProfile.aboutMe != null && userProfile.aboutMe != "A Loritta é minha amiga!") {
				aboutMe = userProfile.aboutMe
			}

			if (aboutMe == null) {
				try {
					val polluxAboutMe = polluxDocument.getElementById("persotex")?.text()

					if (polluxAboutMe != "I have no personal text because I'm too lazy to set one.")
						aboutMe = polluxAboutMe
				} catch (e: SocketTimeoutException) {
					logger.error("Exception while pulling about me information from Pollux", e)
				}
			}

			if (aboutMe == null) {
				aboutMe = "A Loritta é a minha amiga! Sabia que você pode alterar este texto usando \"${context.config.commandPrefix}sobremim\"? :3"
			}

			val background = when {
				file.exists() -> ImageIO.read(File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
				else -> {
					// ===[ POLLUX ]===
					val polluxBackground = try {
						val background = polluxDocument.getElementsByClass("bgprofile").attr("src")

						if (background != "/backdrops/5zhr3HWlQB4OmyCBFyHbFuoIhxrZY6l6.png") { // Caso não seja o background padrão...
							val polluxOriginalBackground = LorittaUtils.downloadImage("https://www.pollux.fun$background")
									.getScaledInstance(971, 473, BufferedImage.SCALE_SMOOTH)
							val polluxBase = BufferedImage(800, 473, BufferedImage.SCALE_SMOOTH)
							val polluxGraphics = polluxBase.graphics
							polluxGraphics.drawImage(polluxOriginalBackground, -86, 0, null)
							polluxBase
						} else {
							null
						}
					} catch (e: Exception) {
						logger.error("Exception while pulling background information from Pollux", e)
						null
					}
					if (polluxBackground != null) {
						polluxBackground
					} else {
						ImageIO.read(File(Loritta.ASSETS + "default_background.png"))
					}
				} // Background padrão
			}

			val map = mapOf(
					"default" to DefaultProfileCreator::class.java,
					"nostalgia" to NostalgiaProfileCreator::class.java,
					"msn" to MSNProfileCreator::class.java,
					"orkut" to OrkutProfileCreator::class.java
			)

			var type = context.rawArgs.getOrNull(1) ?: context.rawArgs.getOrNull(0) ?: "default"
			if (!map.containsKey(type))
				type = "default"

			val creator = map[type]!!
			val profileCreator = creator.newInstance()
			val profile = profileCreator.create(
					context.userHandle,
					user,
					userProfile,
					context.guild,
					context.config,
					badges,
					locale,
					background,
					aboutMe,
					member
			)

			context.sendFile(profile, "lori_profile.png", "📝 **|** " + context.getAsMention(true) + context.locale["PEFIL_PROFILE"] + " ${if (type != "default") "*Atenção: Isto é um design em testes e futuramente será vendido na loja da Loritta!*" else ""}"); // E agora envie o arquivo
		}

		if (contextUser != null) {
			loritta.getLorittaProfileForUser(contextUser.id, callback)
			return
		}

		callback.invoke(userProfile)
	}
}