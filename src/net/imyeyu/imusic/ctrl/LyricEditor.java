package net.imyeyu.imusic.ctrl;

import java.io.File;
import java.util.List;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.tulskiy.musique.util.AudioMath;

import javafx.geometry.Rectangle2D;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Screen;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.LyricCell;
import net.imyeyu.imusic.bean.Music;
import net.imyeyu.imusic.core.AudioPlayer;
import net.imyeyu.imusic.core.LyricEditorTimer;
import net.imyeyu.imusic.core.LyricTimer;
import net.imyeyu.imusic.dialog.Alert;
import net.imyeyu.imusic.ui.LyricEditorList;
import net.imyeyu.imusic.util.LyricLoader;
import net.imyeyu.imusic.view.ViewLyricEditor;
import net.imyeyu.util.Config;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.interfaces.GUIX;

public class LyricEditor extends ViewLyricEditor {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();

	private int wait = -1; // 延时计时器变色，不再另开线程
	private int sourcePlayType;
	private Main main;
	private LyricEditorList editor;
	private LyricEditorTimer timer;

	public LyricEditor(AudioPlayer audioPlayer, Main main, int sourcePlayType) {
		this.main = main;
		this.sourcePlayType = sourcePlayType;
		Music music = audioPlayer.getMusic();

		// 拖拽文件 - 到主舞台
		getScene().setOnDragOver(event -> {
			Dragboard dragboard = event.getDragboard(); 
			if (dragboard.hasFiles()) {
				List<File> files = dragboard.getFiles();
				if (files.size() == 1 && (files.get(0).getName().endsWith("lrc") || files.get(0).getName().endsWith("txt"))) {
					event.acceptTransferModes(TransferMode.COPY);
				}
			}
		});
		// 拖拽文件 - 释放
		getScene().setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			if (dragboard.hasFiles()) {
				List<File> files = dragboard.getFiles();
				if (files.size() == 1) importFromFile(files.get(0));
			}
		});
		
		// 默认显示在主窗体右边
		setTitle(rb.def("lrc.title", music.getTitle()));
		Rectangle2D screen = Screen.getPrimary().getBounds();
		if (screen.getWidth() < main.getStage().getX() + main.getStage().getWidth() + getWidth()) {
			setX(main.getStage().getX() - getWidth() + 14);
		} else {
			setX(main.getStage().getX() + main.getStage().getWidth() - 14);
		}
		setY(main.getStage().getY());
		show();
		
		// 编辑区
		editor = getEditor();
		
		// 关闭
		getClose().setOnAction(event -> close());
		// 往前插入
		getInsertBefore().setOnAction(event -> insertBefore());
		getcBefore().setOnAction(event -> insertBefore());
		// 往后插入
		getInsertAfter().setOnAction(event -> insertAfter());
		getcAfter().setOnAction(event -> insertAfter());
		// 插入第一句
		getInsertFirst().setOnAction(event -> {
			editor.getItems().add(0, new LyricCell());
			editor.getSelectionModel().selectFirst();
		});
		// 插入最后一句
		getInsertLast().setOnAction(event -> {
			editor.getItems().add(editor.getItems().size(), new LyricCell());
			editor.getSelectionModel().selectLast();
		});
		// 移除选中
		getRemove().setOnAction(event -> remove());
		getcRemove().setOnAction(event -> remove());
		// 计时器
		timer = new LyricEditorTimer(audioPlayer.getPlayer());
		timer.valueProperty().addListener((tmp, old, time) -> {
			if (time != null) {
				if (!audioPlayer.isSeeking()) getPb().setValue(time.longValue());
				// 时间点
				String ms = String.valueOf(time.longValue());
				getTime().setText(
					String.format("%02d", (time.longValue() / 1000) / 60) + ":" +
					String.format("%02d", (time.longValue() / 1000) % 60) + "." +
					ms.substring(ms.length() - 3, ms.length())
				);
				// 音频变动
				if (old != null && time.intValue() < old.intValue()) {
					getPb().setMax(audioPlayer.getMusic().getTime() * 1e3);
					setTitle(rb.def("lrc.title", music.getTitle()));
				}
				// 变色延时
				if (2 < wait) getTime().setTextFill(GUIX.BLACK);
				wait = -1 < wait && wait < 3 ? ++wait : -1;
			}
		});
		timer.messageProperty().addListener((tmp, o, flag) -> {
			if (flag.equals("PLAYING")) {
				YeyuUtils.gui().setBg(getToggle(), Entrance.RES_PATH + "pause.png", 16, 6, 6);
			} else {
				YeyuUtils.gui().setBg(getToggle(), Entrance.RES_PATH + "play.png", 16, 6, 6);
			}
		});
		timer.start();
		// 导入
		getImp0rt().setOnAction(event -> {
			ExtensionFilter support = new ExtensionFilter(rb.def("supportedFile"), "*.lrc", "*.txt");
			ExtensionFilter lrc = new ExtensionFilter(rb.def("lrc"), "*.lrc");
			ExtensionFilter txt = new ExtensionFilter(rb.def("txt"), "*.txt");
			ExtensionFilter all = new ExtensionFilter(rb.def("allFiles"), "*.*");
			
			File directory = new File(config.getString("pathLyric"));
			directory = directory.exists() ? directory : new File("/");
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(directory);
			fileChooser.setTitle(rb.def("selectFile"));
			fileChooser.getExtensionFilters().addAll(support, lrc, txt, all);
			File file = fileChooser.showOpenDialog(null);
			if (file != null) {
				config.put("pathLyric", file.getParentFile().getAbsolutePath());
				importFromFile(file);
			}
		});
		// 从剪切板导入
		getImp0rtClipboard().setOnAction(event -> {
			try {
				String data = YeyuUtils.tools().getIntoClipboard();
				String[] lrcs = data.split("\r\n|[\r\n]");
				editor.getItems().clear();
				for (int i = 0; i < lrcs.length; i++) {
					if (lrcs[i].equals("")) continue;
					editor.getItems().add(new LyricCell("", lrcs[i]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		// 生成并应用
		getApply().setOnAction(event -> build(main, music, audioPlayer));
		// 导出
		getExport().setOnAction(event -> {
			try {
				String data = createLyricData();
				ExtensionFilter lrc = new ExtensionFilter(rb.def("lrc"), "*.lrc");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle(rb.def("lrc.save"));
				fileChooser.getExtensionFilters().addAll(lrc);
				File file = fileChooser.showSaveDialog(null);
				if (file != null) YeyuUtils.file().stringToFile(file, data);
			} catch (IllegalArgumentException e) {
				new Alert(rb.def("error"), e.getMessage(), Alert.ERROR);
			}
		});
		// 切换播放状态
		getToggle().setOnAction(event -> {
			if (audioPlayer.isPaused()) {
				audioPlayer.play();
			} else {
				audioPlayer.pause();
			}
		});
		// 进度条最大值
		getPb().setMax(music.getTime() * 1e3);
		// 进度条按下
		getPb().setOnMousePressed(event -> audioPlayer.setSeeking(true));
		// 进度条拖拽
		getPb().setOnMouseDragged(event -> audioPlayer.setSeeking(true));
		// 进度条释放
		getPb().setOnMouseReleased(event -> {
			if (!audioPlayer.getPlayer().isPlaying()) return;
			audioPlayer.seek(AudioMath.millisToSamples((long) getPb().getValue(), audioPlayer.getPlayer().getTrack().getTrackData().getSampleRate()));
			audioPlayer.setSeeking(false);
			if (main.getLyricTimer() != null) main.getLyricTimer().resetIndex((long) getPb().getValue());
		});
		// 提示
		getTips().setOnMouseClicked(event -> {
			if (!getTips().getTooltip().isShowing()) {
				getTips().getTooltip().show(getTips(), event.getScreenX() + 16, event.getScreenY() + 16);
			}
		});
		getTips().hoverProperty().addListener((tmp, o, isHover) -> {
			if (!isHover) getTips().getTooltip().hide();
		});
		// 编辑区键盘事件
		editor.setOnKeyReleased(event -> {
			int i = editor.getSelectionModel().getSelectedIndex();
			// 回车打点
			if (!event.isControlDown() && !event.isShiftDown() && event.getCode().equals(KeyCode.ENTER)) {
				if (i < editor.getItems().size()) {
					String nowTime = getTime().getText();
					nowTime = nowTime.substring(0, nowTime.length() - 1);
					editor.getSelectionModel().getSelectedItem().setTime(nowTime);
					editor.getSelectionModel().select(++i);
					editor.requestFocus();
				}
				wait = 0;
				getTime().setTextFill(GUIX.RED);
			}
			// 上下选择
			if (!editor.isFocused() && !(getScene().getFocusOwner() instanceof VirtualFlow)) {
				if (event.getCode().equals(KeyCode.UP)) {
					editor.getSelectionModel().select(--i);
				}
				if (event.getCode().equals(KeyCode.DOWN)) {
					editor.getSelectionModel().select(++i);
				}
			}
			editor.scrollTo(i - 5);
		});
		// 舞台键盘事件
		getScene().setOnKeyReleased(event -> {
			// 进度调整
			if (event.isShiftDown()) {
				event.consume();
				if (!audioPlayer.getPlayer().isPlaying()) return;
				
				audioPlayer.setSeeking(true);
				if (event.getCode().equals(KeyCode.LEFT)) {  // 慢退
					getPb().setValue(getPb().getValue() - 1e4);
				}
				if (event.getCode().equals(KeyCode.RIGHT)) { // 快进
					getPb().setValue(getPb().getValue() + 1e4);
				}
				audioPlayer.seek(AudioMath.millisToSamples((long) getPb().getValue(), audioPlayer.getTrack().getTrackData().getSampleRate()));
				audioPlayer.setSeeking(false);
				
				main.getLyricTimer().resetIndex((long) getPb().getValue());
				return;
			}
			if (event.getCode().equals(KeyCode.F11)) {
				build(main, music, audioPlayer);
			}
		});
		// 加载当前歌词
		importFromFile(new File("lrcs/" + music.getTitle() + ".lrc"));
		
		setOnCloseRequest(event -> close());
	}
	
	public void close() {
		timer.shutdown();
		main.setPlayType(sourcePlayType);
		Entrance.setConfig(config);
		super.close();
	}
	
	private void insertBefore() {
		int i = editor.getSelectionModel().getSelectedIndex();
		i = i < 0 ? 0 : i;
		editor.getItems().add(i, new LyricCell());
		editor.getSelectionModel().select(i);
	}
	
	private void insertAfter() {
		int i = editor.getSelectionModel().getSelectedIndex();
		editor.getItems().add(i + 1, new LyricCell());
		editor.getSelectionModel().select(i + 1);
	}
	
	private void remove() {
		editor.getItems().remove(editor.getSelectionModel().getSelectedIndex());
	}
	
	/**
	 * 生成歌词并应用到主窗体
	 * 
	 * @param main        主窗体
	 * @param music       正在播放的对象
	 * @param audioPlayer 播放核心
	 */
	private void build(Main main, Music music, AudioPlayer audioPlayer) {
		File lrcFile = new File("lrcs/" + music.getTitle() + ".lrc");
		try {
			YeyuUtils.file().stringToFile(lrcFile, createLyricData());
		} catch (IllegalArgumentException e) {
			new Alert(rb.def("error"), e.getMessage(), Alert.ERROR);
			return;
		}
		if (main.getLyricTimer() != null) {
			main.getLyricTimer().shutdown();
			main.getLyricTimer().cancel();
		}
		LyricTimer lyricTimer = new LyricTimer(audioPlayer.getPlayer(), lrcFile);
		main.setLyricTimer(lyricTimer);
		main.bindLyric();
		lyricTimer.start();
	}
	
	/**
	 * 从文件导入，初始化和导入动作调用
	 * 
	 * @param file
	 */
	private void importFromFile(File file) {
		if (!file.exists()) return;
		boolean isLRC = file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("lrc");
		if (isLRC) {
			LyricLoader loader = new LyricLoader(file);
			loader.valueProperty().addListener((tmp, o, lyric) -> {
				List<String> times = lyric.getTimesSource();
				List<String> lrcs = lyric.getLrcs();
				editor.getItems().clear();
				for (int i = 0; i < lrcs.size(); i++) {
					editor.getItems().add(new LyricCell(times.get(i), lrcs.get(i)));
				}
			});
			loader.start();
		} else {
			String[] datas = YeyuUtils.file().fileToString(file, "UTF-8").split("\r\n|[\r\n]");
			editor.getItems().clear();
			for (int i = 0; i < datas.length; i++) {
				editor.getItems().add(new LyricCell("", datas[i]));
			}
		}
	}
	
	/**
	 * 以当前列表为数据创建 lrc 歌词数据
	 * 
	 * @return
	 * @throws IllegalArgumentException 时轴参数异常
	 */
	private String createLyricData() throws IllegalArgumentException {
		List<LyricCell> list = editor.getItems();
		StringBuffer sb = new StringBuffer();
		String lrc;
		for (int i = 0; i < list.size(); i++) {
			if (!list.get(i).isVaild()) {
				editor.getSelectionModel().select(i);
				editor.scrollTo(i - 4);
				throw new IllegalArgumentException(rb.def("timeAxisError"));
			}
			lrc = list.get(i).getLyric();
			lrc = lrc.trim().equals("") ? " " : lrc;
			sb.append("[" + list.get(i).getTime() + "]" + lrc + "\r\n");
		}
		return sb.toString();
	}
}