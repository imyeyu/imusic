package net.imyeyu.imusic.ctrl;

import java.net.URL;

import com.tulskiy.musique.audio.player.Player;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.util.InternetCtrler;
import net.imyeyu.imusic.util.Timer;
import net.imyeyu.imusic.view.ViewSetting;
import net.imyeyu.util.Config;
import net.imyeyu.util.Lang;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.interfaces.GUIX;

public class Setting extends ViewSetting {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();
	
	public static Timer timer;
	private static TimerEventHandle eventHandle;
	private static TimerVolumeListener volumeListener;
	private static TimerProgressListener progressListener;
	
	private Player player;
	private Slider mainVolume;
	private CheckIC checkIC;
	private boolean isCancelAction = false;
	private SimpleBooleanProperty changeLangProperty = new SimpleBooleanProperty(false);

	public Setting(Player player, Slider mainVolume, InternetCtrler icCtrler) {
		this.player = player;
		this.mainVolume = mainVolume;
		// 选择面板
		getGeneral().setOnAction(event -> togglePane(getGeneralPane()));
		getTimer().setOnAction(event -> togglePane(getTimerPane()));
		getShortcutKey().setOnAction(event -> togglePane(getShortcutKeyPane()));
		getAbout().setOnAction(event -> togglePane(getAboutPane()));
		// 远程控制
		checkIC = new CheckIC(icCtrler.getIP());
		checkIC.valueProperty().addListener((tmp, o, delay) -> {
			if (delay != null) {
				if (icCtrler.isConnect()) {
					getIcTips().setTextFill(delay.intValue() < 64 ? GUIX.GREEN : GUIX.ORANGE);
					getIcTips().setText(rb.def("icConnectSuccess", delay.toString()));
				} else {
					getIcTips().setTextFill(GUIX.RED);
					getIcTips().setText(rb.def("icConnectError", delay.toString()));
				}
			}
		});
		checkIC.start();
		// 远程控制设置
		getIc().selectedProperty().addListener((tmp, o, isSelect) -> {
			config.put("ic", isSelect);
			Entrance.setConfig(config);
			getIcReConnect().setDisable(!isSelect);
		});
		getIcIP().getTextField().focusedProperty().addListener((tmp, o, isFocused) -> {
			if (!isFocused) {
				checkIC.setIP(getIcIP().getText());
				config.put("icIP", getIcIP().getText());
				Entrance.setConfig(config);
			}
		});
		getIcPort().getTextField().focusedProperty().addListener((tmp, o, isFocused) -> {
			if (!isFocused) {
				config.put("icPort", getIcPort().getText());
				Entrance.setConfig(config);
			}
		});

		// 定时器
		getTimerBar().reset(getSecond());
		setDisableTimerSetting(false);
		if (timer != null) bindTimerEvent();
		// 定时 - 选择时间
		getTimeH().valueProperty().addListener((tmp, o, n) -> getTimerBar().setValue(-1, getSecond()));
		getTimeM().valueProperty().addListener((tmp, o, n) -> getTimerBar().setValue(-1, getSecond()));
		// 定时 - 音量
		getToVolume().valueProperty().addListener((tmp, o, volume) -> getVolumeValue().setText(String.valueOf(volume.intValue())));
		// 定时 - 启动
		getStart().setOnAction(event -> {
			isCancelAction = false;
			if (timer != null) {
				timer.removeEventHandler(WorkerStateEvent.ANY, eventHandle);
				timer.shutdown();
				timer.cancel();
				timer = null;
				volumeListener = null;
				progressListener = null;
			}
			timer = new Timer();
			timer.setTime(getSecond(), mainVolume.getValue(), getToVolume().getValue());
			timer.restart();
			bindTimerEvent();
			config.put("timerH", getTimeH().getSelectionModel().getSelectedIndex());
			config.put("timerM", getTimeM().getSelectionModel().getSelectedIndex());
			config.put("timerVolume", getToVolume().getValue());
			config.put("timerEnd", getEnd().getSelectionModel().getSelectedIndex());
			Entrance.setConfig(config);
		});
		// 定时 - 取消
		getCancel().setOnAction(event -> {
			isCancelAction = true;
			timer.shutdown();
			getTimerBar().reset(getSecond());
			setDisableTimerSetting(false);
		});
		// 版本更新
		CheckVersion request = new CheckVersion();
		request.valueProperty().addListener((obs, oldValue, newValue) ->{
			if (!newValue.equals(getVersionValue())) {
				getVersion().setText(rb.def("nowVersion") + getVersionValue() + rb.def("latestVersion") + newValue + rb.def("clickHereUpdate"));
				getVersion().setCursor(Cursor.HAND);
				getVersion().setOnMouseClicked(event -> {
					try {
						YeyuUtils.network().openURIInBrowser(new URL("https://imyeyu.net/article/public/aid126.html").toURI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} else {
				getVersion().setText(getVersionValue());
			}
		});
		request.start();
		// 博客
		getBlog().hoverProperty().addListener((tmp, o, isHover) -> {
			getBlog().setTextFill(isHover ? GUIX.PINK : GUIX.BLACK);
		});
		getBlog().setOnMouseClicked(event -> {
			try {
				YeyuUtils.network().openURIInBrowser(new URL("https://www.imyeyu.net").toURI());
			} catch (Exception e) {
				YeyuUtils.gui().exception(e);
			}
		});
		// 保存
		getSave().setOnAction(event -> {
			icCtrler.shutdown(!getIc().isSelected() && icCtrler.isConnect());
			Platform.setImplicitExit(getCloseToExit().isSelected());

			changeLangProperty.set(!config.getString("language").equals(Lang.toCode(getLang().getSelectionModel().getSelectedItem())));
			config.put("language", Lang.toCode(getLang().getSelectionModel().getSelectedItem()));
			config.put("autoPlay", getAutoPlay().isSelected());
			config.put("dbClickEvent", getPlayNow().isSelected() ? "playNow" : "playNext");
			config.put("exitOnClose", getCloseToExit().isSelected());
			config.put("ic", getIc().isSelected());
			config.put("icIP", getIcIP().getText());
			config.put("icPort", getIcPort().getText());
			config.put("timerH", getTimeH().getSelectionModel().getSelectedIndex());
			config.put("timerM", getTimeM().getSelectionModel().getSelectedIndex());
			config.put("timerVolume", getToVolume().getValue());
			config.put("timerEnd", getEnd().getSelectionModel().getSelectedIndex());
			Entrance.setConfig(config);
			close();
		});
		// 关闭
		getClose().setOnAction(event -> close());
		
		setOnCloseRequest(event -> close());
	}
	
	public void close() {
		checkIC.shutdown();
		super.close();
	}
	
	public Button getReConnectBtn() {
		return getIcReConnect();
	}
	
	public SimpleBooleanProperty changeLangProperty() {
		return changeLangProperty;
	}
	
	/**
	 * 绑定定时器事件
	 * 
	 */
	private void bindTimerEvent() {
		if (timer.isRunning()) setDisableTimerSetting(true);
		// 定时器完成事件
		eventHandle = new TimerEventHandle();
		timer.setOnSucceeded(eventHandle);
		// 定时器音量变化量
		if (volumeListener != null) timer.valueProperty().removeListener(volumeListener);
		volumeListener = new TimerVolumeListener();
		timer.valueProperty().addListener(volumeListener);
		// 定时器进度
		if (progressListener != null) timer.messageProperty().removeListener(progressListener);
		progressListener = new TimerProgressListener();
		timer.messageProperty().addListener(progressListener);
	}
	
	/**
	 * 计算定时器选择秒数
	 * 
	 * @return
	 */
	private int getSecond() {
		int h = getTimeH().getSelectionModel().getSelectedIndex();
		int m = getTimeM().getSelectionModel().getSelectedIndex() * 5;
		return h * 3600 + m * 60;
	}
	
	/**
	 * 定时器完成事件
	 * 
	 * @author Yeyu
	 *
	 */
	private class TimerEventHandle implements EventHandler<WorkerStateEvent> {
		public void handle(WorkerStateEvent event) {
			if (!isCancelAction) {
				mainVolume.setValue(getToVolume().getValue());
				if (getEnd().getSelectionModel().getSelectedIndex() == 1) { // 暂停
					player.pause();
				}
			}
			getTimerBar().reset(getSecond());
			setDisableTimerSetting(false);
		}
	}
	
	/**
	 * 定时器进度
	 * 
	 * @author Yeyu
	 *
	 */
	private class TimerProgressListener implements ChangeListener<String> {
		
		private int second = getSecond();
		
		public void changed(ObservableValue<? extends String> obs, String o, String time) {
			if (time != null) {
				getTimerBar().setValue((second - Double.parseDouble(time)) / second, second - Integer.parseInt(time));
			}
		}
	}
	
	/**
	 * 定时器音量调整事件
	 * 
	 * @author Yeyu
	 *
	 */
	private class TimerVolumeListener implements ChangeListener<Number> {
		
		public void changed(ObservableValue<? extends Number> obs, Number o, Number volume) {
			if (volume != null) mainVolume.setValue(mainVolume.getValue() - volume.doubleValue());
			System.out.println(mainVolume.getValue());
		}
	}

	/**
	 * 检查版本更新，从夜雨博客数据库获取版本号
	 * 
	 * @author Yeyu
	 *
	 */
	private class CheckVersion extends Service<String> {
	
		protected Task<String> createTask() {
			return new Task<String>() {
				protected String call() throws Exception {
					Thread.currentThread().setName("Check Version");
					return YeyuUtils.network().sendGet("https://www.imyeyu.net/java/imusic/version", "");
				}
			};
		}
	}
	
	/**
	 * 检测远程控制延时
	 * 
	 * @author Yeyu
	 *
	 */
	private class CheckIC extends Service<Number> {
		
		private String ip;
		private boolean isShutdown = false;
		
		public CheckIC(String ip) {
			this.ip = ip;
		}
		
		protected Task<Number> createTask() {
			return new Task<Number>() {
				protected Number call() throws Exception {
					Thread.currentThread().setName("Check Internet Connect");
					while (!isShutdown) {
						updateValue(YeyuUtils.network().pingHostByCMD(ip));
						Thread.sleep(4000);
					}
					return null;
				}
			};
		}
		
		public void setIP(String ip) {
			this.ip = ip;
		}
		
		public void shutdown() {
			this.isShutdown = true;
		}
	}
}