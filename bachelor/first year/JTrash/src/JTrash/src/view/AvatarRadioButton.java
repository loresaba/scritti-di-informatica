package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A customized radio button component that displays an image icon and provides
 * visual feedback when selected.
 * 
 * @author Lorenzo Sabatino
 */
public class AvatarRadioButton extends JRadioButton {

	private static final long serialVersionUID = 1L;
	private ImageIcon icon;
	private boolean isSelected;

	/**
	 * Constructs an AvatarRadioButton with the specified image icon.
	 *
	 * @param icon The ImageIcon to display on the radio button.
	 */
	public AvatarRadioButton(ImageIcon icon) {
		this.icon = icon;
		isSelected = false;
		setOpaque(false);

		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				isSelected = isSelected();
				repaint();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the paintComponent method to provide custom rendering behavior for
	 * this component. This method paints the icon at the center of the component
	 * and optionally highlights the component if it is selected.
	 *
	 * @param g The Graphics object to be used for rendering.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (icon != null) {
			int x = (getWidth() - icon.getIconWidth()) / 2;
			int y = (getHeight() - icon.getIconHeight()) / 2;
			icon.paintIcon(this, g, x, y);

			if (isSelected) {
				g.setColor(new Color(50, 150, 250));
				((Graphics2D) g).setStroke(new BasicStroke(5));
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the getPreferredSize method to provide a preferred size for the
	 * JRadioButton based on the size of the associated icon.
	 *
	 * @return The preferred Dimension for the JRadioButton.
	 */
	@Override
	public Dimension getPreferredSize() {
		if (icon != null) {
			return new Dimension(icon.getIconWidth(), icon.getIconHeight());
		} else {
			return super.getPreferredSize();
		}
	}
}