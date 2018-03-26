package com.mrpowergamerbr.loritta.utils.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
/**
 * @param player The audio player this scheduler uses
 */
class TrackScheduler(val guild: Guild, val player: AudioPlayer) : AudioEventAdapter() {
	val queue: BlockingQueue<AudioTrackWrapper>
	var currentTrack: AudioTrackWrapper? = null

	init {
		this.queue = LinkedBlockingQueue()
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	fun queue(track: AudioTrackWrapper, config: ServerConfig) {
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.

		if (player.playingTrack != null) {
			if (currentTrack != null) {
				if (currentTrack!!.isAutoPlay) {
					// Quem liga para músicas do autoplay? Cancele ela agora!
					player.stopTrack()
				}
			}
		}
		if (!player.startTrack(track.track, true)) {
			queue.offer(track)
		} else {
			currentTrack = track

			if (config.musicConfig.logToChannel) {
				val textChannel = guild.getTextChannelById(config.musicConfig.channelId)

				if (textChannel.canTalk() && guild.selfMember.hasPermission(textChannel, Permission.MESSAGE_EMBED_LINKS)) {
					loritta.executor.execute {
						var seconds = 0

						while (true) {
							if (seconds >= 5) {
								return@execute
							}

							if (!track.metadata.isEmpty()) {
								val embed = LorittaUtilsKotlin.createTrackInfoEmbed(guild, LorittaLauncher.loritta.getLocaleById(config.localeId), true)

								textChannel.sendMessage(embed).complete()
								return@execute
							}
							seconds++
							try {
								Thread.sleep(1000)
							} catch (e: InterruptedException) {
								e.printStackTrace()
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	fun nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		val audioTrackWrapper = queue.poll()
		player.startTrack(audioTrackWrapper?.track, false)
		this.currentTrack = audioTrackWrapper

		loritta.getServerConfigForGuild(guild.id) { serverConfig ->
			// this.player.volume = serverConfig.volume

			// Então quer dizer que nós iniciamos uma música vazia?
			// Okay então, vamos pegar nossas próprias coisas
			if (audioTrackWrapper == null) {
				// Ok, Audio Track é null!
				LorittaUtilsKotlin.startRandomSong(guild, serverConfig)
			}
		}
	}

	override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (endReason!!.mayStartNext) {
			nextTrack()
		}
	}
}