package com.mrpowergamerbr.loritta.utils.misc

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import okhttp3.*

object PomfUtils {
	const val POMF_URL = "http://pomf.cat/upload.php"

	fun uploadFile(array: ByteArray): String? {
		return uploadFile(POMF_URL, "mirror.png", array)
	}

	fun uploadFile(array: ByteArray, fileName: String): String? {
		return uploadFile(POMF_URL, fileName, array)
	}

	fun uploadFile(url: String, fileName: String, array: ByteArray): String? {
		val client = OkHttpClient()
		val formBody = MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("files[]", fileName,
						RequestBody.create(MediaType.parse("image/png"), array))
				.build()
		val request = Request.Builder().url(url)
				.post(formBody).build()
		val response = client.newCall(request).execute()

		val _response = response.body()!!.string()

		val json = JSON_PARSER.parse(_response).obj

		if (json.has("files")) {
			return "http://a.pomf.cat/${json["files"].array[0]["url"].string}"
		} else {
			return null
		}
	}
}