package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.AudioManager;
import controller.JsonStatsController;
import controller.SwitchPageController;

/**
 * Represents the profile screen where users can customize their profile
 * settings and view statistics.
 * 
 * @author Lorenzo Sabatino
 */
public class ProfiloScreen extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image sfondo;
	private List<Avatar> avatarList;
	private Avatar selectedAvatar;
	private JButton btnSalva;
	private int avatarIndex;
	private JsonStatsController json;
	private JTextField nicknameInput;

	/**
	 * Constructs a new ProfiloScreen instance.
	 */
	public ProfiloScreen() {
		setLayout(new GridBagLayout());
		sfondo = new ImageIcon("assets/profilo.jpg").getImage();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 0, 10, 0);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		json = new JsonStatsController();
		json.readJsonStats();
		avatarList = Avatar.getAvatar();
		JPanel avatarPanel = createAvatarPanel();
		JPanel statsPanel = createStatsPanel();

		JPanel panelBottoni = createButtonPanel();

		contentPanel.add(avatarPanel, BorderLayout.WEST);
		contentPanel.add(statsPanel, BorderLayout.EAST);
		contentPanel.add(panelBottoni, BorderLayout.SOUTH);
		add(contentPanel, gbc);
	}

	private JPanel createAvatarPanel() {
		JPanel avatarPanel = new JPanel();
		avatarPanel.setLayout(new BorderLayout());

		JPanel titlePanel = new JPanel();
		JLabel titleLabel = new JLabel("Seleziona un personaggio");
		Font titleFont = CustomFont.loadFont(Font.BOLD, 14);
		titleLabel.setFont(titleFont);
		titlePanel.add(titleLabel);
		avatarPanel.add(titlePanel, BorderLayout.NORTH);

		List<AvatarRadioButton> avatarButtons = createAvatarButtons();

		JPanel descriptionPanel = new JPanel();
		JTextArea descriptionText = new JTextArea(selectedAvatar.getNome() + " " + selectedAvatar.getDescrizione());
		descriptionText.setPreferredSize(new Dimension(400, 70));
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setFont(new Font(Font.SERIF, Font.ITALIC, 13));

		descriptionText.setEditable(false);
		descriptionText.setOpaque(false);
		descriptionPanel.add(descriptionText);
		avatarPanel.add(descriptionPanel, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

		ButtonGroup buttonGroup = new ButtonGroup();
		for (AvatarRadioButton button : avatarButtons) {
			buttonGroup.add(button);
			buttonPanel.add(button);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int index = avatarButtons.indexOf(button);
					if (avatarIndex != index) {
						avatarIndex = index;
						btnSalva.setEnabled(true);
						selectedAvatar = avatarList.get(avatarIndex);
						descriptionText.setText(selectedAvatar.getNome() + " " + selectedAvatar.getDescrizione());
					}
				}
			});
		}
		avatarPanel.add(buttonPanel, BorderLayout.CENTER);

		return avatarPanel;
	}

	private List<AvatarRadioButton> createAvatarButtons() {
		List<AvatarRadioButton> buttons = new ArrayList<>();

		List<String> avatarPaths = avatarList.stream().map(Avatar::getImagePath).collect(Collectors.toList());
		List<String> avatarName = avatarList.stream().map(Avatar::getNome).collect(Collectors.toList());

		for (int i = 0; i < avatarPaths.size(); i++) {
			ImageIcon icon = new ImageIcon(avatarPaths.get(i));
			Image scaledImage = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			AvatarRadioButton button = new AvatarRadioButton(new ImageIcon(scaledImage));
			final int index = i;
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					AudioManager.getInstance().play("assets/audio/greeting_" + avatarName.get(index) + ".wav");
				}
			});
			buttons.add(button);
		}

		if (!buttons.isEmpty()) {
			avatarIndex = json.getAvatar();
			selectedAvatar = avatarList.get(avatarIndex);
			buttons.get(avatarIndex).setSelected(true);
		}

		return buttons;
	}

	private JPanel createStatsPanel() {
		JPanel statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));

		JPanel titlePanel = new JPanel();
		JLabel titleLabel = new JLabel("Statistiche");
		Font titleFont = CustomFont.loadFont(Font.BOLD, 14);
		titleLabel.setFont(titleFont);
		titlePanel.add(titleLabel);

		statsPanel.add(titlePanel);
		statsPanel.add(createLabelAndTextField("Nome"));
		statsPanel.add(createLabel("Partite giocate", json.getPartiteGiocate()));
		statsPanel.add(createLabel("Partite vinte", json.getPartiteVinte()));
		statsPanel.add(createLabel("Partite perse", json.getPartitePerse()));
		statsPanel.add(createLabel("Livello", json.getLivello()));
		return statsPanel;
	}

	private JPanel createLabel(String labelName, int value) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel(labelName + ": " + value);
		if (labelName.equals("Livello"))
			label = new JLabel(labelName + ": " + value + "%");
		panel.add(label);
		return panel;
	}

	private JPanel createLabelAndTextField(String labelName) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel(labelName + ":");
		nicknameInput = new JTextField(10);
		nicknameInput.setText(json.getNickname());
		nicknameInput.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				btnSalva.setEnabled(true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				btnSalva.setEnabled(true);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				btnSalva.setEnabled(true);
			}
		});
		panel.add(label);
		panel.add(nicknameInput);
		return panel;
	}

	private JPanel createButtonPanel() {
		JPanel panelBottoni = new JPanel();
		btnSalva = new FuturisticButton("Salva");
		JButton btnMenu = new FuturisticButton("Torna al Menu");
		btnSalva.setEnabled(false);
		btnSalva.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				json.setAvatar(avatarList.indexOf(selectedAvatar));
				json.setNickname(nicknameInput.getText());
				json.setNomeAvatar(selectedAvatar.getNome());
				json.writeJsonStats();

				btnSalva.setEnabled(false);
			}
		});
		btnMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwitchPageController.showMenuScreen();
			}
		});
		panelBottoni.add(btnSalva);
		panelBottoni.add(btnMenu);
		return panelBottoni;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the paintComponent method to provide custom rendering behavior for
	 * this component. This method paints the background image (sfondo) within the
	 * panel, scaling the image to cover the entire component area.
	 *
	 * @param g The Graphics object to be used for rendering.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(sfondo, 0, 0, getWidth(), getHeight(), this);
	}
}