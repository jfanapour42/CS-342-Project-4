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
// Chat stores the essence of a group chat.
// It stores the chat name, list of users on the group chat
// and everything that is said in the chat (log).
//

import java.util.ArrayList;

public class Chat {
	private String name;
	private ArrayList<String> users;
	private ArrayList<String> log;
	
	public Chat(String name) {
		this.name = name;
		this.users = new ArrayList<>();
		this.log = new ArrayList<>();
	}
	
	public Chat(String name, ArrayList<String> users) {
		this.users = users;
		this.log = new ArrayList<>();
		if (!name.equals("")) {
			this.name = name;
		} else {
			this.name = getDefaultName(users);
		}
	}
	
	public Chat(ArrayList<String> users) {
		this.users = users;
		this.name = getDefaultName(users);
		this.log = new ArrayList<>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<String> getUsers() {
		return this.users;
	}
	
	public ArrayList<String> getLog() {
		return this.log;
	}
	
	public boolean addUser(String u) {
		if (!this.users.contains(u)) {
			this.users.add(u);
			return true;
		}
		return false;
	}
	
	public boolean removeUser(String u) {
		if (this.users.contains(u)) {
			this.users.remove(u);
			return true;
		}
		return false;
	}
	
	public void appendLog(String str) {
		this.log.add(str);
	}
	
	private String getDefaultName(ArrayList<String> users) {
		String str = "";
		int i = 0;
		int size = users.size();
		while (i < size - 1) {
			str = str + users.get(i) + ", ";
			i++;
		}
		if (i != 0) {
			str = str + users.get(size - 1);
		}
		return str;
	}
}
