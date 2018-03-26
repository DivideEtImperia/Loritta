package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

object AminoConverterModule {
	fun convertToImage(event: MessageReceivedEvent) {
		for (attachments in event.message.attachments) {
			if (attachments.fileName.endsWith(".Amino") || attachments.fileName == "Amino") {
				val imageUrl = URL(attachments.url)
				val connection = imageUrl.openConnection() as HttpURLConnection
				connection.setRequestProperty("User-Agent",
						Constants.USER_AGENT)

				val byteArray = IOUtils.toByteArray(connection.inputStream)
				// Nós não conseguimos detectar se a imagem é uma GIF a partir do content type...
				val isGif = byteArray.let {
					val byte0 = it[0].toInt()
					val byte1 = it[1].toInt()
					val byte2 = it[2].toInt()
					val byte3 = it[3].toInt()
					val byte4 = it[4].toInt()
					val byte5 = it[5].toInt()

					byte0 == 0x47 && byte1 == 0x49 && byte2 == 0x46 && byte3 == 0x38 && byte4 == 0x39 && byte5 == 0x61 // GIF89a
				}

				val extension = if (isGif) "gif" else "png"

				event.textChannel.sendFile(byteArray.inputStream(), "amino.$extension", MessageBuilder().append("(Por " + event.member.asMention + ") **Link para o \".Amino\":** " + attachments.url).build()).queue()
				event.message.delete().queue()
			}
		}
	}
}