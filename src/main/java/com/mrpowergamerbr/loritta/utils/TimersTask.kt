package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Timers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TimersTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		val timerJobs = mutableMapOf<Long, Job>()
	}

	override fun run() {
		try {
			logger.info("Criando timers para servidores...")

			val timers = transaction(Databases.loritta) {
				Timers.selectAll().map { Timer.wrapRow(it) }
			}

			for (timer in timers) {
				val guild = lorittaShards.getGuildById(timer.guildId) ?: continue
				val textChannel = guild.getTextChannelById(timer.channelId) ?: continue

				if (timerJobs[timer.id.value] != null)
					return

				timerJobs[timer.id.value] = GlobalScope.launch(loritta.coroutineDispatcher) {
					timer.prepareTimer()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao verificar timer threads" }
		}
	}
}