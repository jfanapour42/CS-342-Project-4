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
// The client class allows the GUI to send data to the server.
// It also gets data from the server and sends it to the GUI via
// consumer callback
//

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;



public class Client extends Thread {
	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	User user;
	ArrayList<String> otherUsers;
	
	private Consumer<Serializable> callback;
	
	Client() {
		otherUsers = new ArrayList<>();
	}
	
	Client(Consumer<Serializable> call){
		callback = call;
		otherUsers = new ArrayList<>();
	}
	
	public void setCallBack(Consumer<Serializable> call) {
		callback = call;
	}
	
	public boolean connect() {
		try {
			socketClient= new Socket("127.0.0.1",5555);
		    out = new ObjectOutputStream(socketClient.getOutputStream());
		    in = new ObjectInputStream(socketClient.getInputStream());
		    socketClient.setTcpNoDelay(true);
		    return true;
		} catch(Exception e) {
			socketClient = null;
			out = null;
			in = null;
			return false;
		}
	}
	
	public boolean login(String username) {
		DataPacket<String> loginRequest = new DataPacket<>(-1); //TODO change to 6
		DataPacket<String> loginResult;
		loginRequest.addData(username);
		send(loginRequest);
		
		loginResult = receive();
		String loginResultStr = loginResult.getData().get(0);
		boolean loggedIn = loginResultStr.equals("login successful");
		user = new User(username);
		return loggedIn;
	}
	
	public void send(DataPacket<String> data) {
		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DataPacket<String> receive() {
		DataPacket<String> message;
		try {
			message = (DataPacket<String>) in.readObject();
		} catch (Exception e) {
			message = null;
		}
		return message;
	}
	
	public void run() {
		while(true) {
			DataPacket<String> message = receive();
			//processData(message);
			callback.accept(message);
		}
    }
	/*
	private void processData(DataPacket<String> data) {
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
	}
	
	private void getChatMessage(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String chatName = dataList.get(0);
		String message = dataList.get(1);
		Chat chat = user.getChat(chatName);
		chat.appendLog(message);
	}
	
	private void getNewUser(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String userName = dataList.get(0);
		otherUsers.add(userName);
	}
	
	private void removeUser(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String userName = dataList.get(0);
		otherUsers.remove(userName);
	}
	
	private void getNewChat(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		ArrayList<String> users = new ArrayList<>(dataList.subList(1, dataList.size()));
		String chatName = dataList.get(0);
		user.addChat(chatName, new Chat(chatName, users));
	}
	
	private void getNewChatName(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		String oldChatName = dataList.get(0);
		String newChatName = dataList.get(1);
		user.changeChatName(oldChatName, newChatName);
	}
	
	private void getOnlineClients(DataPacket<String> data) {
		ArrayList<String> dataList = data.getData();
		otherUsers = dataList;
		String chat = "Universal Chat";
		user.addChat(chat, new Chat(chat, otherUsers));
	}*/
}
