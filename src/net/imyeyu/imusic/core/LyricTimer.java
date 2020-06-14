package net.imyeyu.imusic.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.tulskiy.musique.audio.player.Player;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.util.LyricLoader;

/**
 * 歌词滚动核心
 * 
 * @author Yeyu
 *
 */
public class LyricTimer extends Service<List<String>> {
	
	/**
	 * 调整等待，解决播放核心和滚动计时器精度差问题<br />
	 * 播放器的核心进度精度不足 100 毫秒，歌词精度 10 毫秒<br />
	 * 调整进度后歌词进度会立即跳至目标进度，而播放器核心进度还在停留，可能会被判为当前歌词已被播放<br />
	 * 最严重的后果是会在 100 毫秒内滚动歌词约 10 次，即歌词滚动已偏移错位。主要触发在进度往回调<br />
	 * 使用此参数等待播放核心进度调整再允许进行歌词播放判定
	 * 
	 */
	private int wait = -1;
	private int j = 0;         // 当前歌词下标
	private long nextTime = 0; // 下一句歌词时轴
	private File file;         // 切换歌词所选文件
	private Timer timer;
	private Player player;
	private String searchKey, timeLength;
	private List<Long> times;
	private LyricLoader loader;
	
	public LyricTimer(Player player, File file) {
		this.file = file;
		this.player = player;
	}
	
	public LyricTimer(Player player, String searchKey, String timeLength) {
		this.player = player;
		this.searchKey = searchKey;
		this.timeLength = timeLength;
	}

	protected Task<List<String>> createTask() {
		return new Task<List<String>>() {
			protected List<String> call() throws Exception {
				Thread.currentThread().setName("Lyric Timer");
				// 歌词加载器
				loader = file != null ? new LyricLoader(file) : new LyricLoader(searchKey, timeLength);
				loader.valueProperty().addListener((tmp, o, n) -> {
					if (n != null) {
						// 更新歌词到组件
						updateValue(n.getLrcs());
						// 时轴列表
						times = n.getTimes();
						if (times.listIterator().hasNext()) {
							timer = new Timer();
							nextTime = times.get(j); // 下一句歌词时轴
							// 高精度计时器
							wait = 0;
							timer.scheduleAtFixedRate(new TimerTask() {
								public void run() {
									if (!player.isPaused() && wait == -1) {
										// 当前播放时间大于下一句歌词时轴，滚动
										if (nextTime < player.getCurrentMillis()) {
											updateMessage(String.valueOf(j));
											if (j < times.size() - 1) {
												nextTime = times.get(++j); // 更新下一句时轴
											}
										}
									} else {
										wait = 20 < wait ? -1 : ++wait; // 200 毫秒后再允许进入滚动判定
									}
								}
							}, 0, 10);
						}
					}
				});
				loader.titleProperty().addListener((tmp, o, n) -> updateTitle(n));
				loader.exceptionProperty().addListener((tmp, o, e) -> {
					updateTitle("N/A");
					List<String> list = new ArrayList<String>();
					list.add(Entrance.getRb().def("lyricGetFail"));
					updateMessage(String.valueOf(0));
					updateValue(list);
				});
				loader.start();
				return null;
			}
		};
	}
	
	/**
	 * 重置遍历时轴下标，调整当前播放歌词<br />
	 * 通常在调整播放进度时执行
	 * 
	 * @param toTime 调整到的目标时间
	 */
	public void resetIndex(long toTime) {
		wait = 0;
		if (times != null) {
			for (int k = 0; k < times.size(); k++) {
				if (toTime < times.get(k)) {
					j = k - 1 < 0 ? 0 : k - 1;
					nextTime = times.get(j);
					break;
				}
			}
		}
	}
	
	/**
	 * 调整整体歌词时轴，加快或减慢滚动<br />
	 * 负值加快，正值减慢
	 * 
	 * @param offset 偏移量
	 */
	public void adjustTimes(long offset) {
		for (int i = 0; i < times.size(); i++) {
			times.set(i, times.get(i) + offset);
		}
	}
	
	public void slowX() {
		adjustTimes(1000);
	}
	
	public void slow() {
		adjustTimes(200);
	}
	
	public void save() {
		loader.saveLyric();
	}
	
	public void fast() {
		adjustTimes(-200);
	}
	
	public void fastX() {
		adjustTimes(-1000);
	}
	
	public void shutdown() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}
}