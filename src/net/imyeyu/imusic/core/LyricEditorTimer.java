package net.imyeyu.imusic.core;

import java.util.Timer;
import java.util.TimerTask;

import com.tulskiy.musique.audio.player.Player;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 歌词编辑器计时器
 * 
 * @author Yeyu
 *
 */
public class LyricEditorTimer extends Service<Number> {
	
	private Timer timer;
	private Player player;
	
	public LyricEditorTimer(Player player) {
		this.player = player;
	}

	protected Task<Number> createTask() {
		return new Task<Number>() {
			protected Number call() throws Exception {
				Thread.currentThread().setName("Lyric Editor Timer");
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {
						if (!player.isPaused()) {
							updateMessage("PLAYING");
							updateValue(player.getCurrentMillis());
						} else {
							updateMessage("PAUSING");
						}
					}
				}, 0, 100);
				return null;
			}
		};
	}
	
	public void shutdown() {
		timer.cancel();
		timer.purge();
	}
}