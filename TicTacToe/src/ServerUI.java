import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;


public class ServerUI extends JFrame{
	private static final long serialVersionUID = -6625037986217386003L;
	private JTextArea serverStats, clients;
	private JTextField nameInput;
	private JButton btnSubmit, btnStart, btnStop;
	private JPanel userInput, serverButtons, panel;
	private ButtonListener blistener;
	private JLabel text;
	private TimerListener tlistener;
	private Timer tmr;
	private int time;
	TicTacToeServer server;
	public ServerUI() {
		JFrame jf = new JFrame("Tic-Tac-Toe Server UI");
		Container cp = jf.getContentPane();
		cp.setLayout(null);
		server = new TicTacToeServer();
		tlistener = new TimerListener();
		tmr = new Timer(1, tlistener);
		blistener = new ButtonListener();
		serverStats = new JTextArea();
		serverStats.setEditable(false);
		serverStats.setText("Number of Clients: \n\nPort Number: \n\nTime Running: \n");
		serverStats.setPreferredSize(new Dimension(150, 150));
		clients = new JTextArea();
		clients.setEditable(false);
		clients.setText("Clients Connected: \n");
		clients.setPreferredSize(new Dimension(100, 500));
		userInput = new JPanel();
		userInput.setLayout(new BorderLayout());
		userInput.setPreferredSize(new Dimension(150,150));
		text = new JLabel("Enter ID Of Client To Be Removed: ");
		nameInput = new JTextField();
		nameInput.setPreferredSize(new Dimension(100,30));
		btnSubmit = new JButton("Submit");
		btnSubmit.setPreferredSize(new Dimension(100,50));
		btnSubmit.addActionListener(blistener);
		userInput.add(text, BorderLayout.NORTH);
		userInput.add(nameInput, BorderLayout.CENTER);
		userInput.add(btnSubmit, BorderLayout.SOUTH);
		serverButtons = new JPanel();
		serverButtons.setLayout(new BorderLayout());
		serverButtons.setPreferredSize(new Dimension(150,200));
		btnStart = new JButton("Start Server");
		btnStop = new JButton("Stop Server");
		btnStart.setPreferredSize(new Dimension(100,100));
		btnStart.addActionListener(blistener);
		btnStop.setPreferredSize(new Dimension(100,100));
		btnStop.addActionListener(blistener);
		serverButtons.add(btnStart, BorderLayout.NORTH);
		serverButtons.add(btnStop, BorderLayout.SOUTH);
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(150, 500));
		panel.setLayout(new BorderLayout());
		panel.add(serverStats, BorderLayout.NORTH);
		panel.add(userInput, BorderLayout.CENTER);
		panel.add(serverButtons, BorderLayout.SOUTH);
		clients.setBounds(0,0, 250, 500);
		panel.setBounds(250, 0, 250, 500);
		cp.add(clients);
		cp.add(panel);
		jf.pack();
		jf.setVisible(true);
		jf.setSize(520, 540);
	}
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ServerUI s = new ServerUI();
	}
	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if(event.getSource() == btnSubmit) {
				server.removeClient(nameInput.getText());
				nameInput.setText("");
				clients.setText("Clients Connected: \n" + server.getClientList());
			} else if(event.getSource() == btnStart) {
				tmr.start();
				server.main(null);
			} else {
				server.isRunning = false;
				tmr.stop();
				time = 0;
			}
		}
	}
	private class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			clients.setText("Clients Connected: \n" + server.getClientList());
			time++;
			if(time % 1000 == 0) {
				serverStats.setText("Number of Clients: " + server.getNumberOfClients() + "\n\nPort Number: " + server.getPortNumber() + "\n\nTime Running: " + (time/1000) + "s\n");
			}
		}
	}
}
