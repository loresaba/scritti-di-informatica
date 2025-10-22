package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import model.Giocatore;
import model.TrashGame;

/**
 * This class manages the game actions and controls related to players.
 * 
 * @author Lorenzo Sabatino
 */
public class GiocatoreController {

	private static boolean isPescaTime = true;
	private static boolean isScambiaTime;
	private static boolean isScartaTime;
	private static boolean isMioTurno = true;
	private static TrashGame trashGame = TrashGame.getInstance();

	/**
	 * Checks if it's time for a player to discard a card.
	 * 
	 * @return True if it's time to discard, false otherwise.
	 */
	public static boolean isScartaTime() {
		return isScartaTime;
	}

	/**
	 * Sets whether it's time for a player to discard a card.
	 * 
	 * @param isScartaTime True if it's time to discard, false otherwise.
	 */
	public static void setScartaTime(boolean isScartaTime) {
		GiocatoreController.isScartaTime = isScartaTime;
	}

	/**
	 * Checks if it's time for a player to exchange cards.
	 * 
	 * @return True if it's time to exchange, false otherwise.
	 */
	public static boolean isScambiaTime() {
		return isScambiaTime;
	}

	/**
	 * Sets whether it's time for a player to exchange cards.
	 * 
	 * @param isScambiaTime True if it's time to exchange, false otherwise.
	 */
	public static void setScambiaTime(boolean isScambiaTime) {
		GiocatoreController.isScambiaTime = isScambiaTime;
	}

	/**
	 * Checks if it's time for a player to draw a card.
	 * 
	 * @return True if it's time to draw, false otherwise.
	 */
	public static boolean isPescaTime() {
		return isPescaTime;
	}

	/**
	 * Sets whether it's time for a player to draw a card.
	 * 
	 * @param isPescaTime True if it's time to draw, false otherwise.
	 */
	public static void setPescaTime(boolean isPescaTime) {
		GiocatoreController.isPescaTime = isPescaTime;
	}

	/**
	 * Checks if it's the current player's turn.
	 * 
	 * @return True if it's the current player's turn, false otherwise.
	 */
	public static boolean isMioTurno() {
		return isMioTurno;
	}

	/**
	 * Sets whether it's the current player's turn.
	 * 
	 * @param isMioTurno True if it's the current player's turn, false otherwise.
	 */
	public static void setMioTurno(boolean isMioTurno) {
		GiocatoreController.isMioTurno = isMioTurno;
		if (isMioTurno)
			setPescaTime(true);
		else {
			setPescaTime(false);
			setScambiaTime(false);
			setScartaTime(false);
		}
	}

	/**
	 * Sets the number of players and initializes the game.
	 * 
	 * @param numeroDiGiocatori The number of players in the game.
	 */
	public static void setGiocatori(int numeroDiGiocatori) {
		TrashGame trashGame = TrashGame.getInstance();
		trashGame.setNumeroDiGiocatori(numeroDiGiocatori);
		List<Giocatore> giocatori = GiocatoreController.getGiocatori(numeroDiGiocatori);
		trashGame.setGiocatori(giocatori);
	}

	/**
	 * Gets a list of player objects based on the number of players.
	 * 
	 * @param numeroDiGiocatori The number of players.
	 * @return A list of Giocatore objects representing players.
	 */
	public static List<Giocatore> getGiocatori(int numeroDiGiocatori) {
		List<Giocatore> giocatori = new ArrayList<>();

		// Initialize a JsonStatsController to read player data
		JsonStatsController jsc = new JsonStatsController();
		jsc.readJsonStats();
		// Always add the current player using the user's avatar name
		giocatori.add(new Giocatore(jsc.getNomeAvatar()));
		// Add standard AI players based on the number of players
		giocatori.add(new Giocatore("Dr. Reginald Blackwood"));
		if (numeroDiGiocatori >= 3)
			giocatori.add(new Giocatore("Helga Duvall"));
		if (numeroDiGiocatori >= 4)
			giocatori.add(new Giocatore("Victor Thorncrest"));
		return giocatori;
	}

	/**
	 * Gets the number of cards held by a player.
	 * 
	 * @param giocatoreIndex The index of the player.
	 * @return The number of cards held by the player.
	 */
	public static int getNumeroCarte(int giocatoreIndex) {
		return trashGame.getGiocatori().get(giocatoreIndex).getNumeroDiCarte();
	}

	/**
	 * Adds an observer to a player's statistics.
	 * 
	 * @param obs   The observer to add.
	 * @param index The index of the player.
	 */
	public static void addObserver(Observer obs, int index) {
		Giocatore giocatore = trashGame.getGiocatori().get(index);
		giocatore.deleteObservers();
		giocatore.addObserver(obs);
	}

}