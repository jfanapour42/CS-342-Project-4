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
// ClientController controls the main GUI for the client application.
// It allows the user to sign in, see other online clients, create
// group chats and send messages to those group chats.
//

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ClientController implements Initializable{
	
	@FXML
	private VBox root;
	
	@FXML
	private static BorderPane root2;
	
	@FXML
	private ListView<String> userList;
	
	@FXML
	private ListView<Button> chatList;
	
	@FXML
	private ListView<String> chatView;
	
    @FXML
    private TextField messageField;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private Text loginStatusText;
    
    @FXML
    private Text usernameText;
    
    @FXML
    private Text chatText;
    
    @FXML
    private Button loginBtn;
    
    @FXML
    private Button sendBtn;
    
    private Button addChatBtn;
    private Button currChatBtn;
    
    
    static HashMap<Button, String> buttonMap;
    static HashMap<String, ListView<String>> chatMap;
    
    static Client client;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		// if it is the first time initializing, create the client and connect to the server
		if (client == null) {
			client = new Client();
			client.connect();
		}
	}
	
	public void setUpChatScene() throws IOException{
		buttonMap = new HashMap<>();
		chatMap = new HashMap<>();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ChatScene.fxml"));
		root2 = loader.load(); //load view into parent
        ClientController myctr = loader.getController();//get controller created by FXMLLoader
        
        myctr.setUpClient();
        root2.getStylesheets().add("/Styles/ChatScene.css");//set style
        root.getScene().setRoot(root2);//update scene graph
	}
	
	public void setUpClient() {
		client.setCallBack(input -> {
			Platform.runLater(() -> {
				PauseTransition p = new PauseTransition(Duration.seconds(0.25));
				p.setOnFinished(e -> {
					DataPacket<String> data = (DataPacket<String>) input;
					int id = data.getIdentifier();
					switch(id) {
					case 1: getChatMessage(data);
							break;
					case 2: getNewUser(data);
							break;
					case 3: removeUser(data);
							break;
					case 4: getNewChat(data);
							break;
					case 5: getNewChatName(data);
							break;
					case 8: getOnlineClients(data);
							break;
					default: break;
				}
				});
				p.play();
			});
		});
		client.start();
		usernameText.setText(client.user.getName());
	}
	
	public void login(ActionEvent e) throws IOException {
		String username = usernameField.getText();
		boolean loggedIn = false;
		if (!username.equals("")) {
			loggedIn = client.login(username);
			if (loggedIn) {
				setUpChatScene();
			} else {
				loginStatusText.setText("Login failed.");
			}
		} else {
			loginStatusText.setText("Please enter a username");
		}
	}
	
	public void getMessage(ActionEvent e) throws IOException {
		processGetMessage();
	}
	
	public void getMessageOnEnter(KeyEvent e) throws IOException {
		if(e.getCode() == KeyCode.ENTER) {
			processGetMessage();
		}
	}
	
	private void processGetMessage() {
		String chatName = buttonMap.get(currChatBtn);
		String message = client.user.getName() + ": ";
		message = message + messageField.getText();
		messageField.clear();
		DataPacket<String> data = new DataPacket<String>(1);
		data.addData(chatName);
		data.addData(message);
		client.send(data);
	}
	
	private void getChatMessage(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String chatName = dataList.get(0);
		String message = dataList.get(1);
		ListView<String> chat = chatMap.get(chatName);
		chat.getItems().add(message);
	}
	
	private void getNewUser(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String userName = dataList.get(0);
		userList.getItems().add(userName);
	}
	
	private void removeUser(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String userName = dataList.get(0);
		userList.getItems().remove(userName);
	}
	
	private void getNewChat(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		ArrayList<String> users = new ArrayList<>(dataList.subList(1, dataList.size()));
		String chatName = dataList.get(0);
		
		Button chatButton = new Button(chatName);
		ListView<String> chat = new ListView<>();
		
		chatButton.setOnAction(e -> {
			Button b = (Button) e.getSource();
			changeChatView(buttonMap.get(b));
			b.setStyle("-fx-background-color: yellow;");
			
			currChatBtn.setStyle("");
			currChatBtn = b;
		});
		
		chatList.getItems().add(chatList.getItems().size() - 1, chatButton);
		buttonMap.put(chatButton, chatName);
		chatMap.put(chatName, chat);
		
		if(users.get(users.size() - 1).equals(client.user.getName())) {
			chatButton.setStyle("-fx-background-color: yellow;");
			currChatBtn.setStyle("");
			currChatBtn = chatButton;
			changeChatView(chatName);
		}
	}
	
	private void getNewChatName(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String oldChatName = dataList.get(0);
		String newChatName = dataList.get(1);
		ObservableList<Button> buttons = chatList.getItems();
		Button b = null;
		int idx = 0;
		while (idx < buttons.size()) {
			b = buttons.get(idx);
			if (b.getText().equals(oldChatName)) {
				break;
			}
		}
		b.setText(newChatName);
		ListView<String> chat = chatMap.remove(oldChatName);
		chatMap.put(newChatName, chat);
	}
	
	private void getOnlineClients(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		userList.getItems().addAll(dataList);
		
		addChatButton();
		addUniversalChatButton();
	}
	
	private void addChatButton() {
		ImageView plusPic = new ImageView(new Image("/Images/plus.png"));
		plusPic.setFitHeight(60);
		plusPic.setFitWidth(60);
		plusPic.setPreserveRatio(true);
		
		addChatBtn = new Button();
		addChatBtn.setGraphic(plusPic);
		
		addChatBtn.setOnAction(e -> {
			final Stage popUp = new Stage();
            popUp.initModality(Modality.APPLICATION_MODAL);
            try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddNewChatScene.fxml"));
	    		Parent rootNode = loader.load(); //load view into parent
	            NewChatController myctr = loader.getController();//get controller created by FXMLLoader
	            
	            myctr.addCheckBoxes(userList.getItems());
	            
	            Scene scene = new Scene(rootNode, 400, 300);
	            popUp.setScene(scene);
	            popUp.show();
	            
	            popUp.setOnHidden(new EventHandler<WindowEvent>() {
	                @Override
	                public void handle(WindowEvent t) {
	                	ArrayList<String> names = myctr.getNames();
	                	String chatName = myctr.getChatName();
	                	if (!names.isEmpty()) {
		                	DataPacket<String> data = new DataPacket<>(4);
		                	
		                	names.add(client.user.getName());
		                	data.addData(chatName);
		                	data.addAllData(names);
		                	client.send(data);
	                	}
	                }
	            });
            } catch (Exception ex){
            	System.out.println("FXML failed to lauch popUp window");
            	ex.printStackTrace();
            }
		});
		
		chatList.getItems().add(addChatBtn);
	}
	
	private void addUniversalChatButton() {
		String chat = "Universal Chat";
		Button universalChatBtn = new Button(chat);
		universalChatBtn.setOnAction(e -> {              
			Button b = (Button) e.getSource();
			changeChatView(buttonMap.get(b));
			b.setStyle("-fx-background-color: yellow;");
			
			currChatBtn.setStyle("");
			currChatBtn = b;
		});
		universalChatBtn.setStyle("-fx-background-color: yellow;");
		chatList.getItems().add(0, universalChatBtn);
		buttonMap.put(universalChatBtn, chat);
		currChatBtn = universalChatBtn;
		
		ListView<String> universalChat = new ListView<>();
		chatMap.put(chat, universalChat);
		changeChatView(chat);
	}
	
	private void changeChatView(String chat) {
		chatView.setItems(chatMap.get(chat).getItems());
		chatText.setText(chat);
	}
}
