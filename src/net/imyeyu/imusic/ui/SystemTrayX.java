package net.imyeyu.imusic.ui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;

import javafx.application.Platform;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.ctrl.Main;
import net.imyeyu.util.Config;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;

/**
 * 系统托盘，集成视图和事件
 * 
 * @author Yeyu
 *
 */
public class SystemTrayX implements ActionListener, ItemListener {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();

	private Main main;
	private TrayIcon trayIcon;
	private MenuItem playing, prev, toggle, next, random, show, exit;
	private PopupMenu pop, playType;
	private ImageIcon trayImg;
	private SystemTray tray = SystemTray.getSystemTray();
	private CheckboxMenuItem reList, reMusic;

	public SystemTrayX(Main main) {
		this.main = main;

		trayImg = new ImageIcon(getClass().getResource(Entrance.RES_PATH + "icon.png"));
		
		pop = new PopupMenu();
		trayIcon = new TrayIcon(trayImg.getImage(), rb.def("noMusicPlaying"), pop);
		playing = new MenuItem(rb.def("playing") + rb.def("noMusicPlaying"));
		prev = new MenuItem(rb.def("playPrev"));
		toggle = new MenuItem(rb.def("toggle"));
		next = new MenuItem(rb.def("playNext"));
		random = new MenuItem(rb.def("random"));
		playType = new PopupMenu(rb.def("playType"));
		reList = new CheckboxMenuItem(rb.def("repeatList"));
		reList.setState(config.getInt("repeatMode") == 0);
		reList.setName(String.valueOf(0));
		reMusic = new CheckboxMenuItem(rb.def("repeatMusic"));
		reMusic.setState(config.getInt("repeatMode") == 1);
		reMusic.setName(String.valueOf(1));
		show = new MenuItem(rb.def("showWindow"));
		exit = new MenuItem(rb.def("exit"));

		pop.add(playing);
		pop.addSeparator();
		pop.add(prev);
		pop.add(toggle);
		pop.add(next);
		pop.addSeparator();
		pop.add(random);
		pop.addSeparator();
		playType.add(reList);
		playType.add(reMusic);
		pop.add(playType);
		pop.addSeparator();
		pop.add(show);
		pop.add(exit);
		
		MenuItem[] menuItems = {prev, toggle, next, random, show, exit};
		for (int i = 0; i < menuItems.length; i++) {
			menuItems[i].addActionListener(this);
		}
		reList.addItemListener(this);
		reMusic.addItemListener(this);
		
		// 事件
		dbClickEvent();
		
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			YeyuUtils.gui().exception(e);
		}
	}
	
	/**
	 * 设置当前播放提示
	 * 
	 * @param playingTitle
	 */
	public void setTips(String playingTitle) {
		playing.setLabel(rb.def("playing") + YeyuUtils.tools().cutString(playingTitle, 12, true));
		trayIcon.setToolTip(rb.def("playing") + playingTitle);
	}
	
	/**
	 * 选择播放模式
	 * 
	 * @param i
	 */
	public void setPlayType(int i) {
		reList.setState(i == 0);
		reMusic.setState(i == 1);
	}
	
	/**
	 * 启用或就禁用播放模式选择
	 * 
	 * @param enabled
	 */
	public void setPlayType(boolean enabled) {
		playType.setEnabled(enabled);
	}
	
	/**
	 * 移除托盘图标
	 * 
	 */
	public void removeIcon() {
		tray.remove(trayIcon);
	}
	
	/**
	 * 鼠标事件
	 * 
	 */
	private void dbClickEvent() {
		trayIcon.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Platform.runLater(() -> {
						main.getStage().show();
						main.getStage().requestFocus();
					});
				}
			}
		});
	}

	public void itemStateChanged(ItemEvent e) {
		CheckboxMenuItem cmi = (CheckboxMenuItem) e.getSource();
		Platform.runLater(() -> main.setPlayType(Integer.valueOf(cmi.getName())));
	}

	/**
	 * 点击事件
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		MenuItem menu = (MenuItem) e.getSource();
		Platform.runLater(() -> {
			if (menu == prev)   main.playPrev();
			if (menu == toggle) main.playToggle();
			if (menu == next)   main.playNext();
			if (menu == random) main.random();
			if (menu == show)   main.getStage().show();
			if (menu == exit) {
				try {
					main.stop();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});
	}
}