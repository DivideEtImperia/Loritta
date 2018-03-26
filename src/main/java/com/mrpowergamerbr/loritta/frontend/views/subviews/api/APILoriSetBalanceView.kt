package com.mrpowergamerbr.loritta.frontend.views.subviews.api

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.jooby.Request
import org.jooby.Response

class APILoriSetBalanceView : NoVarsRequireAuthView() {
	override fun handleRender(req: Request, res: Response, path: String): Boolean {
		return path.matches(Regex("^/api/v1/economy/set-balance"))
	}

	override fun renderProtected(req: Request, res: Response, path: String): String {
		val json = JsonObject()

		val body = JSON_PARSER.parse(req.body().value()).obj

		val userId = body["userId"].string
		val quantity = body["quantity"].double
		val lorittaProfile = loritta.getLorittaProfileForUser(userId)

		lorittaProfile.dreams = quantity
		loritta save lorittaProfile
		json["balance"] = lorittaProfile.dreams
		return json.toString()
	}
}