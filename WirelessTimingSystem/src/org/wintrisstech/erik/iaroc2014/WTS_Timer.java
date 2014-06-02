package org.wintrisstech.erik.iaroc2014;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class WTS_Timer extends JPanel implements Runnable, ActionListener {
	private static final int SERVER_PORT = 47047;
	private long start;
	private long now;
	private Timer ticker = new Timer(20, this);
	Font displayFont = new Font("Helvetica", Font.PLAIN, 144);
	private boolean running = false;
	private boolean armed = false;
	private boolean localHigh = false;
	private boolean remoteHigh = false;
	private PrintWriter out;
	private static final Pattern EVENT_PATTERN = Pattern.compile("(local|remote):(high|low)");

	public static void main(String[] args) throws IOException {
		if (args.length != 1 || !args[0].matches("[1-2]")) {
			printUsage();
			System.exit(1);
		}
		String host = "192.168.42." + args[0];
		// String host = "localhost";
		WTS_Timer wtsTimer = new WTS_Timer();
		SwingUtilities.invokeLater(wtsTimer);
		wtsTimer.listen(host, SERVER_PORT);
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
			out = new PrintWriter(socket.getOutputStream(), true);
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
						String state = m.group(2);
						changeState(source, state);
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
				if (out != null)
					out.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		System.exit(0);
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("EventClient");
		setPreferredSize(new Dimension(600, 184));
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		// Reset button
		JButton startButton = new JButton("START");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				armed = true;
				running = false;
				start = now;
			}
		});
		buttonPanel.add(startButton);

		// Resume button
		final JButton resumeButton = new JButton("Resume");
		resumeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!armed && remoteHigh) {
					running = true;
				}
			}
		});

		buttonPanel.add(resumeButton);
		frame.add(buttonPanel, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		ticker.start();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.setFont(displayFont);
		g.drawString(getTime(), 20, 144);
		g.setColor(localHigh ? Color.GREEN : Color.RED);
		g.drawOval(20, 10, 4, 4);
		g.setColor(remoteHigh ? Color.GREEN : Color.RED);
		g.drawOval(30, 10, 4, 4);

	}

	private String getTime() {
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
			now = System.currentTimeMillis();
			repaint();
		}
	}

	private void changeState(String source, String state) {
		if (source.equals("local")) {
			localHigh = state.equals("high");
		} else if (source.equals("remote")) {
			remoteHigh = state.equals("high");
		}
		if (armed && source.equals("local") && !localHigh) {
			start = System.currentTimeMillis();
			running = true;
			armed = false;
		} else if (running && source.equals("remote") && !remoteHigh) {
			running = false;
		}
	}

}
