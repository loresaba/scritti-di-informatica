package view;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import controller.AudioManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A custom button with a futuristic appearance.
 * 
 * @author Lorenzo Sabatino
 */
public class FuturisticButton extends JButton {

	private static final long serialVersionUID = 1L;
	private static final Color BUTTON_COLOR = new Color(50, 150, 250);
	private static final Color HOVER_COLOR = new Color(100, 180, 255);
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color DISABLED_COLOR = Color.GRAY;
	private static final Font BUTTON_FONT = new Font("Roboto", Font.BOLD, 18);
	private boolean isRed = false;

	/**
	 * Creates a new FuturisticButton with the specified text.
	 *
	 * @param text The text to display on the button.
	 */
	public FuturisticButton(String text) {
		super(text);
		setStyle();
	}

	/**
	 * Creates a new FuturisticButton with the specified text and icon.
	 *
	 * @param text The text to display on the button.
	 * @param icon The icon to display on the button.
	 */
	public FuturisticButton(String text, ImageIcon icon) {
		super(text, icon);
		setStyle();
	}

	private void setStyle() {
		setBorderPainted(false);
		setMargin(new Insets(10, 20, 10, 20));
		setForeground(TEXT_COLOR);
		setFont(BUTTON_FONT);
		setBackground(BUTTON_COLOR);

		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				AudioManager.getInstance().play("assets/audio/click-21156.wav");
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (isEnabled()) {
					if (isRed) {
						setBackground(new Color(255, 114, 118));
					} else {
						setBackground(HOVER_COLOR);
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (isEnabled()) {
					setBackground(BUTTON_COLOR);
				}
			}
		});
	}

	/**
	 * Sets whether the button should have a red background color when hovered over.
	 *
	 * @param isRed {@code true} to enable the red background color on hover,
	 *              {@code false} otherwise.
	 */
	public void setRed(boolean isRed) {
		this.isRed = isRed;
	}

	/**
	 * Sets the enabled state of the button and updates the background color
	 * accordingly.
	 *
	 * @param enabled {@code true} to enable the button, {@code false} to disable
	 *                it.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		// Cambia lo sfondo del pulsante quando viene disabilitato
		setBackground(enabled ? BUTTON_COLOR : DISABLED_COLOR);
	}

	/**
	 * Paints the component with the appropriate background color based on the
	 * button's state. Overrides the default painting behavior.
	 *
	 * @param g The Graphics context to paint on.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		if (getModel().isArmed() && isEnabled()) {
			g.setColor(HOVER_COLOR);
		} else {
			g.setColor(isEnabled() ? BUTTON_COLOR : DISABLED_COLOR);
		}

		int borderRadius = 30;
		g.fillRoundRect(0, 0, getWidth(), getHeight(), borderRadius, borderRadius);
		super.paintComponent(g);
	}
}
