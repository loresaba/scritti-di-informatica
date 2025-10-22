package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A custom radio button used for selecting a game modality in the main menu.
 * This class extends JRadioButton and displays an icon along with a title and
 * description. When selected, it is highlighted and displays a border.
 * 
 * @author Lorenzo Sabatino
 */
public class ModalityRadioButton extends JRadioButton {

	private static final long serialVersionUID = 1L;
	private ImageIcon icon;
	private boolean isSelected;

	/**
	 * Constructs a new instance of the ModalityRadioButton class with the specified
	 * title, description, and image path.
	 *
	 * @param title       The title of the modality.
	 * @param description A brief description of the modality.
	 * @param imagePath   The path to the image representing the modality.
	 */
	public ModalityRadioButton(String title, String description, String imagePath) {

		isSelected = false;

		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setOpaque(false);
		textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JLabel titleLabel = new JLabel(title);
		Font titleFont = new Font("Dialog", Font.BOLD, 14);
		titleLabel.setFont(titleFont);
		JTextArea descriptionText = new JTextArea(description);
		descriptionText.setOpaque(false);
		descriptionText.setEditable(false);
		Font descriptionFont = new Font("Dialog", Font.ITALIC, 10);
		descriptionText.setFont(descriptionFont);

		textPanel.add(titleLabel, BorderLayout.NORTH);
		textPanel.add(descriptionText, BorderLayout.CENTER);

		setLayout(new BorderLayout());

		ImageIcon backgroundImage = new ImageIcon(imagePath);
		Image image = backgroundImage.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
		icon = new ImageIcon(image);

		add(textPanel, BorderLayout.SOUTH);

		setBorderPainted(false);
		setContentAreaFilled(false);
		setFocusPainted(false);

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
	 * this component. This method paints the associated icon at the center of the
	 * component and optionally highlights the component if it is selected.
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
				((Graphics2D) g).setStroke(new BasicStroke(7));
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the getPreferredSize method to provide a preferred size for the
	 * component based on the size of the associated icon.
	 *
	 * @return The preferred Dimension for the component.
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
