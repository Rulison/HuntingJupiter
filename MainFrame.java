import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import java.awt.event.*;

import javax.swing.*;

public class MainFrame {
	private static final int DEFAULT_PORT = 33333;
	private static int numObstacles;

	public static void main(String[] args) {

		// First, construct a panel that will be placed into a JOptionPane confirm dialog.

		JLabel message = new JLabel("Hunting Jupiter", JLabel.CENTER);
		message.setFont(new Font("Serif", Font.BOLD, 16));
		numObstacles=60;

		final JTextField listeningPortInput = new JTextField("" + DEFAULT_PORT, 5);
		final JTextField hostInput = new JTextField(30);
		final JTextField connectPortInput = new JTextField("" + DEFAULT_PORT, 5);

		final JRadioButton selectServerMode = new JRadioButton("Start a new game");
		final JRadioButton selectClientMode = new JRadioButton("Connect to existing game");

		ButtonGroup group = new ButtonGroup();
		group.add(selectServerMode);
		group.add(selectClientMode);
		final JComboBox length=new JComboBox();

		ActionListener radioListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == length)
				{
					if(length.getSelectedItem().equals("Short"))
						numObstacles=30;
					else if(length.getSelectedItem().equals("Medium"))
						numObstacles=450;
					else
						numObstacles=720;
				}

				if (e.getSource() == selectServerMode) {
					listeningPortInput.setEnabled(true);
					hostInput.setEnabled(false);
					connectPortInput.setEnabled(false);
					listeningPortInput.setEditable(true);
					hostInput.setEditable(false);
					connectPortInput.setEditable(false);
					length.setEnabled(true);
				}
				else if(e.getSource() == selectClientMode){
					listeningPortInput.setEnabled(false);
					hostInput.setEnabled(true);
					connectPortInput.setEnabled(true);
					listeningPortInput.setEditable(false);
					hostInput.setEditable(true);
					connectPortInput.setEditable(true);
					length.setEnabled(false);
				}
			}
		};
		length.addItem("Short");
		length.addItem("Medium");
		length.addItem("Long");
		length.addActionListener(radioListener);
		selectServerMode.addActionListener(radioListener);
		selectClientMode.addActionListener(radioListener);
		selectServerMode.setSelected(true);
		hostInput.setEnabled(false);
		connectPortInput.setEnabled(false);
		hostInput.setEditable(false);
		connectPortInput.setEditable(false);


		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(0,1,5,5));
		inputPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 2),
				BorderFactory.createEmptyBorder(6,6,6,6) ));

		inputPanel.add(message);

		JPanel row;

		inputPanel.add(selectServerMode);

		row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Listen on port: "));
		row.add(listeningPortInput);
		inputPanel.add(row);

		inputPanel.add(selectClientMode);

		row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));      
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Computer: "));
		row.add(hostInput);
		inputPanel.add(row);

		row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Port Number: "));
		row.add(connectPortInput);
		inputPanel.add(row);


		inputPanel.add(length);


		// Show the dialog, get the user's response and -- if the user doesn't
		// cancel -- start a game.  If the user chooses to run as the server
		// then a SkyHub (server) is created and after that a PokerWindow
		// is created that connects to the server running on  localhost, which was
		// just created.  In that case, the game will wait for a second connection. 
		// If the user chooses to connect to an existing server, then only
		// a PokerWindow is created, that will connect to the specified
		// host where the server is running.

		while (true) {  // Repeats until a game is started or the user cancels.

			int action = JOptionPane.showConfirmDialog(null, inputPanel, "SkyRacer", 
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if (action != JOptionPane.OK_OPTION)
				return;

			if (selectServerMode.isSelected()) {
				int port;
				try {
					port = Integer.parseInt(listeningPortInput.getText().trim());
					if (port <= 0)
						throw new Exception();
				}
				catch (Exception e) {
					message.setText("Illegal port number!");
					listeningPortInput.selectAll();
					listeningPortInput.requestFocus();
					continue;
				}
				try {
					new SkyHub(port,numObstacles);
				}
				catch (Exception e) {
					message.setText("Error: Can't listen on port " + port);
					listeningPortInput.selectAll();
					listeningPortInput.requestFocus();
					continue;
				}
				new SkyFrame("localhost", port, true,numObstacles);
				break;
			}
			else {
				String host;
				int port;
				host = hostInput.getText().trim();
				if (host.length() == 0) {
					message.setText("You must enter a computer name!");
					hostInput.requestFocus();
					continue;
				}
				try {
					port = Integer.parseInt(connectPortInput.getText().trim());
					if (port <= 0)
						throw new Exception();
				}
				catch (Exception e) {
					message.setText("Illegal port number!");
					connectPortInput.selectAll();
					connectPortInput.requestFocus();
					continue;
				}
				new SkyFrame(host,port,false,numObstacles);
				break;
			}
		}
	}
}