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
// The Server class host all of the client applications and process
// new incoming clients and their requests.
//

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;


public class Server{
	int count = 1;
	HashMap<String, User> accounts = new HashMap<>();
	Chat universalChat = new Chat("Universal Chat");
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	// dummy object to keep server thread safe.
	private Sync sync;
	
	Server(Consumer<Serializable> call){
		callback = call;
		server = new TheServer();
		sync = new Sync();
		server.start();
	}
	
	//
	// Class used to make server thread safe.
	//
	private class Sync {
		public static final boolean lock = true;
		
		public Sync() {}
	}
	
	//
	// The server sole purpose is to wait for new clients, accept them, put them in a new
	// ClientThread, and wait for a new client.
	//
	public class TheServer extends Thread{
		public void run() {
			try(ServerSocket mysocket = new ServerSocket(5555);){
				System.out.println("Server is waiting for a client!");
				while(true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);
					clients.add(c);
					c.start();
					count++;
			    }
			} catch(Exception e) {
					callback.accept("Server socket did not launch");
			}
		}
	}
	
	//
	// An instance of ClientThread will process and request from the client
	// over the socket connection.
	//
	class ClientThread extends Thread{
		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;
		
		User user;
		private Boolean isAlive = true;
		
		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;	
		}
		
		public void run() {
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);	
			} catch(Exception e) {
				System.out.println("Streams not open");
			}
			// Client will attempt to login
			try {
				boolean loggedIn = false;
				while(!loggedIn) {
					// read input
					DataPacket<String> loginRequest = (DataPacket<String>) in.readObject();
					callback.accept(loginRequest.toString()); // remove
					// process login
					loggedIn = processLogin(loginRequest);
				}
			} catch(Exception e) {
				isAlive = false; // set to false to terminate thread
			}
			// once logged in, send list of online clients.
			loadOnlineUsers();
			callback.accept("new client successfully logged on server: "+user.getName());
			while(isAlive) {
				try {
					// read requests
			    	DataPacket<String> data = (DataPacket<String>) in.readObject();
			    	callback.accept("client: " + count + " sent: " + data.toString());
			    	processData(data);
				} catch(Exception e) {
			    	isAlive = false;
				}
			}
			removeClient();
		}//end of run
		
		//
		// Send data to every client on server
		//
		public void updateClients(DataPacket<String> data) {
			for(int i = 0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				updateClient(t, data);
			}
		}
		
		//
		// Send data to every client on server accept the one associated with this thread.
		//
		public void updateClientsExceptThis(DataPacket<String> data) {
			for(int i = 0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				if (t != this) {
					updateClient(t, data);
				}
			}
		}
		
		//
		// Send data to every client listed in arraylist of users.
		//
		public void updateClientsFromChat(ArrayList<String> users, DataPacket<String> data) {
			for (int i = 0; i < clients.size(); i++) { // TODO make algorithm more efficient (Time complexity is now O(n^2))
				ClientThread t = clients.get(i);
				if (users.contains(t.user.getName())) {
					updateClient(t, data);
				}
			}
		}
		
		//
		// private function to update specific client associated with ClientThread t
		//
		private void updateClient(ClientThread t, DataPacket<String> data) {
			try {
				t.out.writeObject(data);
			} catch(Exception e) {
				t.isAlive = false;
			}
		}
		
		//
		// Removes client from server, updates other clients, and wraps up any data.
		//
		private void removeClient() {
			DataPacket<String> data = new DataPacket<>(3);
			String name = user.getName();
			data.addData(name);
			callback.accept("OOOOPPs...Something wrong with the socket from client: " + name + " ....closing down!");
			synchronized (sync) {
				universalChat.removeUser(name);
				universalChat.appendLog(name + " left the chat.");
				user.removeChat(universalChat.getName());
		    	clients.remove(this);
		    	updateClients(data);
			}
		}
		
		//
		// Process incoming data from client.
		// Each integer specifies a specific request
		//
		private void processData(DataPacket<String> data) {
			int id = data.getIdentifier();
			switch(id) {
				case 1: getChatMessage(data);
						break;
				case 4: getNewChat(data);
						break;
				case 5: getNewChatName(data);
						break;
				default: break;
			}
		}
		
		//
		// Gets chat message from data and send message to other clients
		//
		private void getChatMessage(DataPacket<String> data) {
			ArrayList<String> dataList = data.getData();
			String chatName = dataList.get(0);
			String message = dataList.get(1);
			Chat chat = user.getChat(chatName);
			ArrayList<String> users = chat.getUsers();
			chat.appendLog(message);
			synchronized (sync) {
				updateClientsFromChat(users, data);
			}
		}
		
		//
		// Gets request for new group chat.
		// Makes group chat and adds all users a part of it.
		//
		private void getNewChat(DataPacket<String> data) {
			ArrayList<String> dataList = data.getData();
			ArrayList<String> users = new ArrayList<>(dataList.subList(1, dataList.size()));
			String chatName = dataList.get(0);
			Chat chat = new Chat(chatName, users);
			
			synchronized (sync) {
				for (int i = 0; i < users.size(); i++) {
					User u = accounts.get(users.get(i));
					u.addChat(chatName, chat);
				}
				
				updateClientsFromChat(users, data);
			}
		}
		
		//
		// Gets new name for existing group chat.
		// Updates name and send new name to all users in it.
		//
		private void getNewChatName(DataPacket<String> data) {
			ArrayList<String> dataList = data.getData();
			String oldChatName = dataList.get(0);
			String newChatName = dataList.get(1);
			Chat c = this.user.getChat(oldChatName);
			ArrayList<String> users = c.getUsers();
			
			synchronized (sync) {
				for (int i = 0; i < users.size(); i++) {
					User u = accounts.get(users.get(i));
					u.changeChatName(oldChatName, newChatName);
				}
				
				updateClientsFromChat(users, data);
			}
		}
		
		//
		// Gets login request and passes on processing to other helping functions
		//
		private boolean processLogin(DataPacket<String> login) {
			int id = login.getIdentifier();
			switch(id) {
				case 6: return loginUser(login);
				case 7: return createNewAccount(login);
				case -1: jumpIn(login);
						return true;
				default: return false;
			}
		}
		
		//
		// Logs in user with existing account
		//
		private boolean loginUser(DataPacket<String> login) {
			synchronized (sync) {
				DataPacket<String> loginResult = new DataPacket<>(6);
				String username = login.getData().get(0);
				if (accounts.containsKey(username)) {
					this.user = accounts.get(username);
					loginResult.addData("login successful");
					universalChat.addUser(username);
					universalChat.appendLog(user.getName() + " rejoined the chat.");
					user.addChat(universalChat.getName(), universalChat);
				} else {
					loginResult.addData("login failed");
				}
				updateClient(this, loginResult);
				return this.user != null;
			}
		}
		
		//
		// Creates new account for user
		//
		private boolean createNewAccount(DataPacket<String> login) {
			synchronized (sync) {
				DataPacket<String> loginResult = new DataPacket<>(7);
				String username = login.getData().get(0);
				if (!accounts.containsKey(username)) {
					User user = new User(username);
					accounts.put(username, user);
					this.user = user;
					loginResult.addData("account successfully created");
					universalChat.addUser(username);
					universalChat.appendLog(user.getName() + " is a new user and joined the chat.");
					user.addChat(universalChat.getName(), universalChat);
				} else {
					loginResult.addData("account already exists");
				}
				updateClient(this, loginResult);
				return this.user != null;
			}
		}
		
		//
		// Logs in user regardless of existing account status.
		//
		private void jumpIn(DataPacket<String> login) {
			synchronized (sync) {
				DataPacket<String> loginResult = new DataPacket<>(-1);
				String username = login.getData().get(0);
				if (!accounts.containsKey(username)) {
					User user = new User(username);
					accounts.put(username, user);
					this.user = user;
				} else {
					this.user = accounts.get(username);
				}
				loginResult.addData("login successful");
				universalChat.addUser(user.getName());
				universalChat.appendLog(user.getName() + " is a new user and joined the chat.");
				user.addChat(universalChat.getName(), universalChat);
				updateClient(this, loginResult);
			}
		}
		
		//
		// Sends list of online users to the new client
		//
		private void loadOnlineUsers() {
			synchronized (sync) {
				if (isAlive) {
					DataPacket<String> data = new DataPacket<>(8);
					ArrayList<String> users = new ArrayList<>();
					for (int i = 0; i < clients.size(); i++) {
						ClientThread t = clients.get(i);
						if (t != this) {
							users.add(t.user.getName());
						}
					}
					data.setData(users);
					updateClient(this, data);
					// notify all other clients that new user joined
					DataPacket<String> data2 = new DataPacket<>(2);
					data2.addData(this.user.getName());
					updateClientsExceptThis(data2);
				}
			}
		}
	}//end of client thread
}


	
	

	
