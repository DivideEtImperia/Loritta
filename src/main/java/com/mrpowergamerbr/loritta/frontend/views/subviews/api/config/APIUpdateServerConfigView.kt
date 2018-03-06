package com.mrpowergamerbr.loritta.frontend.views.subviews.api.config

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.NoVarsView
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.types.ConfigPayloadType
import com.mrpowergamerbr.loritta.frontend.views.subviews.api.config.types.ServerListPayload
import com.mrpowergamerbr.loritta.userdata.ServerListConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import net.dv8tion.jda.core.Permission
import org.jooby.Request
import org.jooby.Response
import java.util.*

class APIUpdateServerConfigView : NoVarsView() {
	override fun handleRender(req: Request, res: Response): Boolean {
		return req.path().matches(Regex("^/api/v1/config/update-server-config"))
	}

	override fun render(req: Request, res: Response): String {
		var userIdentification: TemmieDiscordAuth.UserIdentification? = null
		if (req.session().isSet("discordAuth")) {
			val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
			try {
				discordAuth.isReady(true)
				userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
			} catch (e: Exception) {
				req.session().unset("discordAuth")
			}
		}

		if (userIdentification == null) { // Unauthozied (Discord)
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		if (!req.param("guildId").isSet) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val guildId = req.param("guildId").value() // get guild id
		val serverConfig = loritta.getServerConfigForGuild(guildId) // get server config for guild
		val server = lorittaShards.getGuildById(guildId)
		if (server == null) {
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNKNOWN_GUILD
			return payload.toString()
		}

		val id = userIdentification.id
		val member = server.getMemberById(id)

		if (member == null) { // not in server
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.NOT_IN_GUILD
			return payload.toString()
		}

		val lorittaUser = GuildLorittaUser(member, serverConfig, loritta.getLorittaProfileForUser(id))
		var canAccessDashboardViaPermission = lorittaUser.hasPermission(LorittaPermission.ALLOW_ACCESS_TO_DASHBOARD)

		var canOpen = id == Loritta.config.ownerId || canAccessDashboardViaPermission || member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR)

		if (!canOpen) { // not authorized (perm side)
			val payload = JsonObject()
			payload["api:code"] = LoriWebCodes.UNAUTHORIZED
			return payload.toString()
		}

		val body = JSON_PARSER.parse(req.body().value()).obj // content payload
		val type = body["type"].string
		val config = body["config"].obj

		val payload = JsonObject()
		payload["api:code"] = LoriWebCodes.SUCCESS

		val payloadHandlers = mapOf(
				"server_list" to ServerListPayload::class.java
		)

		val payloadHandlerClass = payloadHandlers[type]

		if (payloadHandlerClass != null) {
			val payloadHandler = payloadHandlerClass.newInstance()
			payloadHandler.process(config, serverConfig, server)
			loritta save serverConfig
		} else {
			payload["api:code"] = LoriWebCodes.MISSING_PAYLOAD_HANDLER
			payload["missing_payload_handler"] = type
		}

		return payload.toString()
	}
}