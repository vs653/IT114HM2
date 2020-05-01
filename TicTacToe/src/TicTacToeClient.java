import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

public class TicTacToeClient {
	Socket server;
	private OnReceive onReceiveListener;
	public void registerListener(OnReceive listener) {
		this.onReceiveListener = listener;
	}
	
	private Queue<Payload> toServer = new LinkedList<Payload>();
	private Queue<Payload> fromServer = new LinkedList<Payload>();
	
	public static TicTacToeClient connect(String address, int port) {
		TicTacToeClient client = new TicTacToeClient();
		client._connect(address, port);
		Thread clientThread = new Thread() {
			@Override
			public void run() {
				try {
					client.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		clientThread.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return client;
	}
	private void _connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void start() throws IOException {
		if(server == null) {
			return;
		}
		try(ObjectInputStream in = new ObjectInputStream(server.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());) {
			//Thread to listen for keyboard input so main thread isn't blocked
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							Payload p = toServer.poll();
							if(p != null) {
								out.writeObject(p);
							} else {
								try {
									Thread.sleep(8);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
					catch(Exception e) {
						System.out.println("Client shutdown");
					}
					finally {
						close();
					}
				}
			};
			inputThread.start();//start the thread
			
			//Thread to listen for responses from server so it doesn't block main thread
			Thread fromServerThread = new Thread() {
				@Override
				public void run() {
					try {
						Payload p;
						//while we're connected, listen for payloads from server
						while(!server.isClosed() && (p = (Payload)in.readObject()) != null) {
							fromServer.add(p);
						}
						System.out.println("Stopping server listen thread");
					} catch (Exception e) {
						if(!server.isClosed()) {
							e.printStackTrace();
							System.out.println("Server closed connection");
						} else {
							System.out.println("Connection closed");
						}
					} finally {
						close();
					}
				}
			};
			fromServerThread.start();//start the thread
			
			Thread payloadProcessor = new Thread(){
				@Override
				public void run() {
					while(!server.isClosed()) {
						Payload p = fromServer.poll();
						if(p != null) {
							processPayload(p);
						}
						else {
							try {
								Thread.sleep(8);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			};
			payloadProcessor.start();
			//Keep main thread alive until the socket is closed
			//initialize/do everything before this line
			while(!server.isClosed()) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops
			//without this, it still waits for input before terminating
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	public void postConnectionData(String name, String clientPlayer) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.CONNECT);
		payload.setMessage(name);
		payload.setPlayer(clientPlayer);
		toServer.add(payload);
	}
	public void sendMessage(String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setMessage(message);
		toServer.add(payload);
	}
	public void sendMove(int x, int y, int player) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MOVE);
		payload.setX(x);
		payload.setY(y);
		payload.setPlayer("" + player);
		toServer.add(payload);
	}
	public void sendDisconnect(String name) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.DISCONNECT);
		payload.setName(name);
		toServer.add(payload);
	}
	private void processPayload(Payload payload) {
		switch(payload.getPayloadType()) {
		case CONNECT:
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedConnect(payload.getMessage(), "Spectators: " + payload.getSpecCount() + "\nName: \t\tWins: \n", payload.getBoard());
			}
			break;
		case DISCONNECT:
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedMessage(payload.getMessage());
			}
			break;
		case MESSAGE:
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedMessage(payload.getMessage());
			}
			break;
		case MOVE:
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedMove(payload.getX(), payload.getY(), Integer.parseInt(payload.getPlayer()));
			}
			break;
		case WIN:
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedWin(payload.getMessage(), payload.getGameStats());
			}
			break;
		case FORCEDISCONNECT:
			if(onReceiveListener != null) {
				onReceiveListener.onReceivedDisconnect();
			}
			break;
		default:
			System.out.println("Unhandled payload type: " + payload.getPayloadType().toString());
			break;
		}
	}
	private void close() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

interface OnReceive{
	void onReceivedConnect(String msg, String spectators, int[][] board);
	void onReceivedMessage(String msg);
	void onReceivedMove(int x, int y, int player);
	void onReceivedWin(String msg, String gameStatsText);
	void onReceivedDisconnect();
}