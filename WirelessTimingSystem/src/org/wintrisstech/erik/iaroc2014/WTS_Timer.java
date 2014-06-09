package org.wintrisstech.erik.iaroc2014;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;

@SuppressWarnings("serial")
public class WTS_Timer extends JPanel implements Runnable, ActionListener {
	private static final String HOST_PREFIX = "192.168.42.";
	private static final int SERVER_PORT = 47047;
	private static final Pattern EVENT_PATTERN = Pattern.compile("(local|remote):(high|low)");
	private static final String LOCAL = "local";
	private static final String REMOTE = "remote";
	private static final String HIGH = "high";
//	private static final String LOW = "low";
	private static final String RESET = "RESET";
	private static final String START = "START";
	private static final String STOP = "STOP";
	private static final String RESUME = "RESUME";
	private static final String NO_TEXT = "---";
	private static final Font displayFont = new Font("Helvetica", Font.PLAIN, 144);

	private String id; // "1" or "2" to identify RPi-1 or RPi-2.
	private Timer ticker = new Timer(20, this);
	// beam states
	private boolean localHigh = false;
	private boolean remoteHigh = false;
	// timer states
	// 3 states, each identified by two boolean variables:
	//  1) armed: armed && !running
	//  2) running: !armed && running
	//  3) stopped: !armed && !running
	
	private boolean running = false;
	private boolean armed = true;
	//
	private long start; // Time of transition from armed to running state
	private boolean manualMode = false; // If true, manual mode, else automatic mode.
	private JButton armButton;
	private JButton manualControlButton;
	private JTextField timeTextField;

	public WTS_Timer(String id) {
		this.id = id;
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1 || !args[0].matches("[1-2]")) {
			printUsage();
			System.exit(1);
		}
		// String host = "localhost";
		WTS_Timer wtsTimer = new WTS_Timer(args[0]);
		SwingUtilities.invokeLater(wtsTimer);
		wtsTimer.listen(HOST_PREFIX + args[0], SERVER_PORT);
	}

	private static void printUsage() {
		System.out.println("Usage: java -jar WirelessTimingSystem.jar N"
				+ "\nN is a number between 1 and 2 identifying the source of events.");
	}

	private void listen(String host, int serverPort) {
		Socket socket = null;
		BufferedReader in = null;
		try {
			System.out.println("Trying to connect to " + host + ":" + serverPort);
			socket = new Socket(host, serverPort);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Connected to " + socket.getRemoteSocketAddress());
			boolean listening = true;
			String line;
			while (listening && (line = in.readLine()) != null) {
				System.out.println(line);
				if (line.toLowerCase().equals("bye")) {
					listening = false;
				} else {
					Matcher m = EVENT_PATTERN.matcher(line);
					if (m.matches()) {
						String source = m.group(1);
						String beamState = m.group(2);
						changeState(source, beamState);
					}
				}
			}
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (socket != null)
					socket.close();
				if (in != null)
					in.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		System.exit(0);
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("Timer Lane " + id);
		setPreferredSize(new Dimension(600, 20));
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.NORTH);
		timeTextField = new JTextField("00:00.00");
		CompoundBorder border = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(),
				BorderFactory.createEmptyBorder(0, 20, 0, 0));
		timeTextField.setBorder(border);
		timeTextField.setFont(displayFont);
		frame.add(timeTextField, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		// Arm button
		armButton = new JButton(RESET);
		armButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				armButton.setSelected(true);
			}
		});
		armButton.setSelected(armed);
		buttonPanel.add(armButton);
		// Manual radio button
		final JRadioButton manualRadioButton = new JRadioButton();
		manualRadioButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				manualMode = manualRadioButton.isSelected();
				if (manualMode) {
					if (armed) {
						manualControlButton.setText(START);
					} else if (running) {
						manualControlButton.setText(STOP);
					} else {
						manualControlButton.setText(RESUME);
					}
				} else {
					manualControlButton.setText(NO_TEXT);
				}
				manualControlButton.setEnabled(manualMode);
			}
		});
		manualRadioButton.setSelected(manualMode);
		buttonPanel.add(manualRadioButton);

		// Manual control button
		manualControlButton = new JButton(NO_TEXT);
		manualControlButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (armed) {
					start();
				} else if (running) {
					stop();
				} else {
					resume();
				}
			}
		});
		manualControlButton.setEnabled(manualMode);
		buttonPanel.add(manualControlButton);

		frame.add(buttonPanel, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		ticker.start();
	}

	private void reset() {
		armed = true;
		running = false;
		armButton.setSelected(true);
		if (manualMode) {
			manualControlButton.setText(START);
		}
		timeTextField.setText(getTimeString(start));
	}

	private void start() {
		armed = false;
		start = System.currentTimeMillis();
		running = true;
		armButton.setSelected(false);
		if (manualMode) {
			manualControlButton.setText(STOP);
		}
	}

	private void stop() {
		running = false;
		if (manualMode) {
			manualControlButton.setText(RESUME);
		}
	}

	private void resume() {
		running = true;
		if (manualMode) {
			manualControlButton.setText(STOP);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		RenderingHints rh = new RenderingHints(
	             RenderingHints.KEY_TEXT_ANTIALIASING,
	             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2.setRenderingHints(rh);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(localHigh ? Color.GREEN : Color.RED);
		g2.fillOval(20, 10, 8, 8);
		g2.setColor(remoteHigh ? Color.GREEN : Color.RED);
		g2.fillOval(34, 10, 8, 8);

	}

	private String getTimeString(long now) {
		long hundreds = (now - start + 5) / 10; // round
		long seconds = hundreds / 100;
		long minutes = seconds / 60;
		String time = String.format("%1$02d:%2$02d.%3$02d",
				minutes, seconds % 60, hundreds % 100);
		return time;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (running) {
			timeTextField.setText(getTimeString(System.currentTimeMillis()));
		}
	}

	private void changeState(String source, String beamState) {
		if (source.equals(LOCAL)) {
			localHigh = beamState.equals(HIGH);
		} else if (source.equals(REMOTE)) {
			remoteHigh = beamState.equals(HIGH);
		}
		repaint();
		if (!manualMode && armed && source.equals(LOCAL) && !localHigh) {
			start();
		} else if (!manualMode && running && source.equals(REMOTE) && !remoteHigh) {
			stop();
		}
	}

}
