package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.Giocatore;
import model.TrashGame;

/**
 * ActionListener implementation for handling the swap action.
 * 
 * @author Lorenzo Sabatino
 */
public class ScambiaController implements ActionListener {

	private int index;

	/**
	 * Creates a ScambiaController instance with the specified index.
	 *
	 * @param index The index of the card to be swapped.
	 */
	public ScambiaController(int index) {
		this.index = index;
	}

	/**
	 * Handles the "Scambia" action.
	 *
	 * If it's the appropriate time for the player to swap a card on the table, this
	 * method updates the game state accordingly.
	 *
	 * @param e The ActionEvent object representing the action.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (GiocatoreController.isScambiaTime()) {
			GiocatoreController.setScartaTime(true);
			TrashGame trashGame = TrashGame.getInstance();
			Giocatore giocatore = trashGame.getMioGiocatore();
			if (giocatore.isValidCarta(giocatore.getCartaInMano()))
				giocatore.scambiaCartaSuTavolo(index);
		}
	}

}