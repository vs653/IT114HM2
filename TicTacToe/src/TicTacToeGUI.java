import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class TicTacToeGUI extends JFrame{
	//private static boolean isRunning = true;
	private static final long serialVersionUID = -6625037986217386003L;
	static ObjectOutputStream out;
	public JButton[][] board;
	private JPanel buttons;
	private JPanel chatPanel, chatArea, userInput;
	private JTextArea textArea, gameStats;
	private JTextField textField;
	private JButton submit;
	private ButtonListener blistener;
	private String player;
	private int[][] intBoard;
	private String name;
	private boolean yourTurn;
	public Payload p;
	Socket server;
	public TicTacToeGUI() {
		JFrame jf = new JFrame("Tic-Tac-Toe");
		Container cp = jf.getContentPane();
		cp.setLayout(new BorderLayout());
		buttons = new JPanel();
		GridLayout gl = new GridLayout(3,3);
		buttons.setLayout(gl);
		board = new JButton[3][3];
		intBoard = new int[3][3];
		blistener = new ButtonListener();
		name = JOptionPane.showInputDialog("Enter Your Name: ");
		player = JOptionPane.showInputDialog("Enter 1 For Player 1, 2 For Player 2, S For SPECTATOR: ");
		while(!player.equalsIgnoreCase("1") && !player.equalsIgnoreCase("2") && !player.equalsIgnoreCase("S")) {
			player = JOptionPane.showInputDialog("Enter 1 For Player 1, 2 For Player 2, S For SPECTATOR: ");
		}
		if(player.equalsIgnoreCase("S")) {
			for(int row = 0; row < 3; row++) {
				for(int col = 0; col < 3; col++) {
					board[row][col] = new JButton();
					board[row][col].setSize(50,50);
					board[row][col].setFont(new Font("Arial", Font.PLAIN, 70));
					buttons.add(board[row][col]);
					intBoard[row][col] = 0;
				}
			}
		} else {
			if(player.equals("1")) {
				yourTurn = true;
			} else {
				yourTurn = false;
			}
			for(int row = 0; row < 3; row++) {
				for(int col = 0; col < 3; col++) {
					board[row][col] = new JButton();
					board[row][col].setSize(50,50);
					board[row][col].addActionListener(blistener);
					board[row][col].setFont(new Font("Arial", Font.PLAIN, 70));
					buttons.add(board[row][col]);
					intBoard[row][col] = 0;
				}
			}
		}
		gameStats = new JTextArea();
		gameStats.setEditable(false);
		gameStats.setPreferredSize(new Dimension(300, 500));
		gameStats.setText("Spectators: \nName:\tWins:\tLosses:\n");
		chatPanel = new JPanel();
		chatPanel.setPreferredSize(new Dimension(200,200));
		chatPanel.setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setText("");
		chatArea = new JPanel();
		chatArea.setLayout(new BorderLayout());
		chatArea.add(textArea, BorderLayout.CENTER);
		chatArea.setBorder(BorderFactory.createLineBorder(Color.black));
		chatPanel.add(chatArea, BorderLayout.CENTER);
		userInput = new JPanel();
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(100,30));
		submit = new JButton("Submit");
		submit.setPreferredSize(new Dimension(100,30));
		submit.addActionListener(blistener);
		userInput.add(textField);
		userInput.add(submit);
		chatPanel.add(userInput, BorderLayout.SOUTH);
		cp.add(chatPanel, BorderLayout.SOUTH);
		cp.add(buttons, BorderLayout.CENTER);
		cp.add(gameStats, BorderLayout.EAST);
		jf.pack();
		jf.setVisible(true);
		jf.setSize(600, 500);
		jf.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				  	close();
				    System.exit(0);
				  }
		});
	}
	public static void main(String[] args) {
		TicTacToeGUI client = new TicTacToeGUI();
		client.connect("127.0.0.1", 3000);
		try {
			//if start is private, it's valid here since this main is part of the class
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == submit) {
				Payload p = new Payload();
				p.setPayloadType(PayloadType.MESSAGE);
				p.setMessage(textField.getText());
				try {
					out.writeObject(p);
				} catch(Exception e) {
					e.printStackTrace();
				}
				textField.setText("");
			} else {
				if(yourTurn) {
					for(int i = 0; i < 3; i++) {
						for(int j = 0; j < 3; j++) {
							if(event.getSource() == board[i][j]) {
								intBoard[i][j] = Integer.parseInt(player);
								if(player.equals("1")) {
									board[i][j].setText("X");
								} else {
									board[i][j].setText("O");
								}
								board[i][j].setEnabled(false);
								p = new Payload();
								p.setPayloadType(PayloadType.MOVE);
								p.setMove(board);
								try {
									out.writeObject(p);
								} catch(Exception e) {
									e.printStackTrace();
								}
								TicTacToeUtility t = new TicTacToeUtility();
								if(t.isWin(intBoard, Integer.parseInt(player))) {
									JOptionPane.showMessageDialog(null, "Game Over! You Won!");
									Payload payload = new Payload();
									payload.setPayloadType(PayloadType.MESSAGE);
									payload.setMessage("Game Over! Player " + player + " Wins!");
									try {
										out.writeObject(p);
									} catch(Exception e) {
										e.printStackTrace();
									}
									resetBoard();
								} else if(t.isTie(intBoard)) {
									JOptionPane.showMessageDialog(null, "Game Over! It's A Tie!");
									Payload payload = new Payload();
									payload.setPayloadType(PayloadType.MESSAGE);
									payload.setMessage("Game Over! It's A Tie!");
									try {
										out.writeObject(p);
									} catch(Exception e) {
										e.printStackTrace();
									}
									resetBoard();
								}
							}
						}
					}
				}
			}
		}
	}
	public void connect(String address, int port) {
		try {
			server = new Socket(address, port);
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
		try(Scanner sc = new Scanner(System.in);
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());) {
			out = new ObjectOutputStream(server.getOutputStream());
			while(!server.isClosed() && name != null && name.length() == 0);
			Payload p = new Payload();
			p.setPayloadType(PayloadType.CONNECT);
			p.setMessage(name);
			out.writeObject(p);
			//Thread to listen for keyboard input so main thread isn't blocked
			Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							String line = sc.nextLine();
							if(!"quit".equalsIgnoreCase(line) && line != null) {
								
							}
							else {
								textArea.append("Stopping input thread\n");
								//we're quitting so tell server we disconnected so it can broadcast
								Payload p = new Payload();
								p.setPayloadType(PayloadType.DISCONNECT);
								p.setMessage("bye");
								out.writeObject(p);
								break;
							}
						}
					}
					catch(Exception e) {
						textArea.append("Client shutdown\n");
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
						Payload fromServer;
						//while we're connected, listen for payloads from server
						while(!server.isClosed() && (fromServer = (Payload)in.readObject()) != null) {
							//textArea.append(fromServer);
							processPayload(fromServer);
						}
						textArea.append("Stopping server listen thread\n");
					}
					catch (Exception e) {
						if(!server.isClosed()) {
							e.printStackTrace();
							textArea.append("Server closed connection\n");
						}
						else {
							textArea.append("Connection closed\n");
						}
					}
				}
			};
			fromServerThread.start();//start the thread
			
			//Keep main thread alive until the socket is closed
			//initialize/do everything before this line
			while(!server.isClosed()) {
				Thread.sleep(50);
			}
			textArea.append("Exited loop\n");
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
	private void processPayload(Payload payload) {
		switch(payload.getPayloadType()) {
		case CONNECT:
			textArea.append(payload.getMessage());
			break;
		case DISCONNECT:
			textArea.append(payload.getMessage());
			break;
		case MESSAGE:
			textArea.append(payload.getMessage());
			break;
		case MOVE:
			//JOptionPane.showMessageDialog(null, "Made It Here");
			updateBoard(payload.getMove());
			break;
		default:
			textArea.append("Unhandled payload type: " + payload.getPayloadType().toString());
			break;
		}
	}
	private void close() {
		if(out != null) {
			Payload p = new Payload();
			p.setPayloadType(PayloadType.DISCONNECT);
			p.setMessage("Has Disconnected!");
			try {
				out.writeObject(p);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void updateBoard(JButton[][] board1) {
		int count = 0;
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				this.board[row][col].setText(board1[row][col].getText());
				this.board[row][col].setEnabled(board1[row][col].isEnabled());
				if(board1[row][col].getText().equals("X")) {
					this.intBoard[row][col] = 1;
					//this.board[row][col].setText("X");
					//this.board[row][col].setEnabled(false);
					count++;
				} else if(board1[row][col].getText().equals("O")) {
					this.intBoard[row][col] = 2;
					//this.board[row][col].setText("O");
					//this.board[row][col].setEnabled(false);
					count++;
				}
			}
		}
		if(player.equalsIgnoreCase("S")) {
			TicTacToeUtility t = new TicTacToeUtility();
			if(t.isWin(intBoard, 1)) {
				JOptionPane.showMessageDialog(null, "Game Over! Player 1 Wins!");
				resetBoard();
			} else if(t.isWin(intBoard, 2)) {
				JOptionPane.showMessageDialog(null, "Game Over! Player 2 Wins!");
				resetBoard();
			} else if(t.isTie(intBoard)) {
				JOptionPane.showMessageDialog(null, "Game Over! It's a Tie!");
				resetBoard();
			}
		} else {
			if(count % 2 == 0) {
				if(player.equals("1")) {
					yourTurn = true;
				} else {
					yourTurn = false;
				}
			} else {
				if(player.equals("1")) {
					yourTurn = false;
				} else {
					yourTurn = true;
				}
			}
			TicTacToeUtility t = new TicTacToeUtility();
			if(player.equals("1")) {
				if(t.isWin(intBoard, 2)) {
					JOptionPane.showMessageDialog(null, "Game Over! Sorry You Lost!");
					resetBoard();
				}
			} else {
				if(t.isWin(intBoard, 1)) {
					JOptionPane.showMessageDialog(null, "Game Over! Sorry You Lost!");
					resetBoard();
				}
			}
		}
	}
	private void resetBoard() {
		player = JOptionPane.showInputDialog("Enter 1 For Player 1, 2 For Player 2, S For SPECTATOR: ");
		if(player.equals("1")) {
			yourTurn = true;
		} else {
			yourTurn = false;
		}
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				board[row][col].setText("");
				board[row][col].setEnabled(true);
				intBoard[row][col] = 0;
			}
		}
	}
}
