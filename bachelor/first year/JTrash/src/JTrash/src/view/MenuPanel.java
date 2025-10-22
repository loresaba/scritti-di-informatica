package view;

import javax.swing.*;

import controller.SwitchPageController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main menu panel of the JTrash game. This class represents the initial
 * user interface screen where players can access different game modes,
 * profiles, history, and exit the game.
 * 
 * @author Lorenzo Sabatino
 */
public class MenuPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private FuturisticButton btnGioca, btnProfilo, btnStoria, btnEsci;
	private Image sfondo;

	/**
	 * Constructs a new instance of the MenuPanel class. Initializes the layout,
	 * background image, and creates the main title, subtitle, and buttons of the
	 * main menu.
	 */
	public MenuPanel() {
		setLayout(new GridBagLayout());
		sfondo = new ImageIcon("assets/bg.jpg").getImage();

		JPanel contentPanel = new JPanel();
		JPanel titlePanel = createTitlePanel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 0, 10, 0);

		JPanel buttonPanel = createButtonPanel();
		gbc.gridy = 1;

		contentPanel.add(titlePanel);
		contentPanel.add(buttonPanel);
		add(contentPanel, gbc);
	}

	private JPanel createTitlePanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.setOpaque(false);

		String title = "JTrash";
		JLabel titleLabel = new JLabel(title);
		Font titleFont = CustomFont.loadFont(Font.BOLD, 64);
		titleLabel.setFont(titleFont);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		String subtitle = "Futuristic Card Game";
		JLabel subtitleLabel = new JLabel(subtitle);
		Font subtitleFont = CustomFont.loadFont(Font.ITALIC, 24);
		subtitleLabel.setFont(subtitleFont);
		subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		titlePanel.add(titleLabel);
		titlePanel.add(subtitleLabel);

		return titlePanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.setOpaque(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.insets = new Insets(5, 0, 5, 0);

		btnGioca = new FuturisticButton("Gioca");
		btnProfilo = new FuturisticButton("Profilo");
		btnStoria = new FuturisticButton("Storia");
		btnEsci = new FuturisticButton("Esci");
		btnEsci.setRed(true);

		Font buttonFont = new Font("Arial", Font.BOLD, 20);
		btnGioca.setFont(buttonFont);
		btnProfilo.setFont(buttonFont);
		btnStoria.setFont(buttonFont);
		btnEsci.setFont(buttonFont);

		buttonPanel.add(btnGioca, gbc);
		buttonPanel.add(btnProfilo, gbc);
		buttonPanel.add(btnStoria, gbc);
		buttonPanel.add(btnEsci, gbc);

		btnGioca.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwitchPageController.showGiocaScreen();
			}
		});

		btnProfilo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwitchPageController.showProfiloScreen();
			}
		});

		btnStoria.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwitchPageController.showStoriaScreen();
			}
		});

		btnEsci.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainView mainView = MainView.getInstance();
				int choice = JOptionPane.showConfirmDialog(mainView, "Sei sicuro di voler uscire?", "Conferma uscita",
						JOptionPane.YES_NO_OPTION);

				if (choice == JOptionPane.YES_OPTION) {
					mainView.dispose();
				}
			}
		});

		return buttonPanel;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the paintComponent method to provide custom rendering behavior for
	 * this component. This method paints a background image (sfondo) that covers
	 * the entire component area, scaling the image to fit the component's
	 * dimensions.
	 *
	 * @param g The Graphics object to be used for rendering.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
	}

}
