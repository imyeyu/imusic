package net.imyeyu.imusic.ctrl;

import javafx.scene.paint.Color;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.core.Spectrum;
import net.imyeyu.imusic.view.ViewSpecColor;
import net.imyeyu.util.Config;

public class SpecColor extends ViewSpecColor {
	
	private Config config = Entrance.getConfig();
	
	private boolean isNotCustom = false;

	public SpecColor(Spectrum spectrum, double x, double y) {
		isNotCustom = getPreset().getSelectionModel().getSelectedIndex() != 1; // 非自定义
		getBg().setDisable(isNotCustom);
		getSpecTop().setDisable(isNotCustom);
		getSpecMiddle().setDisable(isNotCustom);
		getSpecBottom().setDisable(isNotCustom);
		getPeak().setDisable(isNotCustom);
		
		getStage().setX(x);
		getStage().setY(y);
		getStage().show();
		
		// 预设
		getPreset().valueProperty().addListener((tmp, o, preset) -> {
			getBg().setValue(preset.getBg());
			getSpecTop().setValue(preset.getSpecTop());
			getSpecMiddle().setValue(preset.getSpecMiddle());
			getSpecBottom().setValue(preset.getSpecBottom());
			getPeak().setValue(preset.getPeak());
			
			isNotCustom = getPreset().getSelectionModel().getSelectedIndex() != 1;
			getBg().setDisable(isNotCustom);
			getSpecTop().setDisable(isNotCustom);
			getSpecMiddle().setDisable(isNotCustom);
			getSpecBottom().setDisable(isNotCustom);
			getPeak().setDisable(isNotCustom);
		});
		// 频谱 - 背景
		getBg().valueProperty().addListener((tmp, o, color) -> {
			if (color != null) {
				spectrum.setBgColor(Integer.parseInt(toHexString(color), 16));
			}
		});
		// 频谱 - 顶部
		getSpecTop().valueProperty().addListener((tmp, o, color) -> {
			if (color != null) {
				spectrum.setSpecTop(Integer.parseInt(toHexString(color), 16));
			}
		});
		// 频谱 - 中间
		getSpecMiddle().valueProperty().addListener((tmp, o, color) -> {
			if (color != null) {
				spectrum.setSpecMiddle(Integer.parseInt(toHexString(color), 16));
			}
		});
		// 频谱 - 底部
		getSpecBottom().valueProperty().addListener((tmp, o, color) -> {
			if (color != null) {
				spectrum.setSpecBottom(Integer.parseInt(toHexString(color), 16));
			}
		});
		// 频谱 - 顶块
		getPeak().valueProperty().addListener((tmp, o, color) -> {
			if (color != null) {
				spectrum.setPeak(Integer.parseInt(toHexString(color), 16));
			}
		});
		
		// 关闭
		getClose().setOnAction(event -> {
			int seleceId = getPreset().getSelectionModel().getSelectedIndex();
			config.put("specID", seleceId);
			if (seleceId == 1) { // 自定义预设
				config.put("specBg", toHexString(getBg().getValue()));
				config.put("specTop", toHexString(getSpecTop().getValue()));
				config.put("specMiddle", toHexString(getSpecMiddle().getValue()));
				config.put("specBottom", toHexString(getSpecBottom().getValue()));
				config.put("specPeak", toHexString(getPeak().getValue()));
				Entrance.setConfig(config);
			}
			getStage().close();
			close();
		});
	}
	
	private String format(double val) {
	    String in = Integer.toHexString((int) Math.round(val * 255));
	    return in.length() == 1 ? "0" + in : in;
	}

	/**
	 * JavaFX Color 对象转十六进制 RGB<br />
	 * AWT Graphics 需要十六进制整型<br />
	 * Graphics 支持透明度（背景绘制），但 GradientPaint 不支持（渐变绘制），所有处理忽略透明度
	 * 
	 * @param value JavaFX Color 对象 
	 * @return 不含 # 号十六进制字符串，如 FF7A9B
	 */
	private String toHexString(Color value) {
	    return (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue())).toUpperCase();
	}
}