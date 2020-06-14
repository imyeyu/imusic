package net.imyeyu.imusic.bean;

import java.util.ArrayList;
import java.util.List;

import net.imyeyu.imusic.Entrance;
import net.imyeyu.util.ResourceBundleX;

public class ShortcutKeys {
	
	private String action;
	private String in;
	private String out;
	
	public ShortcutKeys() {}
	
	public ShortcutKeys(String action, String in, String out) {
		this.action = action;
		this.in = in;
		this.out = out;
	}
	
	public static List<ShortcutKeys> getAll() {
		ResourceBundleX rb = Entrance.getRb();
		List<ShortcutKeys> list = new ArrayList<>();
		list.add(new ShortcutKeys(rb.def("prev"), "Ctrl + A", "Ctrl + Shift + A"));
		list.add(new ShortcutKeys(rb.def("toggle"), "Ctrl + S", "Ctrl + Shift + S"));
		list.add(new ShortcutKeys(rb.def("next"), "Ctrl + D", "Ctrl + Shift + D"));
		list.add(new ShortcutKeys(rb.def("random"), "Ctrl + R", "Ctrl + Shift + R"));
		list.add(new ShortcutKeys(rb.def("remove"), "Ctrl + Delete", ""));
		list.add(new ShortcutKeys(rb.def("sk.volumeUp"), "", "Ctrl + Shift + \u2191"));
		list.add(new ShortcutKeys(rb.def("sk.volumeDown"), "", "Ctrl + Shift + \u2193"));
		list.add(new ShortcutKeys(rb.def("sk.listUp"), "\u2191", ""));
		list.add(new ShortcutKeys(rb.def("sk.listDown"), "\u2193", ""));
		list.add(new ShortcutKeys(rb.def("sk.play"), "Enter", ""));
		list.add(new ShortcutKeys());
		list.add(new ShortcutKeys(rb.def("lyricEditor") + " - " + rb.def("sk.buildTime"), "Enter", ""));
		list.add(new ShortcutKeys(rb.def("lyricEditor") + " - " + rb.def("fast"), "Shift + \u2192", ""));
		list.add(new ShortcutKeys(rb.def("lyricEditor") + " - " + rb.def("slow"), "Shift + \u2190", ""));
		return list;
	}

	public String getAction() {
		return action;
	}

	public String getIn() {
		return in;
	}

	public String getOut() {
		return out;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setIn(String in) {
		this.in = in;
	}

	public void setOut(String out) {
		this.out = out;
	}
}