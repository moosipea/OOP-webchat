package client.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ChannelList extends VBox {
    private String activeChannel = null;

    public ChannelList() {
        setFillWidth(true);
    }

    public void addChannel(String name) {
        Button btn = new Button(name);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnMouseReleased((e) -> {
            activeChannel = name;
        });
        getChildren().add(btn);

        if (activeChannel == null) {
            activeChannel = name;
        }
    }

    public String getActiveChannel() {
        return activeChannel;
    }
}
