package net.imyeyu.imusic.view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.ShortcutKeys;
import net.imyeyu.imusic.ui.TimerBar;
import net.imyeyu.util.Config;
import net.imyeyu.util.Lang;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.gui.BorderX;
import net.imyeyu.util.gui.LabelTextField;
import net.imyeyu.util.gui.NavButton;
import net.imyeyu.util.interfaces.GUIX;

/**
 * 设置视图层
 * 
 * @author Yeyu
 *
 */
public class ViewSetting extends Stage {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();
	
	private Label icTips, volumeValue, blog, version;
	private Slider toVolume;
	private String versionValue = "1.0.0";
	private Button icReConnect, start, cancel, save, close;
	private CheckBox autoPlay, ic;
	private GridPane generalPane, timerPane;
	private TimerBar timerBar;
	private NavButton general, timer, shortcutKey, about;
	private BorderPane main, shortcutKeyPane, aboutPane;
	private RadioButton playNow, playNext, closeToExit, closeToTray;
	private LabelTextField icIP, icPort;
	private ComboBox<String> lang, timeH, timeM, end;

	public ViewSetting() {
		main = new BorderPane();
		
		// 初始化面板
		generalPane = new GridPane();
		initGeneralPane(generalPane);
		timerPane = new GridPane();
		initTimerPane(timerPane);
		shortcutKeyPane = new BorderPane();
		initShortcutKeyPane(shortcutKeyPane);
		aboutPane = new BorderPane();
		initAboutPane(aboutPane);

		// 导航
		general = new NavButton(rb.def("set.general"), 80, 26);
		timer = new NavButton(rb.def("set.timer"), 80, 26);
		shortcutKey = new NavButton(rb.def("set.keys"), 80, 26);
		about = new NavButton(rb.def("set.about"), 80, 26);
		VBox nav = new VBox();
		nav.setBorder(new BorderX("#B5B5B5", BorderX.SOLID, 1).right());
		nav.getChildren().addAll(general, timer, shortcutKey, about);
		
		// 按钮
		HBox btns = new HBox();
		save = new Button(rb.def("save"));
		close = new Button(rb.def("close"));
		btns.setAlignment(Pos.CENTER_RIGHT);
		btns.setSpacing(6);
		btns.setPadding(new Insets(6));
		btns.setBorder(new BorderX("#B5B5B5", BorderX.SOLID, 1).top());
		btns.getChildren().addAll(save, close);
		
		main.setBorder(new BorderX("#B5B5B5", BorderX.SOLID, 1).top());
		main.setLeft(nav);
		main.setCenter(generalPane);
		main.setBottom(btns);

		Scene scene = new Scene(main);
		scene.getStylesheets().add(this.getClass().getResource(Entrance.RES_PATH + "style.css").toExternalForm());
		getIcons().add(new Image(Entrance.RES_PATH + "setting_icon.png"));
		setTitle(rb.def("setting"));
		setMinWidth(560);
		setMinHeight(340);
		setWidth(560);
		setHeight(340);
		setResizable(false);
		initModality(Modality.APPLICATION_MODAL);
		setScene(scene);
		show();
	}
	
	/**
	 * 通用
	 * 
	 * @param pane
	 */
	private void initGeneralPane(GridPane pane) {
		Label langLabel = new Label(rb.def("set.language"));
		Label startLabel = new Label(rb.def("set.startupLabel"));
		Label dbClicklabLabel = new Label(rb.def("set.dbClick"));
		Label closeEventLabel = new Label(rb.def("set.close"));
		Label icLabel = new Label(rb.def("set.icCtrl"));
		icLabel.setPadding(new Insets(4, 0, 0, 0));
		
		lang = new ComboBox<String>();
		lang.getItems().addAll("English", "简体中文", "繁体中文");
		lang.getSelectionModel().select(Lang.toString(config.getString("language")));
		autoPlay = new CheckBox(rb.def("set.autoPlay"));
		autoPlay.setSelected(config.getBoolean("autoPlay"));
		
		ToggleGroup dbClickGroup = new ToggleGroup();
		playNow = new RadioButton(rb.def("set.playNow"));
		playNow.setSelected(config.getString("dbClickEvent").equals("playNow"));
		playNow.setToggleGroup(dbClickGroup);
		playNext = new RadioButton(rb.def("playNext"));
		playNext.setSelected(config.getString("dbClickEvent").equals("playNext"));
		playNext.setToggleGroup(dbClickGroup);
		HBox dbClickBox = new HBox();
		dbClickBox.setSpacing(8);
		dbClickBox.getChildren().addAll(playNow, playNext);
		
		ToggleGroup closeEventGroup = new ToggleGroup();
		closeToTray = new RadioButton(rb.def("set.closeToTray"));
		closeToTray.setSelected(!config.getBoolean("exitOnClose"));
		closeToTray.setToggleGroup(closeEventGroup);
		closeToExit = new RadioButton(rb.def("set.closeToExit"));
		closeToExit.setSelected(config.getBoolean("exitOnClose"));
		closeToExit.setToggleGroup(closeEventGroup);
		HBox closeEventBox = new HBox();
		closeEventBox.setSpacing(8);
		closeEventBox.getChildren().addAll(closeToTray, closeToExit);
		
		ic = new CheckBox(rb.def("enable"));
		ic.setSelected(config.getBoolean("ic"));
		icTips = new Label(rb.def("set.testing"));
		icTips.setTextFill(GUIX.ORANGE);
		icReConnect = new Button(rb.def("set.reconnect"));
		HBox icSetBox = new HBox();
		icSetBox.setSpacing(8);
		icSetBox.setAlignment(Pos.CENTER_LEFT);
		icSetBox.getChildren().addAll(ic, icTips, icReConnect);
		
		icIP = new LabelTextField(rb.def("set.api"), 128, 0);
		icIP.setText(config.getString("icIP"));
		icPort = new LabelTextField(rb.def("set.port"), 56, 0);
		icPort.setText(config.getString("icPort"));
		HBox icConnectBox = new HBox();
		icConnectBox.setSpacing(8);
		icConnectBox.setAlignment(Pos.CENTER_LEFT);
		icConnectBox.getChildren().addAll(icIP, icPort);
		VBox icBox = new VBox();
		icBox.setSpacing(6);
		icBox.getChildren().addAll(icSetBox, icConnectBox);
		
		ColumnConstraints col = new ColumnConstraints(120);
		col.setHalignment(HPos.RIGHT);
		
		GridPane.setValignment(icLabel, VPos.TOP);
		pane.setVgap(14);
		pane.getColumnConstraints().add(col);
		pane.setPadding(new Insets(16, 0, 16, 0));
		pane.addColumn(0, langLabel, startLabel, dbClicklabLabel, closeEventLabel, icLabel);
		pane.addColumn(1, lang, autoPlay, dbClickBox, closeEventBox, icBox);
	}
	
	/**
	 * 定时
	 * 
	 * @param pane
	 */
	private void initTimerPane(GridPane pane) {
		Label timeLabel = new Label(rb.def("set.time"));
		Label eventLabel = new Label(rb.def("set.event"));
		Label endLabel = new Label(rb.def("set.end"));
		Label ctrlLabel = new Label(rb.def("set.timerLabel"));
		
		timeH = new ComboBox<>();
		for (int i = 0; i < 13; i++) {
			timeH.getItems().add(i + rb.l("set.h"));
		}
		timeH.getSelectionModel().select(config.getInt("timerH"));
		timeM = new ComboBox<>();
		for (int i = 0; i < 12; i++) {
			timeM.getItems().add(i * 5 + rb.l("set.m"));
		}
		timeM.getSelectionModel().select(config.getInt("timerM"));
		HBox timeBox = new HBox();
		timeBox.setSpacing(8);
		timeBox.getChildren().addAll(timeH, timeM);
		Label volumeLow = new Label(rb.def("set.volumeTo"));
		toVolume = new Slider();
		toVolume.setPrefWidth(64);
		toVolume.setValue(config.getDouble("timerVolume"));
		volumeValue = new Label();
		HBox volumeBox = new HBox();
		volumeBox.setSpacing(8);
		volumeBox.setAlignment(Pos.CENTER_LEFT);
		volumeBox.getChildren().addAll(volumeLow, toVolume, volumeValue);
		end = new ComboBox<>();
		end.getItems().addAll(rb.def("set.keep"), rb.def("set.pause"));
		end.getSelectionModel().select(config.getInt("timerEnd"));
		start = new Button(rb.def("set.startup"));
		cancel = new Button(rb.def("cancel"));
		HBox ctrlBox = new HBox();
		ctrlBox.setSpacing(8);
		ctrlBox.getChildren().addAll(start, cancel);
		timerBar = new TimerBar();

		ColumnConstraints col = new ColumnConstraints(120);
		col.setHalignment(HPos.RIGHT);
		
		pane.setVgap(14);
		pane.getColumnConstraints().add(col);
		pane.setPadding(new Insets(16, 0, 16, 0));
		pane.addColumn(0, timeLabel, eventLabel, endLabel, ctrlLabel, new Label());
		pane.addColumn(1, timeBox, volumeBox, end, ctrlBox, timerBar);
	}

	/**
	 * 快捷键说明
	 * 
	 * @param pane
	 */
	private void initShortcutKeyPane(BorderPane pane) {
		TableView<ShortcutKeys> table = new TableView<>();
		TableColumn<ShortcutKeys, String> colAction = new TableColumn<ShortcutKeys, String>(rb.def("set.action"));
		TableColumn<ShortcutKeys, String> colIn = new TableColumn<ShortcutKeys, String>(rb.def("set.in"));
		TableColumn<ShortcutKeys, String> colOut = new TableColumn<ShortcutKeys, String>(rb.def("set.out"));
		colAction.setPrefWidth(240);
		colIn.setPrefWidth(90);
		colOut.setPrefWidth(128);
		colAction.setCellValueFactory(new PropertyValueFactory<ShortcutKeys, String>("action"));
		colIn.setCellValueFactory(new PropertyValueFactory<ShortcutKeys, String>("in"));
		colOut.setCellValueFactory(new PropertyValueFactory<ShortcutKeys, String>("out"));
		table.getColumns().add(colAction);
		table.getColumns().add(colIn);
		table.getColumns().add(colOut);
		table.getItems().addAll(ShortcutKeys.getAll());
		table.setStyle("-fx-padding: 0;-fx-background-insets: 0");
		pane.setCenter(table);
	}
	
	/**
	 * 关于
	 * 
	 * @param pane
	 */
	private void initAboutPane(BorderPane pane) {
		VBox center = new VBox();
		Label name = new Label("iMusic", new ImageView(Entrance.RES_PATH + "logo.png"));
		name.setFont(Font.font("System", FontWeight.BOLD, 24));
		Label cname = new Label(rb.def("set.cname"));
		cname.setFont(Font.font(14));
		version = new Label(versionValue);
		center.setAlignment(Pos.TOP_CENTER);
		center.setSpacing(4);
		center.getChildren().addAll(name, cname, version);
		
		VBox bottom = new VBox();
		// 本项目完全免费开源，你可以增删改查任何代码，但我不希望你从界面移除这个 developer Label
		Label developer = new Label(rb.def("set.developer") + "夜雨");
		blog = new Label(rb.def("set.blog") + "https://www.imyeyu.net");
		blog.setCursor(Cursor.HAND);
		Label cr = new Label("Copyright " + rb.def("set.copyright") + " 夜雨 2020 All Rights Reserved 版权所有");
		bottom.setSpacing(4);
		bottom.setAlignment(Pos.CENTER);
		bottom.getChildren().addAll(developer, blog, cr);
		
		pane.prefWidthProperty().bind(pane.widthProperty());
		pane.prefHeightProperty().bind(pane.heightProperty());
		pane.setPadding(new Insets(16));
		pane.setCenter(center);
		pane.setBottom(bottom);
	}
	
	public void togglePane(Pane pane) {
		this.main.setCenter(pane);
	}
	
	public void setDisableTimerSetting(boolean disable) {
		timeH.setDisable(disable);
		timeM.setDisable(disable);
		toVolume.setDisable(disable);
		end.setDisable(disable);
		start.setDisable(disable);
		cancel.setDisable(!disable);
	}
	
	public Button getSave() {
		return save;
	}

	public Button getClose() {
		return close;
	}

	public GridPane getGeneralPane() {
		return generalPane;
	}

	public GridPane getTimerPane() {
		return timerPane;
	}
	
	public BorderPane getShortcutKeyPane() {
		return shortcutKeyPane;
	}

	public BorderPane getAboutPane() {
		return aboutPane;
	}

	public NavButton getGeneral() {
		return general;
	}

	public NavButton getTimer() {
		return timer;
	}
	
	public NavButton getShortcutKey() {
		return shortcutKey;
	}

	public NavButton getAbout() {
		return about;
	}

	public Label getIcTips() {
		return icTips;
	}
	
	public Label getVolumeValue() {
		return volumeValue;
	}
	
	public Label getBlog() {
		return blog;
	}

	public Label getVersion() {
		return version;
	}

	public Slider getToVolume() {
		return toVolume;
	}

	public String getVersionValue() {
		return versionValue;
	}

	public Button getIcReConnect() {
		return icReConnect;
	}
	
	public Button getStart() {
		return start;
	}
	
	public Button getCancel() {
		return cancel;
	}
	
	public TimerBar getTimerBar() {
		return timerBar;
	}

	public CheckBox getAutoPlay() {
		return autoPlay;
	}

	public CheckBox getIc() {
		return ic;
	}

	public RadioButton getPlayNow() {
		return playNow;
	}

	public RadioButton getPlayNext() {
		return playNext;
	}

	public RadioButton getCloseToExit() {
		return closeToExit;
	}

	public RadioButton getCloseToTray() {
		return closeToTray;
	}

	public LabelTextField getIcIP() {
		return icIP;
	}

	public LabelTextField getIcPort() {
		return icPort;
	}

	public ComboBox<String> getLang() {
		return lang;
	}

	public ComboBox<String> getTimeH() {
		return timeH;
	}

	public ComboBox<String> getTimeM() {
		return timeM;
	}

	public ComboBox<String> getEnd() {
		return end;
	}
}