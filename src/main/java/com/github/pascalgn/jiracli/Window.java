package com.github.pascalgn.jiracli;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

class Window {
	private static class Panel extends JPanel {
		private static final long serialVersionUID = 2206865626322221745L;

		private final JTextField rootURL;
		private final JTextField username;
		private final JPasswordField password;

		private final JTextArea output;

		private final JTextArea input;

		public Panel() {
			rootURL = new JTextField();
			username = new JTextField(20);
			password = new JPasswordField(20);
			output = new JTextArea();
			input = new JTextArea(2, 20);
			initComponents();
			layoutComponents();
		}

		private void initComponents() {
			Insets margin = new Insets(3, 3, 3, 3);
			rootURL.setMargin(margin);
			username.setMargin(margin);
			password.setMargin(margin);
			output.setMargin(margin);
			input.setMargin(margin);
		}

		private void layoutComponents() {
			JLabel rootURLLabel = new JLabel("Root URL:");

			JLabel usernameLabel = new JLabel("Username:");
			JLabel passwordLabel = new JLabel("Password:");

			JSeparator separator = new JSeparator();

			JScrollPane outputScrollPane = new JScrollPane(output);
			JScrollPane inputScrollPane = new JScrollPane(input);

			GroupLayout groupLayout = new GroupLayout(this);
			groupLayout.setAutoCreateContainerGaps(true);
			groupLayout.setAutoCreateGaps(true);
			groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addGroup(groupLayout
					.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup().addComponent(rootURLLabel).addComponent(usernameLabel))
					.addGroup(groupLayout.createParallelGroup()
							.addGroup(groupLayout.createSequentialGroup().addComponent(rootURL)).addGroup(
									groupLayout.createSequentialGroup().addComponent(username)
											.addComponent(passwordLabel).addComponent(password))))
					.addComponent(separator).addComponent(outputScrollPane).addComponent(inputScrollPane));
			groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createBaselineGroup(true, false).addComponent(rootURLLabel)
							.addComponent(rootURL))
					.addGroup(groupLayout.createBaselineGroup(true, false).addComponent(usernameLabel)
							.addComponent(username).addComponent(passwordLabel).addComponent(password))
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(outputScrollPane, GroupLayout.DEFAULT_SIZE, 300, GroupLayout.DEFAULT_SIZE)
					.addComponent(inputScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE));
			setLayout(groupLayout);
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new Panel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
