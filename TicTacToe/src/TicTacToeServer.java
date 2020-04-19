import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TicTacToeServer {
	int port = 3000;
	String id;
	public static boolean isRunning = true;
	private List<ServerThread> clients = new ArrayList<ServerThread>();
	private ArrayList<String> ids = new ArrayList<String>();
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);) {
			while(TicTacToeServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Client connecting...");
					//Server thread is the server's representation of the client
					ServerThread thread = new ServerThread(client, this);
					thread.start();
					//add client thread to list of clients
					clients.add(thread);
					id = getID();
					ids.add(id);
					System.out.println("Client added to clients pool");
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				Thread.sleep(50);
				System.out.println("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public int getPortNumber() {
		return port;
	}
	public int getNumberOfClients() {
		return clients.size();
	}
	public void removeClient(String id) {
		for(int i = 0; i < ids.size(); i++) {
			if(id.equalsIgnoreCase(ids.get(i))) {
				ids.remove(i);
				clients.remove(i);
			}
		}
	}
	public String getID() {
		String characters = "abcdefghijklmnopqrstuvwxyz1234567890";
		String id = "";
		for(int i = 0; i < 3; i++) {
			id += characters.charAt((int)(Math.random() * 35));
		}
		return id;
	}
	public String getClientList() {
		String clientList = "";
		for(int i = 0; i < clients.size(); i++) {
			clientList += "ID: " + ids.get(i) + " Name: " + clients.get(i).getClientName() + "\n";
		}
		return clientList;
	}
	@Deprecated
	int getClientIndexByThreadId(long id) {
		for(int i = 0, l = clients.size(); i < l;i++) {
			if(clients.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}
	public synchronized void broadcast(Payload payload, String name) {
		String msg = payload.getMessage();
		payload.setMessage(name + ": " + msg + "\n");
		broadcast(payload);
	}
	public synchronized void broadcastMove(Payload payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		Iterator<ServerThread> iter = clients.iterator();
		Iterator<String> iterS = ids.iterator();
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			String clientID = iterS.next();
			boolean boardSent = client.send(payload);
			if(!boardSent) {
				iterS.remove();
				iter.remove();
				System.out.println("Removed client " + client.getId());
			}
		}
	}
	public synchronized void broadcast(Payload payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		Iterator<ServerThread> iter = clients.iterator();
		Iterator<String> iterS = ids.iterator();
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			String clientID = iterS.next();
			boolean messageSent = client.send(payload);
			if(!messageSent) {
				iterS.remove();
				iter.remove();
				System.out.println("Removed client " + client.getId());
			}
		}
	}
	//Broadcast given payload to everyone connected
	public synchronized void broadcast(Payload payload, long id) {
		//let's temporarily use the index as the client identifier to
		//show in all client's chat. You'll see why this is a bad idea
		//when clients disconnect/reconnect.
		int from = getClientIndexByThreadId(id);
		String msg = payload.getMessage();
		payload.setMessage(
				//prepending client name to front of message
				(from>-1?"Client[" + from+"]":"unknown") 
				//including original message if not null (with a prepended colon)
				+ (msg != null?": "+ msg:"")
		);
		//end temp identifier (maybe this won't be too temporary as I've reused
		//it in a few samples now)
		broadcast(payload);
		
	}
	//Broadcast given message to everyone connected
	public synchronized void broadcast(String message, long id) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setMessage(message);
		broadcast(payload, id);
	}
	

	public static void main(String[] args) {
		//let's allow port to be passed as a command line arg
		//in eclipse you can set this via "Run Configurations" 
		//	-> "Arguments" -> type the port in the text box -> Apply
		int port = 3000;//make some default
		if(args != null && args.length >= 1) {
			String arg = args[0];
			try {
				port = Integer.parseInt(arg);
			}
			catch(Exception e) {
				//ignore this, we know it was a parsing issue
			}
		}
		System.out.println("Starting Server");
		TicTacToeServer server = new TicTacToeServer();
		System.out.println("Listening on port " + port);
		server.start(port);
		System.out.println("Server Stopped");
	}
}