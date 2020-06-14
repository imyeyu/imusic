package net.imyeyu.imusic.bean;

import javafx.scene.paint.Color;

/**
 * 频谱着色方案
 * 
 * @author Yeyu
 *
 */
public class SpecPreset {

	private Color bg, specTop, specMiddle, specBottom, peak;
	private String name;

	public Color getBg() {
		return bg;
	}

	public Color getSpecTop() {
		return specTop;
	}

	public Color getSpecMiddle() {
		return specMiddle;
	}

	public Color getSpecBottom() {
		return specBottom;
	}

	public Color getPeak() {
		return peak;
	}

	public String getName() {
		return name;
	}

	public void setBg(Color bg) {
		this.bg = bg;
	}

	public void setSpecTop(Color specTop) {
		this.specTop = specTop;
	}

	public void setSpecMiddle(Color specMiddle) {
		this.specMiddle = specMiddle;
	}

	public void setSpecBottom(Color specBottom) {
		this.specBottom = specBottom;
	}

	public void setPeak(Color peak) {
		this.peak = peak;
	}

	public void setName(String name) {
		this.name = name;
	}
}