package view;

import javax.swing.*;

import controller.AudioManager;

/**
 * The main graphical user interface of the JTrash game. This class represents
 * the application's main window and manages the different panels that make up
 * the user interface.
 * 
 * @author Lorenzo Sabatino
 */
public class MainView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel currentPanel;
	private static MainView instance;

	private MainView() {
		setTitle("JTrash - Lorenzo Sabatino");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setResizable(false);
		ImageIcon icon = new ImageIcon("assets/icon/icon.png");
		setIconImage(icon.getImage());
		
		currentPanel = new MenuPanel();
		setContentPane(currentPanel);
		AudioManager.getInstance().play("assets/audio/background.wav");
	}

	/**
	 * Returns the instance of the MainView class. If no instance exists, a new one
	 * is created.
	 *
	 * @return The instance of the MainView class.
	 */
	public static MainView getInstance() {
		if (instance == null)
			instance = new MainView();
		return instance;
	}

	/**
	 * Returns the currently displayed panel in the main window.
	 *
	 * @return The current panel being displayed.
	 */
	public JPanel getCurrentPanel() {
		return currentPanel;
	}

	/**
	 * Sets the current panel to be displayed in the main window.
	 *
	 * @param currentPanel The panel to set as the current content panel.
	 */
	public void setCurrentPanel(JPanel currentPanel) {
		this.currentPanel = currentPanel;
		setContentPane(currentPanel);
		setVisible(true);
	}

}
