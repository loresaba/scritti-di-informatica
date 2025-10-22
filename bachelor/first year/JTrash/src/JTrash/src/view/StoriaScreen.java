package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controller.SwitchPageController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Represents the story screen where users can read the background story of the
 * game.
 * 
 * @author Lorenzo Sabatino
 */
public class StoriaScreen extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image sfondo;

	/**
	 * Constructs a new StoriaScreen instance.
	 */
	public StoriaScreen() {
		setLayout(new GridBagLayout());
		sfondo = new ImageIcon("assets/storia.jpg").getImage();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 0, 10, 0);

		JPanel contentPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbcPanel = new GridBagConstraints();
		gbcPanel.anchor = GridBagConstraints.NORTH;
		gbcPanel.insets = new Insets(5, 5, 5, 5);

		gbcPanel.gridx = 0;
		gbcPanel.gridy = 0;

		JLabel titleLabel = new JLabel("La Carta del Destino: L'inizio di JTrash");
		titleLabel.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 22));

		JTextArea testo = new JTextArea();
		testo.setLineWrap(true);
		testo.setWrapStyleWord(true);
		testo.setEditable(false);
		testo.setText(
				"Nel cuore di un futuro apocalittico, dove le città sono soffocate dalla sporcizia e l'inquinamento avvolge la Terra, una luce di speranza si nasconde tra i cumuli di rifiuti. Finn Anderson, un giovane e coraggioso individuo, vive in una di queste città devastate, testimone del caos e della disperazione che la spazzatura ha portato nel mondo.\r\n"
						+ "\r\n"
						+ "Fin da bambino, ha vissuto in questo ambiente degradato, ma il suo spirito è stato nutrito da un ardente desiderio di cambiare le sorti del pianeta e di riportare la Terra al suo antico splendore. In un giorno come tanti, mentre rovista in un mucchio di rifiuti per trovare qualcosa di utile per la sua sopravvivenza, scopre qualcosa di straordinario: una carta dall'aspetto scintillante, diversa da tutte le altre, e con un enigmatico simbolo al retro.\r\n"
						+ "\r\n"
						+ "Incuriosito e affascinato dalla scoperta, inizia a indagare sull'origine della misteriosa carta. Tra i pochi sopravvissuti, le voci parlano di un leggendario gioco di carte chiamato JTrash. Si dice che il vincitore di JTrash possa ottenere un potere senza pari, capace di cambiare il corso della storia e restituire la prosperità alla Terra, liberandola dalla schiavitù della spazzatura.\r\n"
						+ "\r\n"
						+ "Deciso a dare una speranza al suo mondo, Finn si immerge completamente in JTrash. Durante il suo viaggio, incontra Mia Rivers, una giovane biologa dallo spirito libero, che condivide la sua passione per la natura e gli animali. Insieme, formano un'inaspettata e preziosa alleanza, unendo le loro conoscenze e abilità per affrontare le sfide che li attendono.\r\n"
						+ "\r\n"
						+ "Durante il loro cammino, Finn e Mia incontrano anche Oliver Steele, un abile ingegnere e inventore, e Sage Kimura, un anziano sciamano e custode delle antiche conoscenze e tradizioni del passato.\r\n"
						+ "\r\n"
						+ "Mentre Finn, Mia, Oliver e Sage continuano il loro viaggio attraverso il mondo devastato dall'inquinamento, si imbattono nella presenza onnipresente dell'Inquinamento Corp. Questa potente corporation è responsabile di numerose industrie inquinanti che hanno contribuito alla distruzione ambientale. Le loro ciminiere tossiche, gli scarichi di rifiuti industriali e il disprezzo per l'ecologia hanno avvelenato il pianeta e hanno alimentato il caos che circonda le città.\r\n"
						+ "\r\n"
						+ "L'Inquinamento Corp è guidata da un magnate spietato di nome Dr. Reginald Blackwood, un uomo senza scrupoli il cui unico obiettivo è accumulare ricchezza e potere a discapito dell'ambiente e delle persone. Vede la minaccia rappresentata da Finn e la sua squadra di \"ecologisti ribelli\" come una sfida diretta ai suoi interessi, e quindi decide di fermarli ad ogni costo.\r\n"
						+ "\r\n"
						+ "La sfida è enorme e le carte sono state messe sul tavolo: la partita di JTrash può determinare il destino del mondo intero. Con coraggio, determinazione e la forza della loro alleanza, il gruppo si appresta a prendere parte a questa avventura sperando di vincere la partita di carte che potrebbe salvare il mondo dalla catastrofe ambientale.\r\n"
						+ "\r\n"
						+ "Saranno in grado di superare gli ostacoli e le insidie del gioco, oppure il mondo rimarrà prigioniero della spazzatura e dell'inquinamento? La risposta sta nel cuore del gioco, e solo tu puoi scrivere l'epilogo di questa straordinaria avventura.");
		testo.setCaretPosition(0);
		Font font = new Font("Serif", Font.ITALIC, 14);
		testo.setFont(font);
		testo.setBorder(new EmptyBorder(10, 10, 10, 10));
		testo.setOpaque(false);
		JScrollPane scrollPane = new JScrollPane(testo);
		scrollPane.setPreferredSize(new Dimension(500, 300));
		scrollPane.setBorder(null);

		JButton btnMenu = new FuturisticButton("Torna al Menu");
		btnMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwitchPageController.showMenuScreen();
			}
		});
		btnMenu.setAlignmentX(CENTER_ALIGNMENT);
		contentPanel.add(titleLabel, gbcPanel);
		gbcPanel.gridy = 1;
		contentPanel.add(scrollPane, gbcPanel);
		gbcPanel.gridy = 2;
		contentPanel.add(btnMenu, gbcPanel);
		add(contentPanel, gbc);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the paintComponent method to provide custom rendering behavior for
	 * this component. This method paints the specified background image within the
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