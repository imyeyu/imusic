package net.imyeyu.imusic.core;

import java.io.File;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.model.Track;
import com.tulskiy.musique.system.TrackIO;

import net.imyeyu.imusic.bean.Music;

/**
 * 播放核心
 * 
 * @author Yeyu
 *
 */
public class AudioPlayer extends Player {
	
	private Music music;
	private Track track;
	private Player player;
	private boolean isSeeking = false;

	public AudioPlayer() {
		this.player = new Player();
	}
	
	public void play(File file) {
		AudioFileReader audioFileReader = TrackIO.getAudioFileReader(file.getName());
		track = audioFileReader.read(file);
		player.open(track);
	}
	
	public void pause() {
		player.pause();
	}
	
	public void seek(long sample) {
		player.seek(sample);
	}
	
	public void setVolume(double v) {
		player.getAudioOutput().setVolume((float) (v / 100));
	}
	
	public void setVolumeUp() {
		player.getAudioOutput().setVolume(player.getAudioOutput().getVolume(true) + 1);
	}
	
	public void setVolumeDown() {
		player.getAudioOutput().setVolume(player.getAudioOutput().getVolume(true) - 1);
	}
	
	public Player getPlayer() {
		return player;
	}

	public Track getTrack() {
		return track;
	}
	
	public boolean isSeeking() {
		return isSeeking;
	}

	public void setSeeking(boolean isSeeking) {
		this.isSeeking = isSeeking;
	}

	public Music getMusic() {
		return music;
	}

	public void setMusic(Music music) {
		this.music = music;
	}
}