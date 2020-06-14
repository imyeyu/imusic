package net.imyeyu.imusic.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.util.gui.AnchorPaneX;

public class ViewCover extends Stage {
	
	private ImageView imgView;
	private AnchorPane main;
	private BorderPane imgBox;

	public ViewCover() {
		main = new AnchorPane();
		
		imgView = new ImageView();

		imgBox = new BorderPane();
		AnchorPaneX.def(imgBox);
		DropShadow dropshadow = new DropShadow();
		dropshadow.setRadius(5);
		dropshadow.setOffsetX(0);
		dropshadow.setOffsetY(0);
		dropshadow.setSpread(.05);
		dropshadow.setColor(Color.valueOf("#000000DD"));
		imgBox.setEffect(dropshadow);
		imgBox.setPadding(new Insets(5));
		imgBox.setCenter(imgView);
		
		main.getChildren().addAll(imgBox);
		main.setBackground(Background.EMPTY);
		
		Scene scene = new Scene(main);
		scene.setFill(null);
		getIcons().add(new Image(Entrance.RES_PATH + "coverIcon.png"));
		setScene(scene);
		setWidth(220);
		setHeight(48);
		initStyle(StageStyle.TRANSPARENT);
		setResizable(false);
		show();
	}
	
	public ImageView getImgView() {
		return imgView;
	}
	
	public BorderPane getImgBox() {
		return imgBox;
	}
}