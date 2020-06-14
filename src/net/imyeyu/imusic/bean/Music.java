package net.imyeyu.imusic.bean;

import javafx.scene.image.Image;

public class Music {

	private String fileName; // 文件名
	private String path;     // 文件路径
	private String title;    // 标题，歌曲名 - 演唱
	private int time;        // 时长（秒）

	private String name;     // 歌曲名
	private String singer;   // 演唱
	private String album;    // 专辑
	private String year;     // 出版年份
	private long size;       // 文件大小
	private String encode;   // 编码类型
	private Image cover;     // 封面

	public String getFileName() {
		return fileName;
	}

	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}

	public int getTime() {
		return time;
	}

	public String getName() {
		return name;
	}

	public String getSinger() {
		return singer;
	}

	public String getAlbum() {
		return album;
	}

	public String getYear() {
		return year;
	}

	public long getSize() {
		return size;
	}

	public String getEncode() {
		return encode;
	}

	public Image getCover() {
		return cover;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public void setCover(Image cover) {
		this.cover = cover;
	}
}