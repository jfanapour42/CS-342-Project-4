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
// The GuiServer creates the initial selection window and then creates the appropriate
// scene for the server or client window.
//

import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application{

	Button serverChoice,clientChoice;
	HashMap<String, Scene> sceneMap;
	BorderPane startPane;
	HBox buttonBox;
	Scene startScene;
	
	ListView<String> listItems;
	
	Server serverConnection;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("The Networked Client/Server GUI Example");
		
		this.serverChoice = new Button("Server");
		this.serverChoice.setStyle("-fx-pref-width: 300px");
		this.serverChoice.setStyle("-fx-pref-height: 300px");
		
		this.serverChoice.setOnAction(e->{ primaryStage.setScene(sceneMap.get("server"));
											primaryStage.setTitle("This is the Server");
				serverConnection = new Server(data -> {
					Platform.runLater(()->{
						listItems.getItems().add(data.toString());
					});

				});							
		});
		
		
		this.clientChoice = new Button("Client");
		this.clientChoice.setStyle("-fx-pref-width: 300px");
		this.clientChoice.setStyle("-fx-pref-height: 300px");
		
		this.clientChoice.setOnAction(e-> {
			primaryStage.setScene(sceneMap.get("client"));
			primaryStage.setTitle("This is a client");
		});
		
		this.buttonBox = new HBox(400, serverChoice, clientChoice);
		startPane = new BorderPane();
		startPane.setPadding(new Insets(70));
		startPane.setCenter(buttonBox);
		
		startScene = new Scene(startPane, 800,800);
		
		sceneMap = new HashMap<String, Scene>();
		
		listItems = new ListView<String>();
		
		sceneMap.put("server",  createServerGui());
		sceneMap.put("client",  createClientGui());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		
		primaryStage.setScene(startScene);
		primaryStage.show();
	}
	
	public Scene createServerGui() {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");
		
		pane.setCenter(listItems);
	
		return new Scene(pane, 500, 400);
	}
	
	public Scene createClientGui() {
		Scene s1 = null;
		try {
            // Read file fxml and draw interface.
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/WelcomeScene.fxml"));
            s1 = new Scene(root);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
		return s1;
	}

}
