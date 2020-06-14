package net.imyeyu.imusic.util;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import net.imyeyu.imusic.bean.Lyric;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.vo.HTTPInfo;

/**
 * 歌词加载器，从酷狗 API 或本地文件获取
 * 
 * @author Yeyu
 *
 */
public class LyricLoader extends Service<Lyric> {

	private static final String API_GET_SONG = "http://mobilecdn.kugou.com/api/v3/search/song";
	private static final String API_GET_LRC = "http://m.kugou.com/app/i/krc.php";
	
	private File file;
	private Lyric lyric;
	private String searchKey, timeLength;
	
	/**
	 * 从指定文件加载
	 * 
	 * @param file
	 */
	public LyricLoader(File file) {
		this.file = file;
	}

	/**
	 * 从酷狗获取
	 * 
	 * @param searchKey  搜索名，歌曲名 - 歌手
	 * @param timeLength 音频长度
	 */
	public LyricLoader(String searchKey, String timeLength) {
		this.searchKey = searchKey;
		this.timeLength = timeLength;
	}
	
	protected Task<Lyric> createTask() {
		return new Task<Lyric>() {
			protected Lyric call() throws Exception {
				Thread.currentThread().setName("Lyric Loader");
				
				String lrcsString;
				if (file != null) {      // 从指定文件获取歌词
					lrcsString = YeyuUtils.file().fileToString(file, "UTF-8");
					updateTitle("LOCAL");
				} else {                 // 从酷狗获取
					file = new File("lrcs/" + searchKey + ".lrc");
					if (file.exists()) { // 是否存在本地缓存
						lrcsString = YeyuUtils.file().fileToString(file, "UTF-8");
						updateTitle("LOCAL");
					} else {
						lrcsString = getLRCFromAPI(searchKey, timeLength);
						updateTitle("API");
					}
				}
				lyric = stringToLyric(lrcsString);
				return lyric;
			}

			private String getLRCFromAPI(String title, String timelength) throws Exception {
				// 请求数据
				HTTPInfo http = new HTTPInfo();
				http.setUrl(API_GET_SONG);
				http.setParam("format=json&keyword=" + URLEncoder.encode(title, "UTF-8") + "&page=1&pagesize=30");
				http.setCookie("kg_mid=38b475d7f7b7ff8c2cbc5d26d6b4eb92; kg_dfid=0PrIN23TsR430YtMHB12i9c8; kg_dfid_collect=d41d8cd98f00b204e9800998ecf8427e; Hm_lvt_aedee6983d4cfc62f509129360d6bb3d=1569245059,1569245104");
				http.setHost("www.kugou.com");
				http.setCharset("UTF-8");
				// 获取音乐 ID 和 Hash
				String response = YeyuUtils.network().sendGet(http);
				JsonElement jp = new JsonParser().parse(response);
				JsonObject data = (JsonObject) jp.getAsJsonObject().get("data").getAsJsonObject().get("info").getAsJsonArray().get(0);
				String hash = data.get("hash").getAsString();
				// 获取歌词
				response = YeyuUtils.network().sendGet(API_GET_LRC, "cmd=100&keyword=" + title + "&hash=" + hash + "&timelength=" + timelength + "&d=" + Math.random());
				// 保存本地
				file = new File("lrcs/" + title + ".lrc");
				YeyuUtils.file().stringToFile(file, response);
				return response;
			}
		};
	}
	
	/**
	 * 保存歌词（调整时轴偏移后的）
	 * 
	 */
	public void saveLyric() {
		StringBuffer sb = new StringBuffer();
		long tick;
		String ms, lrc;
		List<Long> times = lyric.getTimes();
		List<String> lrcs = lyric.getLrcs();
		for (int i = 0; i < times.size(); i++) {
			tick = times.get(i);
			lrc = lrcs.get(i);
			tick = tick < 0 ? 100 : tick;
			ms = String.valueOf(tick);
			sb.append(
				"[" + String.format("%02d", (tick / 1000) / 60) +
				":" + String.format("%02d", (tick / 1000) % 60) +
				"." + ms.substring(ms.length() - 3, ms.length() - 1) + "]" + lrc
			);
		}
		YeyuUtils.file().stringToFile(file, sb.toString());
	}
	
	/**
	 * 解析歌词文本
	 * 
	 * @param lrcsString
	 * @return
	 */
	public Lyric stringToLyric(String lrcsString) {
		// 准备数据
		String[] data = lrcsString.replaceAll("\\[", "\r\n[").split("\r\n|[\r\n]");
		List<Long> times = new ArrayList<>();
		List<String> timesSource = new ArrayList<>();
		List<String> lrcs = new ArrayList<>();
		// 匹配时轴
		Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}\\.\\d{2})\\]");
		Matcher matcher;
		String time, content = "";
		int groupCount;
		long currentTime = 0;
		String[] lrc;
		for (int i = 0, l = data.length; i < l; i++) {
			matcher = pattern.matcher(data[i]);
			if (matcher.find()) {
				groupCount = matcher.groupCount();
				for (int j = 0; j <= groupCount; j++) {
					time = matcher.group(j);
					if (j == 1) {
						// 时轴
						timesSource.add(time);
						currentTime = timeToLong(time);
					}
				}
				lrc = pattern.split(data[i]);
				for (int j = 0; j < lrc.length; j++) {
					if (j == lrc.length - 1) {
						// 歌词
						content = lrc[j];
					}
				}
				times.add(currentTime);
				lrcs.add(content);
			}
		}
		Lyric lyric = new Lyric();
		lyric.setTimes(times);
		lyric.setTimesSource(timesSource);
		lyric.setLrcs(lrcs);
		return lyric;
	}

	/**
	 * 解析时轴，精度：10 毫秒
	 * 
	 * @param time 时轴文本
	 * @return
	 */
	private long timeToLong(String time) {
		String[] s = time.split(":");
		int min = Integer.parseInt(s[0]);
		String[] ss = s[1].split("\\.");
		int sec = Integer.parseInt(ss[0]);
		int ms = Integer.parseInt(ss[1]);
		return min * 60 * 1000 + sec * 1000 + ms * 10;
	}
}