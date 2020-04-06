import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.*;
import java.util.*;
import javax.swing.Timer;

public class TicTacToeGUI extends JFrame{
	private JButton[][] board;
	private JPanel buttons;
	private JPanel chatPanel, chatArea, userInput;
	private JTextArea textArea, gameStats;
	private JTextField textField;
	private JButton submit;
	private ButtonListener blistener;
	private int player = 1;
	private int[][] intBoard;
	private String name;
	public TicTacToeGUI() {
		JFrame jf = new JFrame("Tic-Tac-Toe");
		Container cp = jf.getContentPane();
		cp.setLayout(new BorderLayout());
		buttons = new JPanel();
		GridLayout gl = new GridLayout(3,3);
		buttons.setLayout(gl);
		name = JOptionPane.showInputDialog("Enter Your Name: ");
		board = new JButton[3][3];
		blistener = new ButtonListener();
		intBoard = new int[3][3];
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
	}
	public static void main(String[] args) {
		TicTacToeGUI t = new TicTacToeGUI();
	}
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == submit) {
				textArea.append(name + ": " + textField.getText() + "\n");
				textField.setText("");
			} else {
				for(int i = 0; i < 3; i++) {
					for(int j = 0; j < 3; j++) {
						if(event.getSource() == board[i][j]) {
							intBoard[i][j] = player;
							if(player == 1) {
								board[i][j].setText("X");
							} else {
								board[i][j].setText("O");
							}
							board[i][j].setEnabled(false);
							TicTacToeUtility t = new TicTacToeUtility();
							if(t.isWin(intBoard, player)) {
								JOptionPane.showMessageDialog(null, "Game Over! Player " + player + " Wins!");
							} else if(t.isTie(intBoard, player)) {
								JOptionPane.showMessageDialog(null, "Game Over! It's A Tie!");
							} else {
								if(player == 1) {
									player = 2;
								} else {
									player = 1;
								}
							}
						}
					}
				}
			}
		}
	}
}
