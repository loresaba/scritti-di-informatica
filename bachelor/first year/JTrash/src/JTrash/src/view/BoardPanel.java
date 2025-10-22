package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.AudioManager;
import controller.GiocatoreController;
import controller.JTrash;
import controller.PDFOpener;
import controller.PescaController;
import controller.PescaScartiController;
import controller.ScartaController;
import controller.SwitchPageController;

/**
 * The main panel that represents the game board. It displays player panels,
 * central elements, buttons, and handles interactions during gameplay.
 * 
 * @author Lorenzo Sabatino
 */
public class BoardPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private int screenWidth;
	private int screenHeight;
	private int panelWidth;
	private int panelHeight;
	private int cardWidth;
	private double rotationAngle;
	private int playerIndex;
	private int playerNumber;
	private JLayeredPane scartiPanel;
	private JButton trashCard;
	private JButton trashButton;
	private int zIndex;
	private List<PlayerPanel> playerPanelList;

	/**
	 * Constructs a new BoardPanel with the specified number of players.
	 *
	 * @param playerNumber The number of players in the game.
	 */
	public BoardPanel(int playerNumber) {
		this.playerNumber = playerNumber;
		setNewBoard();
	}

	/**
	 * Sets up a new game board by creating and arranging components.
	 */
	public void setNewBoard() {
		removeAll();
		createBoardPanel();
		revalidate();
		repaint();
	}

	private void createBoardPanel() {
		setLayout(null);
		setOpaque(false);

		Dimension screenSize = getToolkit().getScreenSize();
		screenWidth = screenSize.width;
		screenHeight = screenSize.height;

		playerPanelList = new ArrayList<>();

		setValue(0, 75, 0);
		PlayerPanel bottomPanel = new PlayerPanel(playerIndex, GiocatoreController.getNumeroCarte(playerIndex),
				screenWidth / 2 - panelWidth / 2, screenHeight / 2, rotationAngle, cardWidth, this);
		add(bottomPanel);
		GiocatoreController.addObserver(bottomPanel, 0);
		playerPanelList.add(bottomPanel);
		bottomPanel.setAvatarPanelStatus(true);

		setValue(1, 55, 180);
		PlayerPanel topPanel = new PlayerPanel(playerIndex, GiocatoreController.getNumeroCarte(playerIndex),
				screenWidth / 2 - panelWidth / 2, screenHeight / 2 - (int) (panelHeight * 1.6), rotationAngle,
				cardWidth, this);
		topPanel.setEnabledButton(false);
		add(topPanel);
		GiocatoreController.addObserver(topPanel, 1);
		playerPanelList.add(topPanel);

		if (playerNumber >= 3) {
			setValue(2, 55, 90);
			PlayerPanel leftPanel = new PlayerPanel(playerIndex, GiocatoreController.getNumeroCarte(playerIndex),
					screenWidth / 2 - (int) (panelHeight * 2.0), screenHeight / 2 - (int) (panelWidth / 2 * 1.1),
					rotationAngle, cardWidth, this);
			leftPanel.setEnabledButton(false);
			add(leftPanel);
			GiocatoreController.addObserver(leftPanel, 2);
			playerPanelList.add(leftPanel);
		}

		if (playerNumber == 4) {
			setValue(3, 55, 270);
			PlayerPanel rightPanel = new PlayerPanel(playerIndex, GiocatoreController.getNumeroCarte(playerIndex),
					screenWidth / 2 + (int) (panelHeight * 1.0), screenHeight / 2 - (int) (panelWidth / 2 * 1.3),
					rotationAngle, cardWidth, this);
			rightPanel.setEnabledButton(false);
			add(rightPanel);
			GiocatoreController.addObserver(rightPanel, 3);
			playerPanelList.add(rightPanel);
		}

		JPanel centerPanel = createCenterPanel();
		add(centerPanel);

		ImageIcon exitIcon = new ImageIcon("assets/icon/exit.png");
		FuturisticButton btnMenu = new FuturisticButton("", exitIcon);
		btnMenu.setRed(true);
		btnMenu.setToolTipText("Esci");
		btnMenu.setBounds(screenWidth - 60, 20, 50, 50);
		btnMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainView mainView = MainView.getInstance();
				int choice = JOptionPane.showConfirmDialog(mainView, "Sei sicuro di voler tornare alla Home?",
						"Conferma Uscita", JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					JTrash.setGameRunning(false);
					;
					SwitchPageController.showMenuScreen();
				}
			}
		});
		add(btnMenu);

		ImageIcon ruleIcon = new ImageIcon("assets/icon/info.png");
		FuturisticButton btnRule = new FuturisticButton("", ruleIcon);
		btnRule.setToolTipText("Come giocare");
		btnRule.setBounds(screenWidth - 120, 20, 50, 50);
		btnRule.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PDFOpener.openPDFFile("assets/JTrash_Come_giocare.pdf");
			}
		});
		add(btnRule);

		ImageIcon unmutedIcon = new ImageIcon("assets/icon/unmuted.png");
		ImageIcon mutedIcon = new ImageIcon("assets/icon/muted.png");
		FuturisticButton muteButton = new FuturisticButton("",
				(AudioManager.getInstance().isMuted()) ? mutedIcon : unmutedIcon);
		muteButton.setToolTipText("Muta");
		muteButton.setBounds(screenWidth - 180, 20, 50, 50);
		muteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AudioManager audioManager = AudioManager.getInstance();
				audioManager.setMuted(!audioManager.isMuted());
				float volume = audioManager.isMuted() ? 0.0f : 1.0f;
				audioManager.setVolume(audioManager.getBgClip(), volume);
				if (audioManager.isMuted()) {
					muteButton.setIcon(mutedIcon);
				} else {
					muteButton.setIcon(unmutedIcon);
				}
			}
		});
		add(muteButton);
	}

	private JPanel createCenterPanel() {
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, screenWidth / 6, 0));
		int centerPanelWidth = 700;
		int centerPanelHeight = 100;
		centerPanel.setBounds(screenWidth / 2 - (int) ((centerPanelWidth / 2) * 0.95),
				screenHeight / 2 - (int) (centerPanelHeight * 1.2), centerPanelWidth, centerPanelHeight);
		centerPanel.setOpaque(false);

		scartiPanel = new JLayeredPane();
		scartiPanel.setOpaque(false);
		scartiPanel.setPreferredSize(new Dimension(75, 100));
		ImageIcon trashIcon = new ImageIcon("assets/icon/trash.png");
		trashButton = new JButton(trashIcon);
		trashButton.setToolTipText("Trash a card");
		trashButton.setBounds(0, 0, 75, 100);
		trashCard = trashButton;
		zIndex++;

		trashCard.addActionListener(new ScartaController());

		scartiPanel.add(trashCard, JLayeredPane.DEFAULT_LAYER);

		centerPanel.add(scartiPanel);
		JButton mazzo = new DownCardButton(75);
		mazzo.addActionListener(new PescaController());
		centerPanel.add(mazzo);

		return centerPanel;
	}

	private void setValue(int playerIndex, int cardWidth, double rotationAngle) {
		this.playerIndex = playerIndex;
		this.cardWidth = cardWidth;
		this.rotationAngle = rotationAngle;
		panelWidth = PlayerPanel.getPanelWidth(cardWidth, rotationAngle);
		panelHeight = PlayerPanel.getPanelHeight(cardWidth);
	}

	/**
	 * Adds a card to the "scarti" (discard) pile on the game board.
	 *
	 * @param cardName The name of the card to be added to the discard pile.
	 */
	public void addScarti(String cardName) {
		FaceUpCardButton newTrashCard = new FaceUpCardButton(cardName, 75);
		newTrashCard.setBounds(0, 0, 75, 100);

		// action listener
		newTrashCard.addActionListener(new PescaScartiController());
		newTrashCard.addActionListener(new ScartaController());

		scartiPanel.add(newTrashCard, new Integer(zIndex++));
		trashCard = newTrashCard;
		scartiPanel.revalidate();
		scartiPanel.repaint();
	}

	/**
	 * Removes the top card from the "scarti" (discard) pile on the game board.
	 */
	public void removeTrash() {
		zIndex--;
		scartiPanel.remove(trashCard);
		scartiPanel.revalidate();
		scartiPanel.repaint();
	}

	/**
	 * Sets the active player panel on the game board based on the provided index.
	 *
	 * @param index The index of the player panel to be set as active.
	 */
	public void setActivePanel(int index) {
		playerPanelList.get(index).setAvatarPanelStatus(false);
		index = (index + 1) % playerNumber;
		playerPanelList.get(index).setAvatarPanelStatus(true);
	}

	/**
	 * Creates a pop-up window displaying the winner of the turn.
	 *
	 * @param winnerName The name of the winner.
	 * @param winImage   The image representing the winner.
	 */
	public void createWinnerPopup(String winnerName, BufferedImage winImage) {
		JPanel popupPanel = new JPanel();
		popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));

		JLabel winnerLabel = new JLabel("Il vincitore del turno è");
		winnerLabel.setFont(CustomFont.loadFont(Font.BOLD, 20));
		winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		popupPanel.add(winnerLabel);

		popupPanel.add(Box.createVerticalStrut(15));

		Image scaledImage = winImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);
		JLabel imageLabel = new JLabel(scaledIcon);
		imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		popupPanel.add(imageLabel);

		popupPanel.add(Box.createVerticalStrut(15));

		JLabel nameLabel = new JLabel(winnerName);
		nameLabel.setFont(new Font("Arial", Font.ITALIC, 18));
		nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		popupPanel.add(nameLabel);

		Random random = new Random();
		int randomNumber = random.nextInt(4);
		AudioManager.getInstance().play("assets/audio/confirmation_" + winnerName + randomNumber + ".wav");
		JOptionPane.showMessageDialog(this, popupPanel, "Vincitore!", JOptionPane.PLAIN_MESSAGE);
	}

}
