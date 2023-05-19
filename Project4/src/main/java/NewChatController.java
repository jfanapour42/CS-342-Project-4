// Jordan Fanapour - 672845980
// CS 342 - Project 4
// April 29, 2022
//
// This is a basic GUI server-client program where the user can create
// make multiple running client applications that connect to one server.
// The user can login and immediately see all other clients online.
// Each user has the ability to send messages to the universal chat,
// where every other client can see. Each user can also make group
// chats with one or many individuals and only those individuals
// can see the chat.
//
//
// NewChatController manages the scene of a popup window in ClientController
// The scene allows you to pick from the list of online clients to create a new
// group chat.
//

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NewChatController implements Initializable {
	@FXML
	private VBox root;
	
	@FXML
	private ListView<CheckBox> friendList;
	
    @FXML
    private TextField chatNameField;
    
    @FXML
    private Button createBtn;
    
    private ArrayList<String> names;
    private String chatName;
    
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		names = new ArrayList<>();
		chatName = "";
	}
	
	public ArrayList<String> getNames() {
		return names;
	}
	
	public String getChatName() {
		return chatName;
	}
	
	//
	// When called, it will add the list of strings in users to
	// the listview friendlist as checkboxes.
	//
	public void addCheckBoxes(ObservableList<String> users) {
		ArrayList<CheckBox> boxes = new ArrayList<>();
		for (int i = 0; i < users.size(); i++) {
			CheckBox box = new CheckBox(users.get(i));
			box.setOnAction(e -> {
				String name = box.getText();
				if (box.isSelected()) {
					names.add(name);
				} else {
					names.remove(name);
				}
				
				if (names.size() > 0) {
					createBtn.setDisable(false);
				} else {
					createBtn.setDisable(true);
				}
			});
			boxes.add(box);
		}
		friendList.getItems().addAll(boxes);
	}
	
	//
	// Hides the window
	//
	public void createChat(ActionEvent e) throws IOException {
		String str = chatNameField.getText();
		if (!str.equals("")) {
			chatName = chatNameField.getText();
		}
		Stage stage = (Stage) createBtn.getScene().getWindow();
	    // do what you have to do
	    stage.close();
	}
}
