package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a player in the card game.
 * 
 * @author Lorenzo Sabatino
 */
public class Giocatore extends Observable {

	private String nome;
	private List<Carta> carteSuTavolo;
	private Map<Valore, Boolean> statoDelleCarte;
	private Carta cartaInMano;
	private int numeroDiCarte;
	private int indice;
	private List<Integer> jollyIndex;

	/**
	 * Constructs a player with the given name.
	 *
	 * @param nome The name of the player.
	 */
	public Giocatore(String nome) {
		this.nome = nome;
		numeroDiCarte = 10;
		initStato();
	}

	/**
	 * Initializes the state of the player's cards.
	 */
	public void initStato() {
		carteSuTavolo = new ArrayList<>();
		statoDelleCarte = Stream.of(Carta.values()).limit(numeroDiCarte)
				.collect(Collectors.toMap(carta -> carta.getValore(), carta -> false));
	}

	/**
	 * Adds a card to the player's table.
	 *
	 * @param carta The card to be added.
	 */
	public void aggiungiCartaSuTavolo(Carta carta) {
		carteSuTavolo.add(carta);
	}

	/**
	 * Executes the player's turn during the game.
	 *
	 * @param scarti The list of discarded cards.
	 * @param mazzi  The list of remaining cards in the deck.
	 */
	public void giocaTurno(List<Carta> scarti, List<Carta> mazzi) {
		Carta cartaDaScarti = scarti.get(scarti.size() - 1);
		if (isValidCarta(cartaDaScarti)) {
			pescaDagliScarti(scarti);
			System.out.println(nome + " pesco dagli scarti: " + cartaInMano);
		} else {
			pesca(mazzi);
			System.out.println(nome + " pesco dal mazzo: " + cartaInMano);
		}
		sleep();
		while (isValidCarta(cartaInMano)) {
			System.out.println(nome + " carta in mano: " + cartaInMano);
			scambiaCartaSuTavolo(indice);
			sleep();
		}
		scarta(scarti);
	}

	/**
	 * Checks if a card is valid for the player to play.
	 *
	 * @param carta The card to be checked.
	 * @return True if the card is valid, false otherwise.
	 */
	public boolean isValidCarta(Carta carta) {
		if (isCarteGirate())
			// Cannot play a card if there are face-down cards
			return false;

		if (Carta.isJolly(carta)) {
			jollyIndex = new ArrayList<>();

			// Check for available face-down cards to replace with jolly
			for (Map.Entry<Valore, Boolean> entry : statoDelleCarte.entrySet())
				if (!entry.getValue())
					jollyIndex.add(entry.getKey().getValoreIntero() - 1);
			indice = jollyIndex.get(0);
			return true;
		} else if (statoDelleCarte.containsKey(carta.getValore()) && statoDelleCarte.get(carta.getValore()) == false) {
			// Check if the card's state is false (face-down)
			indice = carta.getValore().getValoreIntero() - 1;
			return true;
		} else if (statoDelleCarte.containsKey(carta.getValore())
				&& Carta.isJolly(carteSuTavolo.get(carta.getValore().getValoreIntero() - 1))) {
			// Check if there is a jolly on the table at the card's position
			indice = carta.getValore().getValoreIntero() - 1;
			return true;
		}
		return false;
	}

	/**
	 * Swaps a card on the player's table with the card in hand.
	 *
	 * @param index The index of the card to be swapped.
	 */
	public void scambiaCartaSuTavolo(int index) {
		if (Carta.isJolly(cartaInMano)) {
			if (jollyIndex.contains(index)) {
				statoDelleCarte.put(Valore.getValore(index + 1), true);
				// Swap with face-down card if the card in hand is a jolly
				scambia(index);
			}
		} else {
			// Swap with the card of the same value if not a jolly
			if (index == indice) {
				statoDelleCarte.put(cartaInMano.getValore(), true);
				scambia(indice);
			}
		}
	}

	/**
	 * Swaps a card with another card on the player's table.
	 *
	 * @param index The index of the card to be swapped.
	 */
	private void scambia(int index) {
		Carta cartaDaScambiare = carteSuTavolo.get(index);
		carteSuTavolo.set(index, cartaInMano);
		cartaInMano = cartaDaScambiare;
		notifica("scambia", cartaDaScambiare, index);
		System.out.println(nome + " carta scambiata");
		System.out.println(nome + carteSuTavolo);
		System.out.println(nome + statoDelleCarte);
	}

	/**
	 * Draws a card from the deck.
	 *
	 * @param carte The list of remaining cards in the deck.
	 */
	public void pesca(List<Carta> carte) {
		cartaInMano = carte.remove(carte.size() - 1);
		notifica("pesca", cartaInMano);
	}

	/**
	 * Draws a card from the discarded cards.
	 *
	 * @param carte The list of discarded cards.
	 */
	public void pescaDagliScarti(List<Carta> carte) {
		cartaInMano = carte.remove(carte.size() - 1);
		notifica("pesca scarti", cartaInMano);
	}

	/**
	 * Discards the card in hand.
	 *
	 * @param scarti The list of discarded cards.
	 */
	public void scarta(List<Carta> scarti) {
		Carta cartaDaScartare = cartaInMano;
		cartaInMano = null;
		notifica("scarta", cartaDaScartare);
		System.out.println(nome + " carta scartata: " + cartaDaScartare);
		scarti.add(cartaDaScartare);
	}

	private boolean isCarteGirate() {
		return statoDelleCarte.values().stream().allMatch(Boolean::booleanValue);
	}

	/**
	 * Checks if the player has won the round.
	 *
	 * @return True if the player has won, false otherwise.
	 */
	public boolean hasWon() {
		if (isCarteGirate()) {
			System.out.println("############# " + getNome() + " ha VINTO il ROUND #############");
			numeroDiCarte--;
			TrashGame.getInstance().iniziaNuovoRound();
			notifica("vittoria");
			return true;
		}
		return false;
	}

	private void notifica(String message, Object... obj) {
		setChanged();

		List<Object> notificationList = new ArrayList<>();
		notificationList.add(message);
		notificationList.addAll(Arrays.asList(obj));

		notifyObservers(notificationList);
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the name of the player.
	 *
	 * @return The name of the player.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Sets the name of the player.
	 *
	 * @param nome The name of the player.
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Gets the number of cards held by the player.
	 *
	 * @return The number of cards held by the player.
	 */
	public int getNumeroDiCarte() {
		return numeroDiCarte;
	}

	/**
	 * Sets the number of cards held by the player.
	 *
	 * @param numeroDiCarte The number of cards held by the player.
	 */
	public void setNumeroDiCarte(int numeroDiCarte) {
		this.numeroDiCarte = numeroDiCarte;
	}

	/**
	 * Gets the list of cards on the player's table.
	 *
	 * @return The list of cards on the player's table.
	 */
	public List<Carta> getCarteSuTavolo() {
		return carteSuTavolo;
	}

	/**
	 * Gets the card in the player's hand.
	 *
	 * @return The card in the player's hand.
	 */
	public Carta getCartaInMano() {
		return cartaInMano;
	}

	/**
	 * Gets the state of the player's cards.
	 *
	 * @return The state of the player's cards.
	 */
	public Map<Valore, Boolean> getStatoDelleCarte() {
		return statoDelleCarte;
	}

	/**
	 * Returns a string representation of the Giocatore object. This method provides
	 * a formatted string containing information about the player's name, cards on
	 * the table, card states, card in hand, and the total number of cards.
	 *
	 * @return A string representation of the Giocatore object.
	 */
	@Override
	public String toString() {
		return "Giocatore [nome=" + nome + ", carteSuTavolo=" + carteSuTavolo + ", statoDelleCarte=" + statoDelleCarte
				+ ", cartaInMano=" + cartaInMano + ", numeroDiCarte=" + numeroDiCarte + "]";
	}

}