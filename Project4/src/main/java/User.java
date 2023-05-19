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
// User store the essence of client of the messaging application. 
// It stores the name of the user and a hashmap of chats that
// the user is a part of.
//

import java.util.Collection;
import java.util.LinkedHashMap;

public class User {
	private String name;
	private LinkedHashMap<String, Chat> chats;
	
	public User(String name) {
		this.name = name;
		chats = new LinkedHashMap<>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addChat(String name, Chat c) {
		chats.put(name, c);
	}
	
	public Chat getChat(String name) {
		return chats.get(name);
	}
	
	public boolean changeChatName(String oldName, String newName) {
		if (chats.containsKey(oldName)) {
			Chat c = chats.remove(oldName);
			chats.put(newName, c);
			return true;
		}
		return false;
	}
	
	public Chat removeChat(String name) {
		return chats.remove(name);
	}
	
	public Collection<Chat> getChats() {
		return chats.values();
	}
}
