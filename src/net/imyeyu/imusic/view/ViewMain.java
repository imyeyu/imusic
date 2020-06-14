package net.imyeyu.imusic.view;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.core.Spectrum;
import net.imyeyu.imusic.ui.ButtonBar;
import net.imyeyu.imusic.ui.InfoPane;
import net.imyeyu.imusic.ui.LyricPane;
import net.imyeyu.imusic.ui.SpecPane;
import net.imyeyu.util.Config;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.AnchorPaneX;
import net.imyeyu.util.gui.BorderX;
import net.imyeyu.util.gui.TipsX;
import net.imyeyu.util.gui.ToolTipsX;
import net.imyeyu.util.interfaces.GUIX;

/**
 * 主窗体视图层
 * 
 * @author Yeyu
 *
 */
public class ViewMain {
	
	private Config config = Entrance.getConfig();
	private ResourceBundleX rb = Entrance.getRb();
	
	private Label tips, icLabel, cover, timeNow, timeMax;
	private Slider pb, volume;
	private Button addFiles, addFolder, random, remove, clear, setting, search, info, lrc, spec, prev, toggle, next;
	private InfoPane infoPane;
	private SpecPane specPane;
	private MenuItem cPlayNow, cPlayNext, cRandom, cOpenFolder, cRemove, cClear;
	private LyricPane lyricPane;
	private TextField searchKey;
	private ImageView icIcon;
	private AnchorPane tabPane;
	private ContextMenu menu;
	private ListView<File> list;
	private ComboBox<String> playType;
	
	public ViewMain(Stage stage) {
		// 顶部
		HBox ctrlBtns = new HBox();
		addFiles = new Button(rb.def("addMusic"), new ImageView(Entrance.RES_PATH + "addFiles.png"));
		addFolder = new Button(rb.def("addList"), new ImageView(Entrance.RES_PATH + "addFolder.png"));
		random = new Button(rb.def("random"), new ImageView(Entrance.RES_PATH + "random.png"));
		remove = new Button(rb.def("removeSelected"));
		clear = new Button(rb.def("clear"));
		AnchorPaneX.exRight(ctrlBtns);
		ctrlBtns.setSpacing(6);
		ctrlBtns.getChildren().addAll(addFiles, addFolder, random, remove, clear);
		HBox otherBtns = new HBox();
		tips = new Label();
		tips.maxWidthProperty().bind(stage.widthProperty().subtract(ctrlBtns.widthProperty().add(110)));
		
		setting = new Button(rb.def("setting"), new ImageView(Entrance.RES_PATH + "setting.png"));
		AnchorPaneX.exLeft(otherBtns);
		otherBtns.setSpacing(6);
		otherBtns.setAlignment(Pos.CENTER_RIGHT);
		otherBtns.getChildren().addAll(tips, setting);
		
		AnchorPane top = new AnchorPane();
		top.setPadding(new Insets(0, 0, 8, 0));
		top.getChildren().addAll(ctrlBtns, otherBtns);
		
		// 左侧
		Label listLabel = new Label(rb.def("playList"), new ImageView(Entrance.RES_PATH + "list.png"));
		listLabel.setPadding(new Insets(0, 6, 0, 6));
		AnchorPane searchBox = new AnchorPane();
		searchKey = new TextField();
		searchKey.setStyle("-fx-background-insets: 0");
		searchKey.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).horizontal());
		AnchorPaneX.def(searchKey, 0, 26, 0, 0);
		search = new Button();
		search.setPrefWidth(26);
		YeyuUtils.gui().setBg(search, Entrance.RES_PATH + "search.png", 16, 5, 5);
		AnchorPaneX.exLeft(search);
		searchBox.getChildren().addAll(searchKey, search);
		
		BorderPane leftTop = new BorderPane();
		BorderPane.setAlignment(listLabel, Pos.CENTER);
		leftTop.setPrefHeight(25);
		leftTop.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).bottom());
		leftTop.setLeft(listLabel);
		leftTop.setCenter(searchBox);
		
		// 列表
		list = new ListView<>();
		list.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
			public ListCell<File> call(ListView<File> param) {
				return new ListCell<File>() {
					protected void updateItem(File item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							Label fileName = new Label(item.getName());
							fileName.setTooltip(new ToolTipsX(item.getName()));
							setGraphic(fileName);
						} else {
							setGraphic(null);
						}
					}
				};
			}
		});
		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// 列表右键菜单
		menu = new ContextMenu();
		cPlayNow = new MenuItem(rb.def("play"), new ImageView(Entrance.RES_PATH + "play.png"));
		cPlayNext = new MenuItem(rb.def("playNext"));
		cRandom = new MenuItem(rb.def("random"));
		cOpenFolder = new MenuItem(rb.def("showInExplorer"), new ImageView(Entrance.RES_PATH + "folder.png"));
		cRemove = new MenuItem(rb.def("remove"), new ImageView(Entrance.RES_PATH + "delete.png"));
		cClear = new MenuItem(rb.def("clear"));
		menu.getItems().addAll(cPlayNow, cPlayNext, cRandom, cOpenFolder, new SeparatorMenuItem(), cRemove, cClear);
		list.setContextMenu(menu);
		
		BorderPane left = new BorderPane();
		left.setTop(leftTop);
		left.setMinWidth(220);
		left.setMaxWidth(360);
		left.setCenter(list);
		
		// 播放信息的顶部
		ButtonBar tabBtns = new ButtonBar();
		info = new Button(rb.def("info"), new ImageView(Entrance.RES_PATH + "cd.png"));
		lrc = new Button(rb.def("lyric"), new ImageView(Entrance.RES_PATH + "lyric.png"));
		spec = new Button(rb.def("spec"), new ImageView(Entrance.RES_PATH + "spectrum.png"));
		tabBtns.addAll(info, lrc, spec);
		
		icIcon = new ImageView(Entrance.RES_PATH + "ic_default_0.png");
		icIcon.setCursor(Cursor.HAND);
		icLabel = new Label(rb.def("icCtrl"), icIcon);
		icLabel.setCursor(Cursor.HAND);
		icLabel.setTextFill(GUIX.GRAY);
		BorderPane.setAlignment(icLabel, Pos.CENTER);
		
		BorderPane centerTop = new BorderPane();
		centerTop.setPadding(new Insets(0, 6, 0, 0));
		centerTop.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).bottom());
		centerTop.setRight(icLabel);
		centerTop.setCenter(tabBtns);
		
		// 切换面板和封面
		GridPane musicBox = new GridPane();
		
		infoPane = new InfoPane();       // 歌曲信息
		lyricPane = new LyricPane();     // 歌词
		specPane = new SpecPane();       // 频谱
		
		// 播放信息切换面板
		tabPane = new AnchorPane();
		tabPane.getChildren().add(infoPane);
		
		// 封面
		AnchorPane coverBox = new AnchorPane();
		DropShadow dropshadow = new DropShadow();
		dropshadow.setRadius(6);
		dropshadow.setOffsetX(0);
		dropshadow.setOffsetY(0);
		dropshadow.setSpread(.05);
		dropshadow.setColor(Color.valueOf("#0006"));
		cover = new Label();
		cover.setCursor(Cursor.HAND);
		BackgroundSize bs = new BackgroundSize(-1, -1, true, true, true, true);
		Image img = new Image(Entrance.RES_PATH + "cover.png", -1, -1, true, true);
		BackgroundImage bgImg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
		cover.setBackground(new Background(bgImg));
		cover.setEffect(dropshadow);
		AnchorPaneX.def(cover, 8);
		coverBox.getChildren().add(cover);
		
		ColumnConstraints colPane = new ColumnConstraints();
		colPane.setPrefWidth(80);
		colPane.setMaxWidth(560);
		GridPane.setHgrow(tabPane, Priority.SOMETIMES);
		GridPane.setHgrow(coverBox, Priority.SOMETIMES);
		GridPane.setVgrow(tabPane, Priority.ALWAYS);
		GridPane.setVgrow(coverBox, Priority.ALWAYS);
		musicBox.getColumnConstraints().addAll(colPane);
		musicBox.addColumn(0, tabPane);
		musicBox.addColumn(1, coverBox);
		
		// 播放控制
		HBox playBtns = new HBox();
		Border playBorder = new BorderX("#B5B5B5", BorderX.SOLID, 1).def();
		prev = new Button();
		prev.setBorder(playBorder);
		prev.setPrefWidth(28);
		YeyuUtils.gui().setBg(prev, Entrance.RES_PATH + "prev.png", 16, 5, 5);
		toggle = new Button();
		toggle.setBorder(playBorder);
		toggle.setPrefWidth(28);
		YeyuUtils.gui().setBg(toggle, Entrance.RES_PATH + "play.png", 16, 6, 5);
		next = new Button();
		next.setBorder(playBorder);
		next.setPrefWidth(28);
		YeyuUtils.gui().setBg(next, Entrance.RES_PATH + "next.png", 16, 7, 5);
		playBtns.setSpacing(6);
		playBtns.getChildren().addAll(prev, toggle, next);
		
		// 进度
		BorderPane pbBox = new BorderPane();
		timeNow = new Label("00:00");
		BorderPane.setAlignment(timeNow, Pos.CENTER);
		pb = new Slider();
		pb.setPadding(new Insets(0, 4, 0, 4));
		timeMax = new Label("00:00");
		BorderPane.setAlignment(timeMax, Pos.CENTER);
		pbBox.setPadding(new Insets(0, 8, 0, 8));
		pbBox.setLeft(timeNow);
		pbBox.setCenter(pb);
		pbBox.setRight(timeMax);
		// 音量和播放模式
		HBox otherCtrl = new HBox();
		ImageView volumeIco = new ImageView(Entrance.RES_PATH + "volume.png");
		volume = new Slider();
		volume.setValue(config.getDouble("volume"));
		volume.setPrefWidth(52);
		playType = new ComboBox<>();
		playType.getItems().addAll(rb.def("repeatList"), rb.def("repeatMusic"));
		playType.getSelectionModel().select(config.getInt("repeatMode"));
		otherCtrl.setSpacing(6);
		otherCtrl.setAlignment(Pos.CENTER_RIGHT);
		otherCtrl.getChildren().addAll(volumeIco, volume, playType);
		
		BorderPane centerBottom = new BorderPane();
		centerBottom.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).top());
		centerBottom.setPadding(new Insets(6));
		centerBottom.setLeft(playBtns);
		centerBottom.setCenter(pbBox);
		centerBottom.setRight(otherCtrl);
		
		// 中间布局面板
		BorderPane center = new BorderPane();
		center.setTop(centerTop);
		center.setCenter(musicBox);
		center.setBottom(centerBottom);
		
		// 中间分割面板
		SplitPane splitPane = new SplitPane();
		splitPane.setDividerPosition(0, .2);
		splitPane.getItems().addAll(left, center);
		
		// 主面板
		BorderPane main = new BorderPane();
		main.setBorder(new BorderX("#C8C8C8", BorderX.SOLID, 1).top());
		main.setPadding(new Insets(6));
		main.setTop(top);
		main.setCenter(splitPane);
		
		Scene scene = new Scene(main);
		scene.getStylesheets().add(this.getClass().getResource(Entrance.RES_PATH + "style.css").toExternalForm());
		stage.getIcons().add(new Image(Entrance.RES_PATH + "icon.png"));
		stage.setScene(scene);
		stage.setTitle("iMusic - 夜雨");
		stage.setMinWidth(780);
		stage.setMinHeight(380);
		stage.setWidth(config.getDouble("width"));
		stage.setHeight(config.getDouble("height"));
		stage.show();
		
		toggle.requestFocus();
	}
	
	/**
	 * 切换面板
	 * 
	 * @param pane 目标面板
	 */
	public void toggleTab(Node pane) {
		if (!tabPane.getChildren().get(0).equals(pane)) {
			tabPane.getChildren().clear();
			tabPane.getChildren().add(pane);
		}
	}
	
	/**
	 * 远程控制提示
	 * 
	 * @param ping      连接延时
	 * @param isAction  远程操作状态
	 */
	public void icTips(int ping, boolean isAction) {
		int level = -1;
		level = 0 <= ping ? 3 : level;
		level = 64 < ping ? 2 : level;
		level = 128 < ping ? 1 : level;
		if (level == -1) {
			YeyuUtils.gui().tips(tips, rb.def("icConnectClose"), 4000, TipsX.ERROR);
			icIcon.setImage(new Image(Entrance.RES_PATH + "ic_error.png"));
			return;
		}
		icIcon.setImage(new Image(Entrance.RES_PATH + "ic_" + (isAction ? "action_" : "default_") + level + ".png"));
	}
	
	/**
	 * 单选播放列表项
	 * 
	 * @param i 列表项下表
	 */
	public void setListSelect(int i) {
		list.getSelectionModel().clearSelection();
		list.getSelectionModel().select(i);
		list.scrollTo(list.getSelectionModel().getSelectedIndex() - 4);
	}
	
	/**
	 * 单选播放列表项
	 * 
	 * @param file 列表项对象
	 */
	public void setListSelect(File file) {
		list.getSelectionModel().clearSelection();
		list.getSelectionModel().select(file);
		list.scrollTo(list.getSelectionModel().getSelectedIndex() - 4);
	}
	
	public Label getTips() {
		return tips;
	}
	
	public Label getICLabel() {
		return icLabel;
	}

	public Label getCover() {
		return cover;
	}

	public Label getTimeNow() {
		return timeNow;
	}

	public Label getTimeMax() {
		return timeMax;
	}

	public Slider getPb() {
		return pb;
	}

	public Slider getVolume() {
		return volume;
	}
	
	public Slider getSpecBoost() {
		return specPane.getBoost();
	}
	
	public Slider getSpecSpacing() {
		return specPane.getSpacing();
	}
	
	public Slider getSpecSpeed() {
		return specPane.getSpeed();
	}
	
	public Slider getSpecBand() {
		return specPane.getBand();
	}

	public Button getAddFiles() {
		return addFiles;
	}

	public Button getAddFolder() {
		return addFolder;
	}

	public Button getRandom() {
		return random;
	}

	public Button getRemove() {
		return remove;
	}

	public Button getClear() {
		return clear;
	}

	public Button getSetting() {
		return setting;
	}

	public Button getSearch() {
		return search;
	}
	
	public Button getInfo() {
		return info;
	}

	public Button getLrc() {
		return lrc;
	}

	public Button getSpec() {
		return spec;
	}
	
	public Button getSpecColor() {
		return specPane.getSpecColor();
	}

	public Button getPrev() {
		return prev;
	}

	public Button getToggle() {
		return toggle;
	}

	public Button getNext() {
		return next;
	}
	
	public MenuItem getcPlayNow() {
		return cPlayNow;
	}

	public MenuItem getcPlayNext() {
		return cPlayNext;
	}

	public MenuItem getcRandom() {
		return cRandom;
	}
	
	public MenuItem getcOpenFolder() {
		return cOpenFolder;
	}

	public MenuItem getcRemove() {
		return cRemove;
	}

	public MenuItem getcClear() {
		return cClear;
	}

	public Spectrum getSpectrum() {
		return specPane.getSpectrum();
	}
	
	public InfoPane getInfoPane() {
		return infoPane;
	}
	
	public SpecPane getSpecPane() {
		return specPane;
	}

	public LyricPane getLyricPane() {
		return lyricPane;
	}

	public TextField getSearchKey() {
		return searchKey;
	}
	
	public AnchorPane getTabPane() {
		return tabPane;
	}

	public ListView<File> getList() {
		return list;
	}

	public ComboBox<String> getPlayType() {
		return playType;
	}
}