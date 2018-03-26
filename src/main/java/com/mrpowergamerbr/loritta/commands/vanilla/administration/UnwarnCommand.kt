package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission

class UnwarnCommand : AbstractCommand("unwarn", listOf("desavisar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UNWARN_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = LorittaUtils.getUserFromContext(context, 0)

			if (user == null) {
				context.reply(
						LoriReply(
								locale["BAN_UserDoesntExist"],
								Constants.ERROR
						)
				)
				return
			}

			val member = context.guild.getMember(user)

			if (member != null) {
				if (!context.guild.selfMember.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_RoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}

				if (!context.handle.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_PunisherRoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}
			}

			val userData = context.config.getUserData(user.id)

			if (userData.warns.isEmpty()) {
				context.reply(
						LoriReply(
								locale["WARN_DoesntHaveWarns"],
								Constants.ERROR
						)
				)
				return
			}

			userData.warns.removeAt(userData.warns.size - 1)

			loritta save context.config

			context.reply(
					LoriReply(
							locale["WARN_WarnRemoved"],
							"\uD83C\uDF89"
					)
			)
		} else {
			this.explain(context);
		}
	}
}