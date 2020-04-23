import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class TicTacToeGUI extends JFrame implements OnReceive{
	//private static boolean isRunning = true;
	private static final long serialVersionUID = -6625037986217386003L;
	public static JButton[][] board;
	private JPanel buttons;
	private JPanel chatPanel, chatArea, userInput;
	private JTextArea textArea, gameStats;
	private JTextField textField;
	private JButton submit;
	private ButtonListener blistener;
	private String player;
	private static int[][] intBoard;
	private String name;
	private boolean yourTurn;
	static TicTacToeClient client;
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
		gameStats.setText("Spectators: \nName:\t\tWins:\n");
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
				    System.exit(0);
				  }
		});
	}
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		TicTacToeGUI t = new TicTacToeGUI();
		client = new TicTacToeClient();
		client = TicTacToeClient.connect("127.0.0.1", 3000);
		client.registerListener(t);
		t.callMethod();
	}
	public void callMethod() {
		client.postConnectionData(name, player);
	}
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == submit) {
				client.sendMessage(textField.getText(), player);
				textField.setText("");
			} else {
				if(yourTurn) {
					for(int i = 0; i < 3; i++) {
						for(int j = 0; j < 3; j++) {
							if(event.getSource() == board[i][j]) {
								client.sendMove(i, j, Integer.parseInt(player));
							}
						}
					}
				}
			}
		}
	}
	private void updateTurn() {
		int count = 0;
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				if(board[row][col].getText().equals("X") || board[row][col].getText().equals("O")) {
					count++;
				}
			}
		}
		if(count % 2 == 0) {
			if(player.equals("1")) {
				yourTurn = true;
			} else if(player.equals("2")) {
				yourTurn = false;
			}
		} else {
			if(player.equals("1")) {
				yourTurn = false;
			} else if(player.equals("2")) {
				yourTurn = true;
			}
		}
	}
	private void resetBoard() {
		player = JOptionPane.showInputDialog("Enter 1 For Player 1, 2 For Player 2, S For SPECTATOR: ");
		while(!player.equalsIgnoreCase("1") && !player.equalsIgnoreCase("2") && !player.equalsIgnoreCase("S")) {
			player = JOptionPane.showInputDialog("Enter 1 For Player 1, 2 For Player 2, S For SPECTATOR: ");
		}
		if(player.equals("1")) {
			yourTurn = true;
		} else if(player.equals("2")) {
			yourTurn = false;
		}
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) {
				board[row][col].setText("");
				board[row][col].setEnabled(true);
				if(player.equalsIgnoreCase("S")) {
					board[row][col].removeActionListener(blistener);
				}
			}
		}
	}
	@Override
	public void onReceivedConnect(String msg, String spectators) {
		textArea.append(msg);
		gameStats.setText(spectators);
	}
	@Override
	public void onReceivedMessage(String msg) {
		textArea.append(msg);
	}
	@Override
	public void onReceivedMove(int x, int y, int player) {
		if(player == 1) {
			board[x][y].setText("X");
		} else if(player == 2) {
			board[x][y].setText("O");
		}
		board[x][y].setEnabled(false);
		updateTurn();
	}
	@Override
	public void onReceivedWin(String msg, String gameStatsText) {
		textArea.append(msg);
		gameStats.setText(gameStatsText);
		resetBoard();
	}
}
