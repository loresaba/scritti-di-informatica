package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the Trash Card Game.
 * 
 * @author Lorenzo Sabatino
 */
public class TrashGame {

	private static TrashGame instance;
	private List<Carta> mazzi;
	private List<Carta> scarti;
	private List<Giocatore> giocatori;
	private int numeroDiGiocatori;

	private TrashGame() {
		giocatori = new ArrayList<>();
	}

	/**
	 * Returns the singleton instance of the TrashGame class.
	 *
	 * @return The singleton instance of TrashGame.
	 */
	public static TrashGame getInstance() {
		if (instance == null)
			instance = new TrashGame();
		return instance;
	}

	/**
	 * Initializes a new round of the game, including cleaning the table, adding
	 * decks, shuffling decks, distributing cards, and initializing the discard
	 * pile.
	 */
	public void iniziaNuovoRound() {
		pulisciTavolo();
		addMazzi();
		mescolaMazzi();
		distribuisciCarte();
	}

	private void addMazzi() {
		// Calculate the number of decks needed (rounded up)
		for (int i = 0; i < (int) Math.ceil((double) numeroDiGiocatori / 2); i++) {
			mazzi.addAll(new MazzoDiCarte().getMazzo());
		}
	}

	private void mescolaMazzi() {
		Random random = new Random();
		for (int i = 0; i < mazzi.size(); i++) {
			int k = random.nextInt(mazzi.size());
			Collections.swap(mazzi, i, k);
		}
	}

	private void pulisciTavolo() {
		mazzi = new ArrayList<>();
		scarti = new ArrayList<>();
		for (Giocatore giocatore : giocatori)
			giocatore.initStato();
	}

	private void distribuisciCarte() {
		for (Giocatore giocatore : giocatori)
			for (int i = 0; i < giocatore.getNumeroDiCarte(); i++)
				giocatore.aggiungiCartaSuTavolo(mazzi.remove(mazzi.size() - 1));
	}

	/**
	 * Checks if the game has ended, if any player has no cards left.
	 *
	 * @return True if the game has ended, otherwise false.
	 */
	public boolean isFineGioco() {
		for (Giocatore giocatore : giocatori)
			if (giocatore.getNumeroDiCarte() == 0)
				return true;
		return false;
	}

	/**
	 * Returns the player associated with the local player.
	 *
	 * @return The player associated with the local player.
	 */
	public Giocatore getMioGiocatore() {
		return giocatori.get(0);
	}

	/**
	 * Returns the list of decks in the game.
	 *
	 * @return The list of decks in the game.
	 */
	public List<Carta> getMazzi() {
		return mazzi;
	}

	/**
	 * Returns the list of discarded cards.
	 *
	 * @return The list of discarded cards.
	 */
	public List<Carta> getScarti() {
		return scarti;
	}

	/**
	 * Sets the list of players in the game.
	 *
	 * @param giocatori The list of players to set.
	 */
	public void setGiocatori(List<Giocatore> giocatori) {
		this.giocatori = giocatori;
	}

	/**
	 * Returns the list of players in the game.
	 *
	 * @return The list of players in the game.
	 */
	public List<Giocatore> getGiocatori() {
		return giocatori;
	}

	/**
	 * Sets the number of players in the game.
	 *
	 * @param numeroDiGiocatori The number of players to set.
	 */
	public void setNumeroDiGiocatori(int numeroDiGiocatori) {
		this.numeroDiGiocatori = numeroDiGiocatori;
	}

	/**
	 * Returns the number of players in the game.
	 *
	 * @return The number of players in the game.
	 */
	public int getNumeroDiGiocatori() {
		return numeroDiGiocatori;
	}

}