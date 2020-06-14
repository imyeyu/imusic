package net.imyeyu.imusic.ctrl;

import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import com.tulskiy.musique.util.AudioMath;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.InternetCtrl;
import net.imyeyu.imusic.bean.Music;
import net.imyeyu.imusic.core.AudioPlayer;
import net.imyeyu.imusic.core.AudioTimer;
import net.imyeyu.imusic.core.LyricTimer;
import net.imyeyu.imusic.dialog.Confirm;
import net.imyeyu.imusic.ui.SystemTrayX;
import net.imyeyu.imusic.util.CacheLoader;
import net.imyeyu.imusic.util.InternetCtrler;
import net.imyeyu.imusic.util.MusicUtil;
import net.imyeyu.imusic.view.ViewMain;
import net.imyeyu.util.Config;
import net.imyeyu.util.Configer;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.TipsX;
import net.imyeyu.util.interfaces.GUIX;

public class Main extends Application {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();
	
	// 下一首标记，不等于 -1 时调用这个变量，无论任何播放模式，下一首播放事件都应有最高优先级
	private int playNext = -1;
	// 提示区
	private Label tips;
	// 主窗体
	private Stage stage;
	// 视图层
	private ViewMain view;
	// 频谱颜色视图层
	private SpecColor specColor;
	// 歌词计时器
	private LyricTimer lyricTimer;
	// 音频计时器
	private AudioTimer audioTimer;
	// 音频播放器
	private AudioPlayer audioPlayer;
	// 缓存列表加载器
	private CacheLoader cacheLoader;
	// 系统托盘
	private SystemTrayX systemTray;
	// 远程控制核心
	private InternetCtrler icCtrler;
	// 当前播放 ID （相对列表）
	private SimpleIntegerProperty playingID = new SimpleIntegerProperty(-1);
	// 远程控制延时
	private SimpleIntegerProperty icPing = new SimpleIntegerProperty(0);

	public void start(Stage stage) throws Exception {
		this.stage = stage;
		view = new ViewMain(stage);
		tips = view.getTips();
		
		// 播放器核心
		audioPlayer = new AudioPlayer();
		audioPlayer.setVolume(config.getDouble("volume"));
		// 播放器核心事件
		audioPlayer.getPlayer().addListener(event -> {
			switch (event.getEventCode()) {
				case STOPPED:         // 播放完成
					Platform.runLater(() -> {
						int type = view.getPlayType().getSelectionModel().getSelectedIndex();
						if (playNext != -1) {
							playingID.set(playNext);
						} else {
							if (type == 0 && playNext == -1) {
								playingID.set(playingID.get() == view.getList().getItems().size() - 1 ? 0 : playingID.add(1).get());
							}
						}
						view.setListSelect(playingID.get());
						playSelected();
						playNext = -1;
					});
					break;
				case PAUSED:          // 暂停
					YeyuUtils.gui().setBg(view.getToggle(), Entrance.RES_PATH + "play.png", 16, 6, 5);
					break;
				case PLAYING_STARTED: // 播放
					YeyuUtils.gui().setBg(view.getToggle(), Entrance.RES_PATH + "pause.png", 16, 6, 5);
					break;
				default:
					break;
			}
		});
		// 进度条绑定
		audioTimer = new AudioTimer(audioPlayer.getPlayer());
		audioTimer.valueProperty().addListener((tmp, o, n) -> {
			if (n != null) {
				view.getTimeNow().setText(
					String.format("%02d", Math.abs(n.longValue() / 60000)) + ":" +
					String.format("%02d", n.longValue() / 1000 % 60)
				);
				if (!audioPlayer.isSeeking()) view.getPb().setValue(n.longValue());
			}
		});
		audioTimer.start();
		// 变更播放 ID
		playingID.addListener((tmp, o, n) -> {
			view.getInfoPane().setListStatus(playingID.get(), view.getList().getItems().size());
			view.setListSelect(n.intValue());
		});

		// 拖拽文件 - 到主舞台
		stage.getScene().setOnDragOver(event -> {
			Dragboard dragboard = event.getDragboard(); 
			if (dragboard.hasFiles()) {
				List<File> files = dragboard.getFiles();
				if (1 < files.size()) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					if (files.get(0).getName().endsWith("mp3") || files.get(0).getName().endsWith("flac") || files.get(0).getName().endsWith("lrc")) {
						event.acceptTransferModes(TransferMode.COPY);
					}
				}
			}
		});
		// 拖拽文件 - 释放
		stage.getScene().setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			if (dragboard.hasFiles()) {
				List<File> files = dragboard.getFiles();
				if (0 < files.size()) {
					int canPlayIndex = -1;
					for (int i = 0; i < files.size(); i++) {
						if (files.get(i).getName().endsWith("mp3") || files.get(i).getName().endsWith("flac")) {
							canPlayIndex = canPlayIndex == -1 ? i : canPlayIndex;
							view.getList().getItems().add(files.get(i));
							continue;
						}
						if (files.get(i).getName().endsWith("lrc")) {
							// 切换歌词滚动核心
							if (lyricTimer != null) {
								lyricTimer.shutdown();
								lyricTimer.cancel();
							}
							lyricTimer = new LyricTimer(audioPlayer.getPlayer(), files.get(i));
							bindLyric();
							lyricTimer.start();
							break;
						}
					}
					if (canPlayIndex != -1) {
						view.setListSelect(files.get(canPlayIndex));
						playSelected();
					}
				}
			}
		});
		// 添加文件
		view.getAddFiles().setOnAction(event -> {
			ExtensionFilter support = new ExtensionFilter(rb.def("supportedFile"), "*.flac", "*.mp3");
			ExtensionFilter mp3 = new ExtensionFilter("MP3" + rb.l("music"), "*.mp3");
			ExtensionFilter flac = new ExtensionFilter(rb.def("flac"), "*.flac");
			ExtensionFilter all = new ExtensionFilter(rb.def("allFiles"), "*.*");
			
			File directory = new File(config.getString("pathAdd"));
			directory = directory.exists() ? directory : new File("/");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(rb.def("selectFiles"));
			fileChooser.setInitialDirectory(directory);
			fileChooser.getExtensionFilters().addAll(support, flac, mp3, all);
			List<File> files = fileChooser.showOpenMultipleDialog(null);
			if (files != null) {
				for (int i = 0; i < files.size(); i++) {
					if (files.get(i).getName().endsWith("mp3") || files.get(i).getName().endsWith("flac")) {
						view.getList().getItems().add(files.get(i));
					}
				}
				view.getInfoPane().setListStatus(playingID.get(), view.getList().getItems().size());
				config.put("pathAdd", files.get(0).getParentFile().getAbsolutePath());
			}
		});
		
		// 添加文件夹
		view.getAddFolder().setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle(rb.def("selectFolder"));
			directoryChooser.setInitialDirectory(new File(config.getString("pathAdd")));
			File folder = directoryChooser.showDialog(null);
			if (folder != null) {
				File[] files = folder.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().endsWith("mp3") || files[i].getName().endsWith("flac")) {
						view.getList().getItems().add(files[i]);
					}
				}
				view.getInfoPane().setListStatus(playingID.get(), view.getList().getItems().size());
				config.put("pathAdd", folder.getAbsolutePath());
			}
		});
		// 随机列表
		view.getRandom().setOnAction(event -> random());
		view.getcRandom().setOnAction(event -> random());
		// 打开文件位置
		view.getcOpenFolder().setOnAction(event -> {
			try {
				String absPath = view.getList().getSelectionModel().getSelectedItem().getAbsolutePath();
				Runtime.getRuntime().exec("explorer.exe /select, '" + absPath + "'");
//				Desktop.getDesktop().open(view.getList().getSelectionModel().getSelectedItem().getParentFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		// 移除选中
		view.getRemove().setOnAction(event -> remove());
		view.getcRemove().setOnAction(event -> remove());
		// 清空列表
		view.getClear().setOnAction(event -> {
			playingID.set(0);
			view.getList().getItems().clear();
			view.getInfoPane().setListStatus(playingID.get() - 1, view.getList().getItems().size());
		});
		// 右键菜单
		view.getList().setOnContextMenuRequested(event -> {
			List<File> selected = view.getList().getSelectionModel().getSelectedItems();
			view.getcPlayNow().setDisable(1 < selected.size());
			view.getcPlayNext().setDisable(1 < selected.size());
		});
		// 设置
		view.getSetting().setOnAction(event -> setting());
		// 列表搜索
		view.getSearch().setOnAction(event -> search());
		view.getSearchKey().setOnKeyReleased(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) search();
		});
		view.getSearchKey().textProperty().addListener((tmp, o, searchKey) -> {
			if (searchKey != null && searchKey.equals("")) view.setListSelect(playingID.get());
		});
		// 音频双击
		view.getList().setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				if (config.getString("dbClickEvent").equals("playNow")) {
					playSelected();
				} else {
					playNextItem();
				}
			}
		});
		// 立即播放
		view.getcPlayNow().setOnAction(event -> playSelected());
		// 下一首播放
		view.getcPlayNext().setOnAction(event -> playNextItem());
		// 面板切换
		view.getInfo().setOnAction(event -> view.toggleTab(view.getInfoPane()));
		view.getLrc().setOnAction(event -> view.toggleTab(view.getLyricPane()));
		view.getSpec().setOnAction(event -> {
			view.getSpecPane().setSize(view.getTabPane().getWidth(), view.getTabPane().getHeight() - 2);
			view.toggleTab(view.getSpecPane());
		});
		// 远程控制提示
		view.getICLabel().setOnMouseClicked(event -> setting());
		// 歌词调整
		view.getLyricPane().getLrcSlowX().setOnAction(event -> {
			lyricTimer.slowX();
			view.getLyricPane().offset(-1000);
		});
		view.getLyricPane().getLrcSlow().setOnAction(event -> {
			lyricTimer.slow();
			view.getLyricPane().offset(-200);
		});
		view.getLyricPane().getLrcSave().setOnAction(event -> lyricTimer.save());
		view.getLyricPane().getLrcFast().setOnAction(event -> {
			lyricTimer.fast();
			view.getLyricPane().offset(200);
		});
		view.getLyricPane().getLrcFastX().setOnAction(event -> {
			lyricTimer.fastX();
			view.getLyricPane().offset(1000);
		});
		// 导入歌词
		view.getLyricPane().getImport().setOnAction(event -> {
			if (!audioPlayer.getPlayer().isPlaying()) return;
			ExtensionFilter lrc = new ExtensionFilter(rb.def("lrc"), "*.lrc");
			
			File directory = new File(config.getString("pathLyric"));
			directory = directory.exists() ? directory : new File("/");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(directory);
			fileChooser.setTitle(rb.def("selectLrc"));
			fileChooser.getExtensionFilters().addAll(lrc);
			File file = fileChooser.showOpenDialog(null);
			if (file != null) {
				// 切换歌词滚动核心
				if (lyricTimer != null) {
					lyricTimer.shutdown();
					lyricTimer.cancel();
				}
				lyricTimer = new LyricTimer(audioPlayer.getPlayer(), file);
				bindLyric();
				lyricTimer.start();
				config.put("pathLyric", file.getParentFile().getAbsolutePath());
			}
		});
		// 歌词编辑器
		view.getLyricPane().getEditor().setOnAction(event -> {
			if (audioPlayer.getMusic() != null) {
				int playType = view.getPlayType().getSelectionModel().getSelectedIndex();
				setPlayType(1); // 强制单曲循环
				systemTray.setPlayType(false);
				new LyricEditor(audioPlayer, this, playType);
			}
		});
		// 频谱颜色设置
		view.getSpecColor().setOnMouseClicked(event -> {
			if (specColor == null) {
				specColor = new SpecColor(view.getSpectrum(), event.getScreenX(), event.getScreenY());
			} else {
				if (!specColor.isShowing()) {
					specColor.show();
					specColor.getStage().show();
				}
				specColor.getStage().setX(event.getScreenX());
				specColor.getStage().setY(event.getScreenY());
				specColor.getStage().requestFocus();
			}
		});
		// 频谱事件
		view.getSpecPane().widthProperty().addListener((tmp, o, n) -> view.getSpectrum().setWidth(n.intValue() - 1));
		view.getSpecPane().heightProperty().addListener((tmp, o, n) -> view.getSpectrum().setHeight(n.intValue() - 51));
		// 频谱调整 - 拖动
		view.getSpecBoost().valueProperty().addListener((tmp, o, n) -> {
			view.getTips().setText(rb.def("spec.boost") + n.intValue());
			view.getSpectrum().setBoost(n.intValue());
		});
		view.getSpecSpacing().valueProperty().addListener((tmp, o, n) -> {
			view.getTips().setText(rb.def("spec.spacing") + n.intValue());
			view.getSpectrum().setSpacing(n.intValue());
		});
		view.getSpecSpeed().valueProperty().addListener((tmp, o, n) -> {
			view.getTips().setText(rb.def("spec.speed") + n.intValue());
			view.getSpectrum().setSpeed(n.intValue());
		});
		view.getSpecBand().valueProperty().addListener((tmp, o, n) -> {
			view.getTips().setText(rb.def("spec.band") + n.intValue());
			view.getSpectrum().setBand(n.intValue());
		});
		// 频谱调整 - 释放
		view.getSpecBoost().setOnMouseReleased(event -> {
			YeyuUtils.gui().tips(view.getTips(), rb.def("spec.boost") + (int) view.getSpecBoost().getValue());
			config.put("specBoost", (int) view.getSpecBoost().getValue());
		});
		view.getSpecSpacing().setOnMouseReleased(event -> {
			YeyuUtils.gui().tips(view.getTips(), rb.def("spec.spacing") + (int) view.getSpecSpacing().getValue());
			config.put("specSpacing", (int) view.getSpecSpacing().getValue());
		});
		view.getSpecSpeed().setOnMouseReleased(event -> {
			YeyuUtils.gui().tips(view.getTips(), rb.def("spec.speed") + (int) view.getSpecSpeed().getValue());
			config.put("specSpeed", (int) view.getSpecSpeed().getValue());
		});
		view.getSpecBand().setOnMouseReleased(event -> {
			YeyuUtils.gui().tips(view.getTips(), rb.def("spec.band") + (int) view.getSpecBand().getValue());
			config.put("specBand", (int) view.getSpecBand().getValue());
		});
		// 封面
		view.getCover().setOnMouseClicked(event -> {
			if (audioPlayer.getMusic() != null) new Cover(audioPlayer.getMusic());	
		});
		// 上一首
		view.getPrev().setOnAction(event -> playPrev());
		// 播放状态切换
		view.getToggle().setOnAction(event -> playToggle());
		// 下一首
		view.getNext().setOnAction(event -> playNext());
		// 进度条按下
		view.getPb().setOnMousePressed(event -> audioPlayer.setSeeking(true));
		// 进度条拖拽
		view.getPb().setOnMouseDragged(event -> audioPlayer.setSeeking(true));
		// 进度条释放
		view.getPb().setOnMouseReleased(event -> {
			if (!audioPlayer.getPlayer().isPlaying()) return;
			audioPlayer.seek(AudioMath.millisToSamples((long) view.getPb().getValue(), audioPlayer.getTrack().getTrackData().getSampleRate()));
			audioPlayer.setSeeking(false);
			if (lyricTimer != null) lyricTimer.resetIndex((long) view.getPb().getValue());
		});
		// 播放器音量
		view.getVolume().valueProperty().addListener((tmp, o, n) -> audioPlayer.setVolume(n.doubleValue()));
		view.getVolume().setOnMouseReleased(event -> config.put("volume", view.getVolume().getValue()));
		// 播放模式
		view.getPlayType().valueProperty().addListener((tmp, o, n) -> {
			int i = view.getPlayType().getSelectionModel().getSelectedIndex();
			systemTray.setPlayType(i);
			config.put("repeatMode", i);
			sendPlayState();
		});
		// 主窗体快捷键
		stage.getScene().setOnKeyReleased(event -> {
			KeyCode code = event.getCode();
			if (event.isControlDown() && !event.isShiftDown()) { // Control 组合键
				switch (code) {
					case A:      playPrev();   return;
					case S:      playToggle(); return;
					case D:      playNext();   return;
					case R:      random();     return;
					case DELETE: remove();     return;
					default:
				}
			}
			switch (code) { // 单键
				case UP:    view.setListSelect(view.getList().getSelectionModel().getSelectedIndex() - 1); return;
				case DOWN:  view.setListSelect(view.getList().getSelectionModel().getSelectedIndex() + 1); return;
				case ENTER: playSelected();                                                                return;
				default:
			}
		});
		// 全局快捷键
		regShortcutKeyInWindows();
		
		
		// 读取缓存播放列表
		cacheLoader = new CacheLoader();
		cacheLoader.valueProperty().addListener((tmp, o, list) -> {
			view.getList().getItems().setAll(list);
		});
		cacheLoader.setOnSucceeded(event -> {
			// 选择上一次播放的音频
			playingID.set(config.getInt("playingID"));
			// 自动播放
			if (config.getBoolean("autoPlay") && playingID.get() != -1) playSelected();
		});
		cacheLoader.start();
		// 远程控制
		initInternetCtrler();
		// 系统托盘
		systemTray = new SystemTrayX(this);
		// 关闭事件
		Platform.setImplicitExit(config.getBoolean("exitOnClose"));
		stage.setOnCloseRequest(event -> {
			if (config.getBoolean("exitOnClose")) {
				// 停止播放
				if (audioPlayer.getPlayer().isPlaying()) audioPlayer.pause();
				audioPlayer.stop();
				// 关闭其他窗口
				if (specColor != null && specColor.isShowing()) specColor.close();
			}
		});
		// 关闭启动页
		if (SplashScreen.getSplashScreen() != null) SplashScreen.getSplashScreen().close();
	}
	
	/**
	 * 退出程序
	 * 
	 */
	public void stop() throws Exception {
		stage.close();
		// 缓存播放列表
		cacheLoader.setCacheList(view.getList().getItems());
		// 保存配置
		config.put("width", stage.getWidth());
		config.put("height", stage.getHeight());
		config.put("playingID", playingID.get());
		Entrance.setConfig(config);
		new Configer("iMusic").set(Entrance.getConfig());
		// 移除托盘
		systemTray.removeIcon();
		// 终止后台进程
		if (lyricTimer != null) lyricTimer.shutdown();
		if (icCtrler != null) icCtrler.shutdown();
		audioTimer.shutdown();
		view.getSpecPane().shutdown();
		super.stop();
	}
	
	/**
	 * 重启程序
	 * 
	 */
	private void restart() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
					Runtime.getRuntime().exec("java -jar " + System.getProperty("java.class.path"));
				} catch (IOException e) {
					e.printStackTrace();
				}
            }    
        });
		try {
			stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 随机列表
	 * 
	 */
	public void random() {
		ObservableList<File> list = view.getList().getItems();
		File playNextFile = null;
		if (playingID.get() != -1 && 0 < list.size()) {
			File playingFile = list.get(playingID.get());
			playNextFile = playNext != -1 ? list.get(playNext) : null;
			Collections.shuffle(list);
			playingID.set(view.getList().getItems().indexOf(playingFile));
			view.setListSelect(playingID.get());
			view.getList().scrollTo(playingID.get() - 4);
		} else {
			Collections.shuffle(list);
		}
		if (playNextFile != null) {
			playNext = list.indexOf(playNextFile);
			view.getInfoPane().setListStatus(playingID.get(), playNext, view.getList().getItems().size());
		}
		sendPlayState();
	}
	
	/**
	 * 搜索列表
	 * 
	 */
	private int searchIndex = 0;
	private void search() {
		boolean isFind = false;
		String searchKey = view.getSearchKey().getText();
		ObservableList<File> list = view.getList().getItems();
		for (int i = searchIndex; i < list.size(); i++) {
			if (list.get(i).getName().indexOf(searchKey) != -1) {
				view.setListSelect(i);
				view.getList().scrollTo(i - 4);
				searchIndex = i;
				isFind = true;
				break;
			}
		}
		searchIndex = isFind ? ++searchIndex : 0;
	}
	
	/**
	 * 移除列表选中
	 * 
	 */
	private void remove() {
		ObservableList<File> list = view.getList().getItems();
		File playingFile = list.get(playingID.get());
		ObservableList<File> selected = view.getList().getSelectionModel().getSelectedItems();
		view.getList().getItems().removeAll(selected);
		playingID.set(list.indexOf(playingFile));
		playingID.set(playingID.get() == -1 ? 0 : playingID.get());
		sendPlayState();
	}
	
	/**
	 * 打开设置
	 * 
	 */
	private void setting() {
		Setting setting = new Setting(audioPlayer.getPlayer(), view.getVolume(), icCtrler);
		setting.getReConnectBtn().setOnAction(subEvent -> {
			if (config.getBoolean("ic")) {
				setting.getIcTips().setTextFill(GUIX.RED);
				setting.getIcTips().setText(rb.def("icConnectError", "-1"));
				icCtrler.shutdown();
				initInternetCtrler();
			}
		});
		setting.changeLangProperty().addListener((tmp, o, isChanged) -> {
			if (isChanged) {
				Confirm confirm = new Confirm(rb.def("settingRestartConfirm"), rb.def("restart"));
				confirm.initConfirm(confirm, new EventHandler<ActionEvent>() {
					public void handle(ActionEvent event) {
						confirm.close();
						restart(); // 重新启动
					}
				});
			}
		});
	}
	
	/**
	 * 播放列表选中项
	 * 
	 */
	private void playSelected() {
		MultipleSelectionModel<File> selector = view.getList().getSelectionModel();
		if (selector.getSelectedIndex() == -1) selector.select(0);
		File file = selector.getSelectedItem();
		if (file != null) {
			if (file.exists()) {
				playingID.set(selector.getSelectedIndex());
				play(file);
			} else {
				YeyuUtils.gui().tips(view.getTips(), rb.def("notFoundFile") + file.getAbsolutePath(), 5000, TipsX.ERROR);
			}
		} else {
			YeyuUtils.gui().tips(view.getTips(), rb.def("notMusic"), 5000, TipsX.ERROR);
		}
	}
	
	/**
	 * 远程控制初始化
	 * 
	 */
	private void initInternetCtrler() {
		icCtrler = new InternetCtrler(icPing, config.getString("icIP"), config.getInt("icPort"));
		icCtrler.valueProperty().addListener((tmp, o, ACTION) -> {
			if (ACTION != null) {
				view.icTips(icPing.get(), true);
				if (!InternetCtrl.toString(ACTION).equals("")) {
					YeyuUtils.gui().tips(tips, rb.def("icDoCtrl") + InternetCtrl.toString(ACTION), 4000, TipsX.WARNING);
				}
				switch (ACTION) {
					case PLAY_PREV:   playPrev();                       break;
					case PLAY_TOGGLE: playToggle();                     break;
					case PLAY_NEXT:   playNext();                       break;
					case RANDOM:      random();                         break;
					case ACTION:      view.icTips(icPing.get(), false); break;
				}
			}
		});
		icCtrler.exceptionProperty().addListener((tmp, o, e) -> {
			YeyuUtils.gui().tips(tips, rb.def("icConnectClose"), 4000, TipsX.ERROR);
			icPing.set(-1);
			view.icTips(icPing.get(), false);
		});
		icPing.addListener((tmp, o, ping) -> view.icTips(ping.intValue(), false));
		if (config.getBoolean("ic")) icCtrler.start();
	}
	
	/**
	 * 上一首
	 * 
	 */
	public void playPrev() {
		if (0 < playingID.get()) {
			playingID.set(playingID.get() - 1);
		} else {
			playingID.set(view.getList().getItems().size() - 1);
		}
		view.setListSelect(playingID.get());
		playSelected();
	}
	
	/**
	 * 切换播放状态
	 * 
	 */
	public void playToggle() {
		if (!audioPlayer.getPlayer().getAudioOutput().isOpen()) {
			if (playNext != -1) {
				view.setListSelect(playNext);
				playNext = -1;
			}
			playSelected();
		} else {
			if (audioPlayer.isPaused()) {
				audioPlayer.play();
			} else {
				audioPlayer.pause();
			}
		}
	}
	
	/**
	 * 下一首
	 * 
	 */
	public void playNext() {
		if (playNext != -1) {
			playingID.set(playNext);
		} else {
			if (playingID.get() < view.getList().getItems().size() - 1) {
				playingID.set(playingID.get() + 1);
			} else {
				playingID.set(0);
			}
		}
		view.setListSelect(playingID.get());
		playSelected();
		playNext = -1;
	}
	
	/**
	 * 下一首播放列表选中项
	 * 
	 */
	private void playNextItem() {
		playNext = view.getList().getSelectionModel().getSelectedIndex();
		view.getInfoPane().setListStatus(playingID.get(), playNext, view.getList().getItems().size());
		String name = view.getList().getSelectionModel().getSelectedItem().getName();
		YeyuUtils.gui().tips(tips, rb.def("playNextTips") + name, 4000, TipsX.WARNING);
	}
	
	/**
	 * 发送播放信息到远程控制中转接口
	 * 
	 */
	private void sendPlayState() {
		int next = playingID.get() == view.getList().getItems().size() - 1 ? 0 : playingID.get() + 1;
		next = playNext != -1 ? playNext : next;
		icCtrler.setState(
			view.getList().getItems().get(playingID.get()).getName(),
			view.getList().getItems().get(next).getName(),
			view.getPlayType().getSelectionModel().getSelectedIndex()
		);
	}
	
	/**
	 * 播放音频
	 * 
	 * @param file 音频文件
	 */
	private void play(File file) {
		// 音频信息
		Music music = MusicUtil.getMusicInfo(file, view.getTips());
		audioPlayer.setMusic(music);
		
		/* 窗体标题     */ stage.setTitle(music.getTitle());
		/* 播放ID       */ playingID.set(view.getList().getItems().indexOf(file));
		/* 播放信息面板 */ view.getInfoPane().set(music);
		/* 频谱面板     */ view.getSpecPane().setTitle(music.getTitle());
		/* 系统托盘     */ systemTray.setTips(music.getTitle());
		/* 远程控制     */ sendPlayState();
		
		// 封面
		BackgroundSize bs = new BackgroundSize(-1, -1, true, true, true, true);
		BackgroundImage bgImg = new BackgroundImage(music.getCover(), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
		view.getCover().setBackground(new Background(bgImg));
		// 进度条最大值
		view.getPb().setMax(music.getTime() * 1e3);
		// 时间最大值
		view.getTimeMax().setText(String.format("%02d", Math.abs(music.getTime() / 60)) + ":" + String.format("%02d", music.getTime() % 60));
		try {
			// 启动歌词滚动核心
			if (lyricTimer != null) {
				lyricTimer.shutdown();
				lyricTimer.cancel();
			}
			lyricTimer = new LyricTimer(audioPlayer.getPlayer(), music.getTitle(), String.valueOf(music.getTime()));
			bindLyric();
			lyricTimer.start();
			// 播放器音频
			audioPlayer.play(file);
		} catch (Exception e) {
			YeyuUtils.gui().tips(view.getTips(), rb.def("audioPlayerError"), 4000, TipsX.ERROR);
		}
	}
	
	/**
	 * 绑定歌词，除了上面播放方法会自动绑定，还会在手动选择歌词时调用此绑定
	 * 
	 */
	public void bindLyric() {
		ListView<String> lrcView = view.getLyricPane().getLrcs();
		// 获取歌词
		lyricTimer.valueProperty().addListener((tmp, o, n) -> {
			if (n != null) {
				lrcView.getItems().clear();
				lrcView.getItems().setAll(n);
				lrcView.scrollTo(0);
				view.getLyricPane().resetOffset();
			}
		});
		// 滚动歌词
		MultipleSelectionModel<String> selector = lrcView.getSelectionModel();
		lyricTimer.messageProperty().addListener((tmp, o, n) -> {
			if (!n.equals("isGetFail")) {
				selector.select(Integer.parseInt(n));
				lrcView.scrollTo(selector.getSelectedIndex() - 3);
			} else {
				YeyuUtils.gui().tips(tips, rb.def("lyricGetFail"), 3500, TipsX.ERROR);
			}
		});
		// 设置标题
		lyricTimer.titleProperty().addListener((tmp, o, n) -> {
			if (n != null) view.getLyricPane().setTitle(audioPlayer.getMusic().getTitle(), n);
		});
		// 异常
		lyricTimer.exceptionProperty().addListener((tmp, o, e) -> e.printStackTrace());
	}
	
	private final int PREV = 0, TOGGLE = 1, NEXT = 2, RANDOM = 3, VOLUME_UP = 4, VOLUME_DOWN = 5;
	/**
	 * 注册全局快捷键（Windows 系统）
	 * 
	 */
	private void regShortcutKeyInWindows() {
		JIntellitype.getInstance().registerHotKey(PREV, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int) 'A');
		JIntellitype.getInstance().registerHotKey(TOGGLE, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int) 'S');
		JIntellitype.getInstance().registerHotKey(NEXT, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int) 'D');
		JIntellitype.getInstance().registerHotKey(RANDOM, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int) 'R');
		JIntellitype.getInstance().registerHotKey(VOLUME_UP, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, 38);
		JIntellitype.getInstance().registerHotKey(VOLUME_DOWN, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, 40);
		JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
			public void onHotKey(int key) {
				Platform.runLater(() -> {
					switch (key) {
						case PREV:        playPrev();                                                 return;
						case TOGGLE:      playToggle();                                               return;
						case NEXT:        playNext();                                                 return;
						case RANDOM:      random();                                                   return;
						case VOLUME_UP:   view.getVolume().setValue(view.getVolume().getValue() + 5); return;
						case VOLUME_DOWN: view.getVolume().setValue(view.getVolume().getValue() - 5); return;
					}
				});
			}
		});
	}

	public Stage getStage() {
		return stage;
	}
	
	/**
	 * 对外提供歌词核心接口，主要面向歌词编辑器
	 * 
	 * @return
	 */
	public LyricTimer getLyricTimer() {
		return lyricTimer;
	}
	
	public void setLyricTimer(LyricTimer lyricTimer) {
		this.lyricTimer = lyricTimer;
	}
	
	/**
	 * 对外提供播放模式选择接口（通常是系统托盘调用的）
	 * 
	 * @param i 播放模式，0 为列表循环，1 为单曲循环
	 */
	public void setPlayType(int i) {
		systemTray.setPlayType(true);
		view.getPlayType().getSelectionModel().select(i);
	}
}