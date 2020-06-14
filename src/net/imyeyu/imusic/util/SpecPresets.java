package net.imyeyu.imusic.util;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.SpecPreset;
import net.imyeyu.util.Config;
import net.imyeyu.util.ResourceBundleX;

/**
 * 频谱预设模块，由于 AWT GradientPaint 绘制不支持透明度，所有逻辑忽略透明度
 * 
 * @author Yeyu
 *
 */
public class SpecPresets {
	
	private static Config config = Entrance.getConfig();
	
	private static int[] bg = {
		0xF4F4F4,
		config.getInt("specBg", 16),
		0xF4F4F4,
		0xF4F4F4,
		0xF4F4F4,
		0xF4F4F4,
		0x211C0F
	};
	
	private static int[] specTop = {
		0xC8DDF2,
		config.getInt("specTop", 16),
		0x66CC33,
		0xFFFFFF,
		0x11C2EE,
		0x7A22EE,
		0xFFFF00
	};
	
	private static int[] specMiddle = {
		0xC8DDF2,
		config.getInt("specMiddle", 16),
		0x66CC33,
		0xFFD2DD,
		0x11C2EE,
		0x7A22EE,
		0xFFFF00
	};
	
	private static int[] specBottom = {
		0x486BB1,
		config.getInt("specBottom", 16),
		0x66CC33,
		0xFF7A9B,
		0x11C2EE,
		0x7A22EE,
		0xFFFF00
	};
	
	private static int[] peak = {
		0x486BB1,
		config.getInt("specPeak", 16),
		0xFF6600,
		0xFF7A9B,
		0xF709F7,
		0xFF6600,
		0xFF6600
	};
	
	/**
	 * 生成所有预设模块
	 * 
	 * @return
	 */
	public static SpecPreset[] get() {
		ResourceBundleX rb = Entrance.getRb();
		String[] names = {
			rb.def("spec.default"),
			rb.def("spec.custom"),
			rb.def("spec.green"),
			rb.def("spec.pink"),
			rb.def("spec.blue"),
			rb.def("spec.purple"),
			rb.def("spec.classic")
		};
		SpecPreset preset;
		List<SpecPreset> list = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			preset = new SpecPreset();
			preset.setName(names[i]);
			preset.setBg(new Color(
				((bg[i] >> 16) & 0xFF) / 255d,
				((bg[i] >> 8) & 0xFF) / 255d,
				(bg[i] & 0xFF) / 255d, 1
			));
			preset.setSpecTop(new Color(
				((specTop[i] >> 16) & 0xFF) / 255d,
				((specTop[i] >> 8) & 0xFF) / 255d,
				(specTop[i] & 0xFF) / 255d, 1
			));
			preset.setSpecMiddle(new Color(
				((specMiddle[i] >> 16) & 0xFF) / 255d,
				((specMiddle[i] >> 8) & 0xFF) / 255d,
				(specMiddle[i] & 0xFF) / 255d, 1
			));
			preset.setSpecBottom(new Color(
				((specBottom[i] >> 16) & 0xFF) / 255d,
				((specBottom[i] >> 8) & 0xFF) / 255d,
				(specBottom[i] & 0xFF) / 255d, 1
			));
			preset.setPeak(new Color(
				((peak[i] >> 16) & 0xFF) / 255d,
				((peak[i] >> 8) & 0xFF) / 255d,
				(peak[i] & 0xFF) / 255d, 1
			));
			list.add(preset);
		}
		
		SpecPreset[] presets = new SpecPreset[list.size()];
		return list.toArray(presets);
	}
	
	/**
	 * 将颜色对象 RGB 转换成 int 数值（忽略透明度）
	 * 
	 * @param color 颜色对象
	 * @return int 数值
	 */
	public static int toHexColor(Color color) {
		return Integer.parseInt(toHexString(color), 16);
	}
	
	private static String format(double val) {
	    String in = Integer.toHexString((int) Math.round(val * 255));
	    return in.length() == 1 ? "0" + in : in;
	}

	private static String toHexString(Color value) {
	    return format(value.getRed()) + format(value.getGreen()) + format(value.getBlue());
	}
}