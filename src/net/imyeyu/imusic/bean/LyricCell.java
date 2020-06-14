package net.imyeyu.imusic.bean;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * 主要用于歌词编辑器的编辑列表单元
 * 
 * @author Yeyu
 *
 */
public class LyricCell {

	private SimpleStringProperty time = new SimpleStringProperty();
	private SimpleStringProperty lyric = new SimpleStringProperty();
	private SimpleBooleanProperty valid = new SimpleBooleanProperty(true);
	
	public LyricCell() {}
	
	public LyricCell(String time, String lyric) {
		this.time.set(time);
		this.lyric.set(lyric);;
	}

	public SimpleStringProperty getTimeProperty() {
		return time;
	}
	
	public SimpleStringProperty getLyricProperty() {
		return lyric;
	}
	
	public SimpleBooleanProperty getVaildProperty() {
		return valid;
	}

	public void setTime(String time) {
		this.time.set(time);
	}

	public void setLyric(String lyric) {
		this.lyric.set(lyric);
	}
	
	public void setVaild(boolean isVaild) {
		this.valid.set(isVaild);
	}
	
	public String getTime() {
		return time.get();
	}
	
	public String getLyric() {
		return lyric.get();
	}
	
	public boolean isVaild() {
		return valid.get();
	}
}