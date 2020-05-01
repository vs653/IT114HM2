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
	private List<Integer> wins = new ArrayList<Integer>();
	private static int specCount = 0;
	private int[][] board = new int[3][3];
	private OnReceiveServer onReceiveListener;
	public void registerListener(OnReceiveServer listener) {
		this.onReceiveListener = listener;
	}
	public void clientConnect(String name) {
		for(int i = 0; i < clients.size(); i++) {
			if(name.equals(clients.get(i).getClientName())) {
				if(onReceiveListener != null) {
					onReceiveListener.onReceivedConnect(name, ids.get(i), wins.get(i));
				}
			}
		}
	}
	public void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		Thread serverThread = new Thread() {
			@Override
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(port);) {
					while(TicTacToeServer.isRunning) {
						try {
							Socket client = serverSocket.accept();
							System.out.println("Client connecting...");
							//Server thread is the server's representation of the client
							ServerThread thread = new ServerThread(client, getServer());
							thread.start();
							//add client thread to list of clients
							clients.add(thread);
							id = getID();
							ids.add(id);
							wins.add(0);
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
		};
		serverThread.start();
	}
	public TicTacToeServer getServer() {
		return this;
	}
	public int getPortNumber() {
		return port;
	}
	public int getNumberOfClients() {
		return clients.size();
	}
	public void stopServer() {
		for(int i = 0; i < ids.size(); i++) {
			removeClient(ids.get(i));
		}
		TicTacToeServer.isRunning = false;
	}
	public void verifyMove(Payload p) {
		if(p.getX() >= 0 && p.getX() < 3 && p.getY() >= 0 && p.getY() < 3) {
			if(board[p.getX()][p.getY()] == 0) {
				board[p.getX()][p.getY()] = Integer.parseInt(p.getPlayer());
			}
		}
		broadcastMove(p);
		checkWinAndTie(p);
	}
	public void checkWinAndTie(Payload p) {
		TicTacToeUtility t = new TicTacToeUtility();
		if(t.isWin(board, Integer.parseInt(p.getPlayer()))) {
			for(int i = 0; i < clients.size(); i++) {
				if(clients.get(i).getClientName().equalsIgnoreCase(p.getName())) {
					wins.set(i, wins.get(i) + 1);
				}
			}
			Payload payload = new Payload();
			payload.setPayloadType(PayloadType.WIN);
			payload.setMessage("Game Over! Player " + p.getPlayer() + " Wins!\n");
			broadcastWin(payload);
			board = new int[3][3];
		} else if(t.isTie(board)) {
			Payload payload = new Payload();
			payload.setPayloadType(PayloadType.WIN);
			payload.setMessage("Game Over! It's A Tie!\n");
			broadcastWin(payload);
			board = new int[3][3];
		}
	}
	public void removeClient(String id) {
		for(int i = 0; i < ids.size(); i++) {
			if(id.equalsIgnoreCase(ids.get(i))) {
				Payload payload = new Payload();
				payload.setPayloadType(PayloadType.FORCEDISCONNECT);
				clients.get(i).send(payload);
				ids.remove(i);
				clients.remove(i);
				wins.remove(i);
			}
		}
		if(onReceiveListener != null) {
			onReceiveListener.onReceivedDisconnect(id);
		}
	}
	public void disconnectPlayer(String name) {
		for(int i = 0; i < clients.size(); i++) {
			if(name.equalsIgnoreCase(clients.get(i).getClientName())) {
				if(onReceiveListener != null) {
					onReceiveListener.onReceivedDisconnect(ids.get(i));
				}
				clients.remove(i);
				ids.remove(i);
				wins.remove(i);
				Payload payload = new Payload();
				payload.setPayloadType(PayloadType.DISCONNECT);
				payload.setMessage("Has Disconnected!");
				broadcast(payload, name);
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
	public synchronized void broadcastWin(Payload payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		Iterator<ServerThread> iter = clients.iterator();
		Iterator<String> iterS = ids.iterator();
		Iterator<Integer> iterW = wins.iterator();
		String gameStatText = "Spectators: " + specCount + "\nName: \t\tWins: \n";
		for(int i = 0; i < clients.size(); i++) {
			gameStatText += clients.get(i).getClientName() + " \t\t" + wins.get(i) + " \n";
		}
		payload.setGameStatText(gameStatText);
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			@SuppressWarnings("unused")
			String clientID = iterS.next();
			@SuppressWarnings("unused")
			int clientWin = iterW.next();
			boolean boardSent = client.send(payload);
			if(!boardSent) {
				iterS.remove();
				iter.remove();
				iterW.remove();
				System.out.println("Removed client " + client.getId());
			}
		}
	}
	public synchronized void broadcast(Payload payload, String name) {
		String msg = payload.getMessage();
		payload.setMessage(name + ": " + msg + "\n");
		switch(payload.getPayloadType()) {
		case CONNECT:
			clientConnect(name);
			payload.setBoard(board);
			if(payload.getPlayer().equalsIgnoreCase("S")) {
				specCount += 1;
				payload.setSpecCount(specCount);
			}
			break;
		default:
			break;
		}
		broadcast(payload);
	}
	public synchronized void broadcastMove(Payload payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		Iterator<ServerThread> iter = clients.iterator();
		Iterator<String> iterS = ids.iterator();
		Iterator<Integer> iterW = wins.iterator();
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			@SuppressWarnings("unused")
			String clientID = iterS.next();
			@SuppressWarnings("unused")
			int clientWins = iterW.next();
			boolean boardSent = client.send(payload);
			if(!boardSent) {
				iterS.remove();
				iter.remove();
				iterW.remove();
				System.out.println("Removed client " + client.getId());
			}
		}
	}
	public synchronized void broadcast(Payload payload) {
		System.out.println("Sending message to " + clients.size() + " clients");
		Iterator<ServerThread> iter = clients.iterator();
		Iterator<String> iterS = ids.iterator();
		Iterator<Integer> iterW = wins.iterator();
		while(iter.hasNext()) {
			ServerThread client = iter.next();
			@SuppressWarnings("unused")
			String clientID = iterS.next();
			@SuppressWarnings("unused")
			int clientWins = iterW.next();
			boolean messageSent = client.send(payload);
			if(!messageSent) {
				iterS.remove();
				iter.remove();
				iterW.remove();
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
	}
}
interface OnReceiveServer{
	void onReceivedConnect(String name, String id, int wins);
	void onReceivedDisconnect(String id);
}