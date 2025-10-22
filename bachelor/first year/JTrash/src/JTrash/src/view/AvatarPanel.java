package view;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controller.JsonStatsController;

/**
 * Represents a panel displaying an avatar's information, including an image,
 * player name, character name, and number of remaining cards.
 * 
 * @author Lorenzo Sabatino
 */
public class AvatarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Avatar avatar;
	private BufferedImage playerImage;
	private String playerName;
	private String characterName;
	private int remainingCards;
	private boolean isActive = false;
	private Color panelColor;
	private float opacity;
	private JPanel imagePanel;

	/**
	 * Constructs an AvatarPanel with the specified avatar index and remaining cards
	 * count.
	 *
	 * @param avatarIndex    The index of the avatar to display.
	 * @param remainingCards The number of remaining cards associated with the
	 *                       avatar.
	 */
	public AvatarPanel(int avatarIndex, int remainingCards) {
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(15, 0, 15, 0));
		setOpaque(false);
		if (avatarIndex == 0) {
			JsonStatsController jsc = new JsonStatsController();
			jsc.readJsonStats();
			this.playerName = jsc.getNickname();
		} else
			this.playerName = "";
		avatar = Avatar.getAvatarBoardByIndex(avatarIndex);
		characterName = avatar.getNome();
		try {
			playerImage = ImageIO.read(new File(avatar.getImagePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.remainingCards = remainingCards;
		createAvatarPanel();
		updatePanelStyle();
	}

	private void createAvatarPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new GridLayout(1, 2, -1, 0));

		imagePanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				int imageSize = 60;
				int centerX = (getWidth() - imageSize) / 2;
				int centerY = (getHeight() - imageSize) / 2;
				int width = getWidth();
				int height = getHeight();

				g.setColor(new Color(255, 255, 255, 0));
				g.fillRect(0, 0, width / 2, height);

				g.setColor(panelColor);
				g.fillRect(width / 2, 0, width / 2, height);

				Ellipse2D circle = new Ellipse2D.Double(centerX, centerY, imageSize, imageSize);
				((Graphics2D) g).setClip(circle);

				Image resizedImage = playerImage.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH);

				g.drawImage(resizedImage, centerX, centerY, this);
			}
		};
		imagePanel.setOpaque(false);

		JPanel infoPanel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;

				int width = getWidth();
				int height = getHeight();

				int borderRadius = 30;

				g2d.setColor(panelColor);
				g2d.fillRect(0, 0, width - borderRadius, height);

				RoundRectangle2D roundedRect = new RoundRectangle2D.Float(0, 0, width, height, borderRadius,
						borderRadius);
				g2d.setColor(panelColor);
				g2d.fill(roundedRect);
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension dimensionePreferita = super.getPreferredSize();
				int nuovaLarghezza = dimensionePreferita.width + 10;
				return new Dimension(nuovaLarghezza, dimensionePreferita.height);
			}
		};
		infoPanel.setOpaque(false);
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

		JLabel playerNameLabel = new JLabel(playerName);
		playerNameLabel.setFont(new Font("Arial", Font.BOLD, 12));
		infoPanel.add(playerNameLabel);

		JLabel characterNameLabel = new JLabel(characterName);
		Font italicFont = new Font("Arial", Font.ITALIC | Font.BOLD, 12);
		characterNameLabel.setFont(italicFont);
		infoPanel.add(characterNameLabel);

		JLabel remainingCardsLabel = new JLabel("Carte: " + remainingCards);
		infoPanel.add(remainingCardsLabel);

		mainPanel.add(imagePanel);
		mainPanel.add(infoPanel);
		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Sets the active state of the avatar panel.
	 *
	 * @param isActive True if the panel should be active, false otherwise.
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
		updatePanelStyle();
	}

	private void updatePanelStyle() {
		if (isActive) {
			panelColor = Color.WHITE;
			opacity = 1.0f;
		} else {
			panelColor = Color.GRAY;
			opacity = 0.6f;
		}
		repaint();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Overrides the paintComponent method to provide custom painting behavior for
	 * this component. This method is responsible for rendering graphics on the
	 * component, including setting the alpha composite to control the transparency
	 * of the rendered image and text.
	 * 
	 * @param g The Graphics object to be used for rendering.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
	}

	/**
	 * Returns the player's avatar image.
	 *
	 * @return The BufferedImage representing the player's avatar image.
	 */
	public BufferedImage getPlayerImage() {
		return playerImage;
	}

	/**
	 * Returns the character name associated with the avatar.
	 *
	 * @return The character name.
	 */
	public String getCharacterName() {
		return characterName;
	}

}
