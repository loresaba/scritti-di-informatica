package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

import model.TrashGame;

/**
 * This class manages reading and writing player statistics to a JSON file.
 * 
 * @author Lorenzo Sabatino
 */
public class JsonStatsController {

	private String nickname;
	private int avatar;
	private int partiteGiocate;
	private int partiteVinte;
	private int partitePerse;
	private int livello;
	private String nomeAvatar;
	private final String filePath = "assets/stats.json";

	/**
	 * Reads player statistics from the JSON file.
	 */
	public void readJsonStats() {
		try {
			StringBuilder content = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
			reader.close();

			JSONObject jsonObject = new JSONObject(content.toString());

			nickname = jsonObject.getString("nickname");
			avatar = jsonObject.getInt("avatar");
			partiteGiocate = jsonObject.getInt("partite giocate");
			partiteVinte = jsonObject.getInt("partite vinte");
			partitePerse = jsonObject.getInt("partite perse");
			livello = jsonObject.getInt("livello");
			nomeAvatar = jsonObject.getString("nome avatar");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes player statistics to the JSON file.
	 */
	public void writeJsonStats() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("nickname", nickname);
		jsonObject.put("avatar", avatar);
		jsonObject.put("partite giocate", partiteGiocate);
		jsonObject.put("partite vinte", partiteVinte);
		jsonObject.put("partite perse", partitePerse);
		jsonObject.put("livello", livello);
		jsonObject.put("nome avatar", nomeAvatar);
		try (FileWriter fileWriter = new FileWriter(filePath)) {
			fileWriter.write(jsonObject.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the player's nickname.
	 *
	 * @return The player's nickname.
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Sets the player's nickname.
	 *
	 * @param nickname The player's nickname.
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * Gets the player's selected avatar.
	 *
	 * @return The player's selected avatar.
	 */
	public int getAvatar() {
		return avatar;
	}

	/**
	 * Sets the player's selected avatar.
	 *
	 * @param avatar The player's selected avatar.
	 */
	public void setAvatar(int avatar) {
		this.avatar = avatar;
	}

	/**
	 * Gets the number of games played by the player.
	 *
	 * @return The number of games played by the player.
	 */
	public int getPartiteGiocate() {
		return partiteGiocate;
	}

	/**
	 * Sets the number of games played by the player.
	 *
	 * @param partiteGiocate The number of games played by the player.
	 */
	public void setPartiteGiocate(int partiteGiocate) {
		this.partiteGiocate = partiteGiocate;
	}

	/**
	 * Gets the number of games won by the player.
	 *
	 * @return The number of games won by the player.
	 */
	public int getPartiteVinte() {
		return partiteVinte;
	}

	/**
	 * Sets the number of games won by the player.
	 *
	 * @param partiteVinte The number of games won by the player.
	 */
	public void setPartiteVinte(int partiteVinte) {
		this.partiteVinte = partiteVinte;
	}

	/**
	 * Gets the number of games lost by the player.
	 *
	 * @return The number of games lost by the player.
	 */
	public int getPartitePerse() {
		return partitePerse;
	}

	/**
	 * Sets the number of games lost by the player.
	 *
	 * @param partitePerse The number of games lost by the player.
	 */
	public void setPartitePerse(int partitePerse) {
		this.partitePerse = partitePerse;
	}

	/**
	 * Gets the player's level based on games won and played.
	 *
	 * @return The player's level.
	 */
	public int getLivello() {
		return livello;
	}

	/**
	 * Sets the player's level based on games won and played.
	 *
	 * @param livello The player's level.
	 */
	public void setLivello(int livello) {
		this.livello = livello;
	}

	/**
	 * Gets the name of the player's avatar.
	 *
	 * @return The name of the player's avatar.
	 */
	public String getNomeAvatar() {
		return nomeAvatar;
	}

	/**
	 * Sets the name of the player's avatar.
	 *
	 * @param nomeAvatar The name of the player's avatar.
	 */
	public void setNomeAvatar(String nomeAvatar) {
		this.nomeAvatar = nomeAvatar;
	}

	/**
	 * Gets the file path of the JSON stats file.
	 *
	 * @return The file path of the JSON stats file.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Updates the player statistics based on the game outcome.
	 */
	public void updateStats() {
		readJsonStats();
		partiteGiocate++;
		if (TrashGame.getInstance().getMioGiocatore().getNumeroDiCarte() == 0)
			partiteVinte++;
		else
			partitePerse++;
		livello = partiteVinte * 100 / partiteGiocate;
		writeJsonStats();
	}

}