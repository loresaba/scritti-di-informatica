package controller;

import model.Giocatore;
import model.TrashGame;
import view.MainView;

/**
 * The main class that starts and manages the Trash game.
 * 
 * @author Lorenzo Sabatino
 */
public class JTrash {

	private static MainView mainView;
	private static TrashGame trashGame;
	private static boolean isGameRunning = true;

	/**
	 * The main method that starts the game.
	 * 
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		mainView = MainView.getInstance();
		mainView.setVisible(true);
		trashGame = TrashGame.getInstance();
	}

	/**
	 * Starts the Trash game and manages the turns of players.
	 */
	public static void startGame() {
		trashGame.iniziaNuovoRound();
		int turno = 0;

		while (isGameRunning && !trashGame.isFineGioco()) {
			System.out.println("****** Turno: " + turno + " ******");
			System.out.println("Dimensione mazzo: " + trashGame.getMazzi().size());

			for (Giocatore giocatore : trashGame.getGiocatori()) {
				if (!isGameRunning) {
					return;
				}

				if (giocatore.equals(trashGame.getMioGiocatore())) {
					// Human player's turn
					GiocatoreController.setMioTurno(true);
					while (isGameRunning && GiocatoreController.isMioTurno()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (!isGameRunning) {
							return;
						}
					}
				} else {
					// AI player's turn
					giocatore.giocaTurno(trashGame.getScarti(), trashGame.getMazzi());
				}

				if (giocatore.hasWon()) {
					break; // Exit the loop if a player has won
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			turno++;
		}

		// Game has ended
		if (isGameRunning) {
			SwitchPageController.showMenuScreen();
			JsonStatsController jsc = new JsonStatsController();
			jsc.updateStats();
			System.out.println("scrivo i DATI");
			setGameRunning(false);
		}
		System.out.println("FINE gioco");
	}

	/**
	 * Sets the status of the game running.
	 * 
	 * @param isGameRunning True to indicate the game is running, false otherwise.
	 */
	public static void setGameRunning(boolean isGameRunning) {
		JTrash.isGameRunning = isGameRunning;
	}

}