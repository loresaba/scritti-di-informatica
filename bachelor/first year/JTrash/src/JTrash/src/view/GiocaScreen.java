package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import controller.GiocatoreController;
import controller.JTrash;
import controller.PDFOpener;
import controller.SwitchPageController;

/**
 * Represents the "Gioca" screen of the game, where players can select the game
 * mode and start playing. This panel displays different game modes, provides
 * options to learn how to play, and starts the game.
 * 
 * @author Lorenzo Sabatino
 */
public class GiocaScreen extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image sfondo;
	private JButton btnAvvia;
	private JPanel modalityPanel;
	private int playerNumber;

	/**
	 * Constructs a new instance of the GiocaScreen. Initializes the layout,
	 * background image, and components of the play screen.
	 */
	public GiocaScreen() {
		setLayout(new GridBagLayout());
		sfondo = new ImageIcon("assets/board.jpg").getImage();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 0, 10, 0);
		modalityPanel = createModalityPanel();
		add(modalityPanel, gbc);
	}

	private JPanel createModalityPanel() {
		JPanel modPanel = new JPanel(new BorderLayout());

		JLabel titleLabel = new JLabel("Seleziona una modalità", SwingConstants.CENTER);
		Font titleFont = CustomFont.loadFont(Font.BOLD, 16);
		titleLabel.setFont(titleFont);
		modPanel.add(titleLabel, BorderLayout.NORTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		btnAvvia = new FuturisticButton("Avvia Gioco");
		btnAvvia.setEnabled(false);
		btnAvvia.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GiocatoreController.setGiocatori(playerNumber);
				remove(modalityPanel);
				setLayout(new BorderLayout());
				BoardPanel boardPanel = new BoardPanel(playerNumber);
				add(boardPanel, BorderLayout.CENTER);
				revalidate();
				repaint();

				JTrash.setGameRunning(true);
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						JTrash.startGame();
						return null;
					}
				};
				worker.execute();
			}
		});
		JButton btnComeGiocare = new FuturisticButton("Come giocare");
		btnComeGiocare.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PDFOpener.openPDFFile("assets/JTrash_Come_giocare.pdf");
			}
		});
		JButton btnMenu = new FuturisticButton("Torna al Menu");
		btnMenu.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwitchPageController.showMenuScreen();
			}
		});
		buttonsPanel.add(btnAvvia);
		buttonsPanel.add(btnComeGiocare);
		buttonsPanel.add(btnMenu);
		modPanel.add(buttonsPanel, BorderLayout.SOUTH);

		JPanel centralModPanel = new JPanel(new FlowLayout());
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton button1 = new ModalityRadioButton("FACILE", "Ti scontrerai contro Dr. Reginald\n Blackwood",
				"assets/modality/mod1.jpg");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playerNumber = 2;
				btnAvvia.setEnabled(true);
			}
		});

		JRadioButton button2 = new ModalityRadioButton("NORMALE",
				"Ti scontrerai contro Dr. Reginald\n Blackwood e il suo braccio destro", "assets/modality/mod2.jpg");
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playerNumber = 3;
				btnAvvia.setEnabled(true);
			}
		});
		JRadioButton button3 = new ModalityRadioButton("DIFFICILE",
				"Ti scontrerai contro Dr. Reginald\n Blackwood e i suoi scagnozzi", "assets/modality/mod3.jpg");
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playerNumber = 4;
				btnAvvia.setEnabled(true);
			}
		});

		buttonGroup.add(button1);
		buttonGroup.add(button2);
		buttonGroup.add(button3);
		centralModPanel.add(button1);
		centralModPanel.add(button2);
		centralModPanel.add(button3);
		modPanel.add(centralModPanel, BorderLayout.CENTER);

		return modPanel;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the paintComponent method to provide custom rendering behavior for
	 * this component. This method paints the specified background image (sfondo)
	 * scaled to cover the entire component area.
	 *
	 * @param g The Graphics object to be used for rendering.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
	}

}
