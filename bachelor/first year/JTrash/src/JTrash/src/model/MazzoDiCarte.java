package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a deck of playing cards.
 * 
 * @author Lorenzo Sabatino
 */
public class MazzoDiCarte {

	private List<Carta> mazzo;

	/**
	 * Constructs a new deck of cards containing all possible cards.
	 */
	public MazzoDiCarte() {
		mazzo = new ArrayList<>();
		mazzo.addAll(List.of(Carta.values()));
	}

	/**
	 * Retrieves the list of cards in the deck.
	 *
	 * @return The list of cards in the deck.
	 */
	public List<Carta> getMazzo() {
		return mazzo;
	}

	/**
	 * Returns a string representation of the deck.
	 *
	 * @return The string representation of the deck.
	 */
	@Override
	public String toString() {
		return mazzo.toString();
	}

}