package controller;

import view.GiocaScreen;
import view.MainView;
import view.MenuPanel;
import view.ProfiloScreen;
import view.StoriaScreen;

/**
 * Controller class for switching between different screens in the game.
 * 
 * @author Lorenzo Sabatino
 */
public class SwitchPageController {

	private static MainView mainView = MainView.getInstance();

	/**
	 * Shows the menu screen by removing the current panel and displaying the
	 * MenuPanel.
	 */
	public static void showMenuScreen() {
		// Rimuovi il pannello corrente e mostra il GiocaScreen
		MenuPanel menuPanel = new MenuPanel();
		mainView.setCurrentPanel(menuPanel);
	}

	/**
	 * Shows the "Gioca" (Play) screen by removing the current panel and displaying
	 * the GiocaScreen.
	 */
	public static void showGiocaScreen() {
		// Rimuovi il pannello corrente e mostra il GiocaScreen
		GiocaScreen giocaScreen = new GiocaScreen();
		mainView.setCurrentPanel(giocaScreen);
	}

	/**
	 * Shows the "Profilo" (Profile) screen by removing the current panel and
	 * displaying the ProfiloScreen.
	 */
	public static void showProfiloScreen() {
		// Rimuovi il pannello corrente e mostra il ProfiloScreen
		ProfiloScreen profiloScreen = new ProfiloScreen();
		mainView.setCurrentPanel(profiloScreen);
	}

	/**
	 * Shows the "Storia" (Story) screen by removing the current panel and
	 * displaying the StoriaScreen.
	 */
	public static void showStoriaScreen() {
		// Rimuovi il pannello corrente e mostra il StoriaScreen
		StoriaScreen storiaScreen = new StoriaScreen();
		mainView.setCurrentPanel(storiaScreen);
	}

}