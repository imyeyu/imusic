package net.imyeyu.imusic.ui;

import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.LyricCell;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.BorderX;

/**
 * 歌词编辑器列表项
 * 
 * @author Yeyu
 *
 */
public class LyricEditorList extends ListView<LyricCell> {

	private Insets test = new Insets(0);
	private BorderX borderX = new BorderX("#C8C8C8", BorderX.SOLID, 1);
	private Border lrcBorder = borderX.horizontal();
	private Border mainBorder = borderX.bottom();
	private final String timeReg = "^([0-9][0-9]:[0-5][0-9].[0-9][0-9]){0,8}$"; // 时轴验证正则
	private final String timeBaseStyle = ";-fx-background-insets: 0;-fx-background-radius: 0";

	public LyricEditorList(ContextMenu contextMenu) {
		setCellFactory(new Callback<ListView<LyricCell>, ListCell<LyricCell>>() {
			public ListCell<LyricCell> call(ListView<LyricCell> param) {
				return new ListCell<LyricCell>() {
					protected void updateItem(LyricCell item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							// 时轴
							TextField time = new TextField(item.getTime());
							time.textProperty().bindBidirectional(item.getTimeProperty());
							time.textProperty().addListener((tmp, o, newTime) -> {
								item.setVaild(newTime != null && !newTime.equals("") && Pattern.matches(timeReg, newTime));
							});
							item.getVaildProperty().addListener((tmp, o, isVaild) -> {
								time.setStyle("-fx-background-color: " + (isVaild ? "transparent" : "#FF7A9B") + timeBaseStyle);
							});
							time.setPrefSize(64, 26);
							time.setStyle("-fx-background-color: " + (item.isVaild() ? "transparent" : "#FF7A9B") + timeBaseStyle);
							time.setContextMenu(contextMenu);
							time.setOnMouseClicked(event -> getSelectionModel().select(item));
							// 歌词
							TextField data = new TextField(item.getLyric());
							item.getLyricProperty().bind(data.textProperty());
							data.setBorder(lrcBorder);
							data.setPrefHeight(26);
							data.setBackground(Background.EMPTY);
							data.setStyle("-fx-background-insets: 0");
							data.setOnMouseClicked(event -> getSelectionModel().select(item));
							// 移除
							Button remove = new Button();
							remove.setMinSize(24, 16);
							remove.setPrefSize(24, 16);
							remove.setOnAction(event -> {
								getSelectionModel().select(item);
								getItems().remove(getSelectionModel().getSelectedIndex());	
							});
							YeyuUtils.gui().setBgTp(remove, Entrance.RES_PATH + "close.png", 16, 4, 0);
							// 容器
							BorderPane main = new BorderPane();
							BorderPane.setMargin(time, test);
							BorderPane.setAlignment(remove, Pos.CENTER);
							main.setBorder(mainBorder);
							main.setLeft(time);
							main.setCenter(data);
							main.setRight(remove);
							this.setGraphic(main);
						} else {
							this.setGraphic(null);
						}
					}
				};
			}
		});
	}
}