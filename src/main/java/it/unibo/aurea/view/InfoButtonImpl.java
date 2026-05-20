package it.unibo.aurea.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class InfoButtonImpl {
    public Button build() {
        final SVGPath infoIcon = new SVGPath();
        infoIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"
        + "m1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        infoIcon.setFill(Color.web(COLOR_NAME_GOLD));
        infoIcon.setScaleX(INFO_ICON_SCALE);
        infoIcon.setScaleY(INFO_ICON_SCALE);

        final Button btn = new Button();
        btn.setGraphic(infoIcon);
        btn.getStyleClass().add("info-button");
        btn.setOnAction(e -> showRules());
        return btn;
    }

    private void showRules() {
        final Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Tome of Decrees");
        popup.setResizable(false);

        final Label title = new Label("The Royal Decrees");
        title.getStyleClass().add("rules-title");

        final Label body = new Label(rulesText());
        body.setWrapText(true);
        body.getStyleClass().add("rules-body");

        final ScrollPane scroll = new ScrollPane(body);
        scroll.getStyleClass().add("rules-scroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(RULES_POPUP_HEIGHT - 100);

        final Button closeBtn = new Button("Close the Tome");
        closeBtn.getStyleClass().add("counsellor-dismiss");
        closeBtn.setOnAction(e -> popup.close());

        final VBox content = new VBox(12);
        content.setPadding(new Insets(PADDING_NORMAL));
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("counsellor-content");
        content.getChildren().addAll(title, scroll, closeBtn);

        final Scene scene = new Scene(content, RULES_POPUP_WIDTH, RULES_POPUP_HEIGHT);
        final var ss = getClass().getResource("/styles.css");
        if (ss != null) {
            scene.getStylesheets().add(ss.toExternalForm());
        }
        popup.setScene(scene);
        popup.showAndWait();
    }
}
