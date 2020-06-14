package net.imyeyu.imusic.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.util.ResourceBundleX;

public class Confirm extends Stage {
	
	private ResourceBundleX rbx = Entrance.getRb();
	
	private String icon = "warn", title, content, confirmText;
	private Button confirm, deny;
	private boolean showDeny = false;
	
	private void init() {

		BorderPane main = new BorderPane();
		
		FlowPane contentPane = new FlowPane();
		Text text = new Text(content);
		text.setWrappingWidth(210);
		contentPane.setAlignment(Pos.CENTER);
		contentPane.getChildren().add(text);
		
		HBox btnBox = new HBox();
		confirm = new Button(confirmText);
		deny = new Button(rbx.def("no"));
		Button cancel = new Button(rbx.def("cancel"));
		btnBox.setAlignment(Pos.CENTER);
		btnBox.setSpacing(6);
		if (showDeny) {
			btnBox.getChildren().addAll(confirm, deny, cancel);
		} else {
			btnBox.getChildren().addAll(confirm, cancel);
		}
		
		BorderPane.setAlignment(confirm, Pos.CENTER);
		main.setPadding(new Insets(8));
		main.setCenter(contentPane);
		main.setBottom(btnBox);
		
		Scene scene = new Scene(main);
		getIcons().add(new Image(Entrance.RES_PATH + icon + ".png"));
		setScene(scene);
		setTitle(title);
		setWidth(260);
		setHeight(140);
		setResizable(false);
		initModality(Modality.APPLICATION_MODAL);
		show();
		
		confirm.requestFocus();
		
		cancel.setOnAction(event -> close());
	}

	public Confirm(String content) {
		this.title = rbx.def("tips");
		this.content = content;
		this.showDeny = false;
		this.confirmText = rbx.def("yes");
		init();
	}

	public Confirm(String content, String confirmText) {
		this.title = rbx.def("tips");
		this.content = content;
		this.showDeny = false;
		this.confirmText = confirmText;
		init();
	}

	public Confirm(String title, String content, boolean showDeny) {
		this.title = title;
		this.content = content;
		this.showDeny = showDeny;
		this.confirmText = rbx.def("yes");
		init();
	}

	public Confirm(String title, String content, String confirmText, String icon) {
		this.icon = icon;
		this.title = title;
		this.content = content;
		this.showDeny = false;
		this.confirmText = confirmText;
		init();
	}
	
	public void initConfirm(Confirm self, EventHandler<ActionEvent> event) {
		confirm.setOnAction(event);
	}
	
	public void initDeny(Confirm self, EventHandler<ActionEvent> event) {
		deny.setOnAction(event);
	}
}