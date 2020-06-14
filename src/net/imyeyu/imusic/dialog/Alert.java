package net.imyeyu.imusic.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.gui.BorderX;

public class Alert extends Stage {
	
	private ResourceBundleX rbx = Entrance.getRb();
	
	public static String WARNING = "warn";
	public static String ERROR = "error";
	
	private String btn, icon, title, content;
	
	private void init() {
		BorderPane main = new BorderPane();

		HBox texts = new HBox();
		Text text = new Text(content);
		text.setWrappingWidth(210);
		texts.setAlignment(Pos.CENTER);
		texts.setPadding(new Insets(8));
		texts.getChildren().add(text);
		
		HBox btns = new HBox();
		Button ok = new Button(btn);
		ok.setPrefWidth(64);
		btns.setBorder(new BorderX("#B5B5B5", BorderX.SOLID, 1).top());
		btns.setAlignment(Pos.CENTER);
		btns.setPadding(new Insets(6));
		btns.getChildren().add(ok);
		
		main.setBorder(new BorderX("#B5B5B5", BorderX.SOLID, 1).top());
		main.setCenter(texts);
		main.setBottom(btns);
		
		Scene scene = new Scene(main);
		getIcons().add(new Image(Entrance.RES_PATH + icon + ".png"));
		setScene(scene);
		setTitle(title);
		setWidth(260);
		setHeight(160);
		setResizable(false);
		initModality(Modality.APPLICATION_MODAL);
		show();

		main.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) close();
		});
		ok.setOnAction(event -> close());
	}
	
	public Alert(String title, String content) {
		this.btn = rbx.def("ok");
		this.icon = WARNING;
		this.title = title;
		this.content = content;
		init();
	}
	
	public Alert(String title, String content, String icon) {
		this.btn = rbx.def("ok");
		this.icon = icon;
		this.title = title;
		this.content = content;
		init();
	}
	
	public Alert(String title, String content, String icon, String btn) {
		this.btn = btn;
		this.icon = icon;
		this.title = title;
		this.content = content;
		init();
	}
}