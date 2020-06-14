package net.imyeyu.imusic.core;

import com.tulskiy.musique.audio.player.Player;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 播放核心计时器
 * 
 * @author Yeyu
 *
 */
public class AudioTimer extends Service<Number> {

	private Player player;
	private boolean isShutdown = false;
	
	public AudioTimer(Player player) {
		this.player = player;
	}
	
	protected Task<Number> createTask() {
		return new Task<Number>() {
			protected Number call() throws Exception {
				Thread.currentThread().setName("Audio Timer");
				while (!isShutdown) {
					updateValue(player.getCurrentMillis());
					Thread.sleep(100);
				}
				return 0l;
			}
		};
	}
	
	public void shutdown() {
		this.isShutdown = true;
	}
}