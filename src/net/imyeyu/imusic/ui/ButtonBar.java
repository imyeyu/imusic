package net.imyeyu.imusic.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import net.imyeyu.util.gui.BorderX;

public class ButtonBar extends FlowPane {
	
	private Button[] btns;
	
	private void init(Button... button) {
		this.btns = button;
		HBox box = new HBox();
		BorderX border = new BorderX("#B5B5B5", BorderX.SOLID, 1);
		border.setRadius(0, false);
		
		Insets iconPadding = new Insets(4, 8, 4, 24);
		for (int i = 0; i < button.length; i++) {
			button[i].setBorder(new BorderX("#B5B5B5", BorderX.SOLID, 1).right());
			if (button[i].getStyle().equals("")) {
				button[i].setStyle("-fx-background-insets: 0");
			} else {
				if (!button[i].getText().equals("")) button[i].setPadding(iconPadding);
			}
		}
		box.getChildren().addAll(button);
		
		setAlignment(Pos.CENTER_LEFT);
		setPrefWidth(Region.USE_PREF_SIZE);
		getChildren().add(box);
	}
	
	public int length() {
		return btns.length;
	}

	public void add(Button button) {
		init(button);
	}
	
	public void addAll(Button... button) {
		init(button);
	}
}