package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.TrashGame;

/**
 * ActionListener implementation for handling the discard action.
 * 
 * @author Lorenzo Sabatino
 */
public class ScartaController implements ActionListener {

	/**
	 * Handles the "Scarta" action.
	 *
	 * If it's the appropriate time for the player to discard a card, this method
	 * updates the game state accordingly.
	 *
	 * @param e The ActionEvent object representing the action.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (GiocatoreController.isScartaTime()) {
			GiocatoreController.setMioTurno(false);
			TrashGame trashGame = TrashGame.getInstance();
			trashGame.getMioGiocatore().scarta(trashGame.getScarti());
		}
	}

}