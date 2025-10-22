package view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * An abstract class representing a card button.
 * 
 * @author Lorenzo Sabatino
 */
public abstract class CardButton extends JButton {

	private static final long serialVersionUID = 1L;
	private ImageIcon image;
	private double rotationAngle;
	private int cardWidth;
	private int cardHeight;

	/**
	 * Constructs a CardButton with the specified image and card width.
	 *
	 * @param imagePath The path to the image representing the card.
	 * @param cardWidth The width of the card.
	 */
	public CardButton(String imagePath, int cardWidth) {
		this.cardWidth = cardWidth;
		cardHeight = cardWidth * 4 / 3;
		image = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(this.cardWidth, this.cardHeight,
				Image.SCALE_SMOOTH));
		setPreferredSize(new Dimension(image.getIconWidth(), image.getIconHeight()));
	}

	/**
	 * Sets the rotation angle for the card button.
	 *
	 * @param angle The rotation angle in degrees.
	 */
	public void setRotationAngle(double angle) {
		this.rotationAngle = angle;
		if (rotationAngle == 90 || rotationAngle == 270)
			setPreferredSize(new Dimension(image.getIconHeight(), image.getIconWidth()));
		repaint();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the paintComponent method to provide custom rendering behavior for
	 * this component. This method paints the associated image with a specified
	 * rotation angle at the center of the component.
	 *
	 * @param g The Graphics object to be used for rendering.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();

		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;

		AffineTransform rotation = AffineTransform.getRotateInstance(Math.toRadians(rotationAngle), centerX, centerY);

		g2d.transform(rotation);

		if (image != null) {
			int x = (getWidth() - image.getIconWidth()) / 2;
			int y = (getHeight() - image.getIconHeight()) / 2;
			image.paintIcon(this, g2d, x, y);
		}

		g2d.dispose();
	}
}
