package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import controller.AudioManager;
import controller.ScambiaController;

/**
 * A panel representing a player's game interface, including cards in hand and
 * avatar.
 * 
 * @author Lorenzo Sabatino
 */
public class PlayerPanel extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private int panelWidth;
	private int panelHeight;
	private int numberOfCards;
	private int x;
	private int y;
	private double rotationAngle;
	private int cardWidth;
	private CardButton cardInHand;
	private List<CardButton> cards;
	private JPanel cardInHandPanel;
	private boolean enabledValue = true;
	private BoardPanel boardPanel;
	private int playerIndex;
	private AvatarPanel avatarPanel;

	/**
	 * Constructs a new PlayerPanel instance.
	 *
	 * @param playerIndex   The index of the player.
	 * @param numberOfCards The number of cards in hand.
	 * @param x             The x-coordinate position of the panel.
	 * @param y             The y-coordinate position of the panel.
	 * @param rotationAngle The rotation angle of the panel.
	 * @param cardWidth     The width of each card.
	 * @param boardPanel    The associated BoardPanel.
	 */
	public PlayerPanel(int playerIndex, int numberOfCards, int x, int y, double rotationAngle, int cardWidth,
			BoardPanel boardPanel) {
		this.playerIndex = playerIndex;
		this.numberOfCards = numberOfCards;
		this.x = x;
		this.y = y;
		this.rotationAngle = rotationAngle;
		this.cardWidth = cardWidth;
		this.boardPanel = boardPanel;
		panelWidth = getPanelWidth(cardWidth, rotationAngle);
		panelHeight = getPanelHeight(cardWidth);
		cards = new ArrayList<>();
		setOpaque(false);
		setLayout(new BorderLayout());
		createPanel();
	}

	private void createPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		if (rotationAngle == 90 || rotationAngle == 270)
			topPanel.setPreferredSize(
					new Dimension(getPanelHeight(cardWidth), getPanelWidth(cardWidth, rotationAngle) - 50));
		topPanel.add(createCardInHandPanel());
		topPanel.add(createCardPanel());
		JPanel bottomPanel = new JPanel();
		bottomPanel.setOpaque(false);
		avatarPanel = new AvatarPanel(playerIndex, numberOfCards);
		bottomPanel.add(avatarPanel);
		if (rotationAngle == 180 || rotationAngle == 270) {
			add(bottomPanel, BorderLayout.NORTH);
			add(topPanel, BorderLayout.SOUTH);
		} else {
			add(topPanel, BorderLayout.NORTH);
			add(bottomPanel, BorderLayout.SOUTH);
		}

	}

	private JPanel createCardInHandPanel() {
		cardInHandPanel = new JPanel();
		cardInHandPanel.setOpaque(false);
		return cardInHandPanel;
	}

	private JPanel createCardPanel() {
		JPanel tablePanel = new JPanel();
		tablePanel.setOpaque(false);
		JPanel riga1;
		JPanel riga2;
		int rows = (numberOfCards <= 5) ? 1 : 2;
		if (rotationAngle == 90 || rotationAngle == 270) {
			riga1 = new JPanel(new GridLayout(5, 1, 3, 3));
			riga2 = new JPanel(new GridLayout(5, 1, 3, 3));
			tablePanel.setLayout(new GridLayout(1, rows, 5, 0));
			setBounds(x, y, panelHeight, panelWidth);
		} else {
			riga1 = new JPanel(new FlowLayout());
			riga2 = new JPanel(new FlowLayout());
			tablePanel.setLayout(new GridLayout(rows, 1, 0, 0));
			setBounds(x, y, panelWidth, panelHeight);
		}

		riga1.setOpaque(false);
		riga2.setOpaque(false);

		for (int i = 1; i <= numberOfCards; i++) {
			CardButton cardButton = new DownCardButton(cardWidth);
			cardButton.addActionListener(new ScambiaController(i - 1));
			cardButton.setRotationAngle(rotationAngle);
			if (i <= 5)
				riga1.add(cardButton);
			else
				riga2.add(cardButton);
			cards.add(cardButton);
		}

		if (rows > 1 && (rotationAngle == 180 || rotationAngle == 90)) {
			tablePanel.add(riga2);
			tablePanel.add(riga1);
		} else {
			tablePanel.add(riga1);
			if (rows > 1)
				tablePanel.add(riga2);
		}
		return tablePanel;
	}

	/**
	 * Sets whether the buttons on the panel are enabled.
	 *
	 * @param enabledValue True to enable the buttons, false to disable.
	 */
	public void setEnabledButton(boolean enabledValue) {
		this.enabledValue = enabledValue;
		cards.stream().forEach(button -> button.setEnabled(enabledValue));
		if (cardInHand != null)
			cardInHand.setEnabled(enabledValue);
	}

	private void setCardInHand(String cardName) {
		if (cardName == null)
			cardInHandPanel.remove(cardInHand);
		else {
			if (cardInHand != null)
				cardInHandPanel.remove(cardInHand);
			FaceUpCardButton newCardInHand = new FaceUpCardButton(cardName, cardWidth);
			newCardInHand.setEnabled(enabledValue);
			newCardInHand.setRotationAngle(rotationAngle);
			cardInHandPanel.add(newCardInHand);
			cardInHand = newCardInHand;
		}
		cardInHandPanel.revalidate();
		cardInHandPanel.repaint();
	}

	private void setCardAtIndex(int index, CardButton newCard) {
		CardButton oldCard = cards.get(index);

		JPanel cardPanel = (JPanel) oldCard.getParent();
		cardPanel.remove(oldCard);

		newCard.addActionListener(new ScambiaController(index));
		cardPanel.add(newCard, index % 5);

		cards.set(index, newCard);

		cardPanel.revalidate();
		cardPanel.repaint();
	}

	/**
	 * Gets the preferred width of the panel based on card width and rotation angle.
	 *
	 * @param cardWidth     The width of each card.
	 * @param rotationAngle The rotation angle of the panel.
	 * @return The preferred width of the panel.
	 */
	public static int getPanelWidth(int cardWidth, double rotationAngle) {
		if (rotationAngle == 90 || rotationAngle == 270)
			return (cardWidth * 6) + 90;
		return (cardWidth * 6) + 50;
	}

	/**
	 * Gets the preferred height of the panel based on card width.
	 *
	 * @param cardWidth The width of each card.
	 * @return The preferred height of the panel.
	 */
	public static int getPanelHeight(int cardWidth) {
		return (cardWidth * 4 / 3) * 2 + 90;
	}

	/**
	 * Gets the card currently in hand.
	 *
	 * @return The card currently in hand, or null if no card is in hand.
	 */
	public CardButton getCardInHand() {
		return cardInHand;
	}

	/**
	 * Sets the status of the avatar panel.
	 *
	 * @param active True to make the avatar panel active, false to deactivate.
	 */
	public void setAvatarPanelStatus(boolean b) {
		avatarPanel.setActive(b);
	}

	/**
	 * Update method called when the observed object's state has changed. This
	 * method handles various messages received as notifications and updates the
	 * panel accordingly.
	 *
	 * @param o   The Observable object that triggered the update.
	 * @param arg An argument passed to the update method (typically a list of
	 *            objects).
	 */
	@Override
	public void update(Observable o, Object arg) {
		List<Object> notificationArgs = (List<Object>) arg;
		String message = (String) notificationArgs.get(0);

		switch (message) {
		case "vittoria":
			handleVittoria();
			break;

		case "pesca":
			handlePesca(notificationArgs);
			break;

		case "pesca scarti":
			handlePescaScarti(notificationArgs);
			break;

		case "scarta":
			handleScarta(notificationArgs);
			break;

		case "scambia":
			handleScambia(notificationArgs);
			break;

		default:
			break;
		}
	}

	private void handleVittoria() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boardPanel.createWinnerPopup(avatarPanel.getCharacterName(), avatarPanel.getPlayerImage());
		boardPanel.setNewBoard();
	}

	private void handlePesca(List<Object> args) {
		AudioManager.getInstance().play("assets/audio/pesca.wav");
		setCardInHand(args.get(1).toString());
	}

	private void handlePescaScarti(List<Object> args) {
		AudioManager.getInstance().play("assets/audio/pesca.wav");
		setCardInHand(args.get(1).toString());
		boardPanel.removeTrash();
	}

	private void handleScarta(List<Object> args) {
		AudioManager.getInstance().play("assets/audio/TRASH.wav");
		setCardInHand(null);
		boardPanel.addScarti(args.get(1).toString());
		boardPanel.setActivePanel(playerIndex);
	}

	private void handleScambia(List<Object> args) {
		AudioManager.getInstance().play("assets/audio/scambia.wav");
		int index = (int) args.get(2);
		setCardAtIndex(index, cardInHand);
		setCardInHand(args.get(1).toString());
	}

}
