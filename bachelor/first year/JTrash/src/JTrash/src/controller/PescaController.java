package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import model.TrashGame;

/**
 * ActionListener implementation for handling the draw action.
 * 
 * @author Lorenzo Sabatino
 */
public class PescaController implements ActionListener {

	/**
	 * Handles the "Pesca" action.
	 *
	 * If it's the appropriate time for the player to draw a card, this method
	 * updates the game state accordingly.
	 *
	 * @param e The ActionEvent object representing the action.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (GiocatoreController.isPescaTime()) {
			GiocatoreController.setPescaTime(false);
			GiocatoreController.setScambiaTime(true);
			GiocatoreController.setScartaTime(true);
			TrashGame trashGame = TrashGame.getInstance();
			trashGame.getMioGiocatore().pesca(trashGame.getMazzi());
		}
	}

}