package net.imyeyu.imusic.bean;

import java.util.List;

/**
 * 歌词，包含以毫秒为单位的长整型时轴，原始时轴（00:00.00）及歌词文本列表
 * 
 * @author Yeyu
 *
 */
public class Lyric {

	private List<Long> times;
	private List<String> timesSource;
	private List<String> lrcs;

	public List<Long> getTimes() {
		return times;
	}

	public void setTimes(List<Long> times) {
		this.times = times;
	}

	public List<String> getLrcs() {
		return lrcs;
	}

	public void setLrcs(List<String> lrcs) {
		this.lrcs = lrcs;
	}

	public List<String> getTimesSource() {
		return timesSource;
	}

	public void setTimesSource(List<String> timesSource) {
		this.timesSource = timesSource;
	}
}