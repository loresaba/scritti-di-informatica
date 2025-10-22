package view;

import java.util.List;

import controller.JsonStatsController;

/**
 * Represents an avatar in the game, containing information such as name, image
 * path, and description.
 * 
 * @author Lorenzo Sabatino
 */
public class Avatar {

	private String nome;
	private String imagePath;
	private String descrizione;

	/**
	 * Constructs an Avatar object with the specified details.
	 *
	 * @param nome        The name of the avatar.
	 * @param imagePath   The file path to the avatar image.
	 * @param descrizione A description of the avatar.
	 */
	public Avatar(String nome, String imagePath, String descrizione) {
		this.nome = nome;
		this.imagePath = imagePath;
		this.descrizione = descrizione;
	}

	/**
	 * Returns the name of the avatar.
	 *
	 * @return The name of the avatar.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Returns the file path to the avatar image.
	 *
	 * @return The image path of the avatar.
	 */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * Returns the description of the avatar.
	 *
	 * @return The description of the avatar.
	 */
	public String getDescrizione() {
		return descrizione;
	}

	/**
	 * Returns a list of predefined avatars with their names, image paths, and
	 * descriptions.
	 *
	 * @return A list of Avatar objects representing predefined avatars.
	 */
	public static List<Avatar> getAvatar() {
		return List.of(new Avatar("Finn Anderson", "assets/avatar/avatar1.jpg",
				"è un'anima generosa, con un forte senso di giustizia. La scoperta della misteriosa carta scintillante è solo l'inizio di un viaggio che lo porterà a scoprire il leggendario gioco di carte JTrash e la sua importanza per il destino del mondo."),
				new Avatar("Mia Rivers", "assets/avatar/avatar2.jpg",
						"è una giovane biologa dallo spirito libero e un'anima avventurosa. Il suo entusiasmo contagioso, la conoscenza e il rispetto per la flora e la fauna la rendono un'aggiunta preziosa alla squadra."),
				new Avatar("Oliver Steele", "assets/avatar/avatar3.jpg",
						"è un abile ingegnere e inventore, esperto nel creare dispositivi e macchine utili per la sopravvivenza. La sua creatività sarà fondamentale nell'elaborare strategie innovative durante le sfide di JTrash."),
				new Avatar("Sage Kimura", "assets/avatar/avatar4.jpg",
						"è un anziano sciamano e custode delle antiche conoscenze e tradizioni del passato. La sua saggezza e guida spirituale saranno fondamentali per il viaggio del gruppo."));
	}

	/**
	 * Retrieves an avatar from the predefined list by index.
	 *
	 * @param index The index of the avatar to retrieve.
	 * @return The Avatar object corresponding to the given index.
	 */
	public static Avatar getAvatarBoardByIndex(int index) {
		if (index == 0) {
			// Retrieve the avatar based on stored JSON data
			JsonStatsController jsc = new JsonStatsController();
			jsc.readJsonStats();
			return getAvatar().get(jsc.getAvatar());
		} else if (index == 1)
			return new Avatar("Dr. Reginald Blackwood", "assets/avatar/Dr. Reginald Blackwood.jpg", "");
		else if (index == 2)
			return new Avatar("Helga Duvall", "assets/avatar/Helga Duvall.jpg", "");
		else
			return new Avatar("Victor Thorncrest", "assets/avatar/Victor Thorncrest.jpg", "");
	}

}
