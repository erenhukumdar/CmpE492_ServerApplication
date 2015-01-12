package edu.boun.gglass.ui;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import javax.swing.JLabel;

import edu.boun.gglass.server.httpServer;
import edu.boun.gglass.server.mdnsServer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;

import java.awt.Color;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class mainWindow extends JFrame implements uiCallbacks{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4532936918733438343L;
	private JPanel contentPane;
	private JLabel lblHttpServerStatus;
	private JLabel lblMdnsServerStatus;
	private JLabel lblImageArea;
	private JLabel lblImageInfo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindow frame = new mainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public mainWindow() {
		final httpServer server = new httpServer(this);
		final mdnsServer mdns = new mdnsServer(this);

		setTitle("Google Glass server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnNewButton = new JButton("Start Server");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {

				Object source = arg0.getSource();
				if (source instanceof JButton) {
					JButton clickedButton = (JButton)source;
					if (clickedButton.getText().equals("Start Server")) {
						clickedButton.setText("Stop Server");
						(new Thread(server)).start();
						(new Thread(mdns)).start();
					} else if (clickedButton.getText().equals("Stop Server")) {
						clickedButton.setText("Start Server");
						server.stop();
						mdns.stop();
					}
				}				
			}
		});
		btnNewButton.setBounds(10, 11, 120, 23);
		contentPane.add(btnNewButton);

		JLabel lblWebServer = new JLabel("Web Server:");
		lblWebServer.setForeground(new Color(128, 128, 128));
		lblWebServer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWebServer.setBounds(208, 11, 81, 23);
		contentPane.add(lblWebServer);

		JLabel lblMdnsServer = new JLabel("mDNS Server:");
		lblMdnsServer.setForeground(new Color(128, 128, 128));
		lblMdnsServer.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMdnsServer.setBounds(391, 11, 93, 23);
		contentPane.add(lblMdnsServer);

		lblImageArea = new JLabel("");
		lblImageArea.setBounds(10, 66, 604, 364);
		contentPane.add(lblImageArea);

		lblHttpServerStatus = new JLabel("Not Started");
		lblHttpServerStatus.setForeground(new Color(128, 128, 128));
		lblHttpServerStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblHttpServerStatus.setBounds(300, 11, 81, 23);
		contentPane.add(lblHttpServerStatus);

		lblMdnsServerStatus = new JLabel("Not Started");
		lblMdnsServerStatus.setForeground(new Color(128, 128, 128));
		lblMdnsServerStatus.setBounds(494, 11, 93, 23);
		contentPane.add(lblMdnsServerStatus);

		lblImageInfo = new JLabel("");
		lblImageInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblImageInfo.setForeground(new Color(128, 0, 0));
		lblImageInfo.setBounds(10, 52, 604, 14);
		contentPane.add(lblImageInfo);
	}

	@Override
	public void onHttpServerStart() {
		lblHttpServerStatus.setText("Running");
	}

	@Override
	public void onHttpServerStop() {
		lblHttpServerStatus.setText("Stopped");
	}

	@Override
	public void onMdnsServerStart() {
		lblMdnsServerStatus.setText("Running");
	}

	@Override
	public void onMdnsServerStop() {
		lblMdnsServerStatus.setText("Stopped");
	}

	@Override
	public void onImageReceived(String imagePath, String Ip) {
		ImageIcon icon = new ImageIcon(imagePath);
		lblImageArea.setIcon(icon);
		
		//remove garbage image since we do not need it again.
		File file = new File(imagePath);
		file.delete();
		
		//inform user about incoming image
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		lblImageInfo.setText("image received from '" + Ip + "' at " +  dateFormat.format(date));
	}
}
