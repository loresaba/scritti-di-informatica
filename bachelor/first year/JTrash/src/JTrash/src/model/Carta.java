package model;

/**
 * Enum representing a playing card.
 * 
 * @author Lorenzo Sabatino
 */
public enum Carta {

	ASSO_DI_PICCHE(Valore.ASSO, Seme.PICCHE), DUE_DI_PICCHE(Valore.DUE, Seme.PICCHE),
	TRE_DI_PICCHE(Valore.TRE, Seme.PICCHE), QUATTRO_DI_PICCHE(Valore.QUATTRO, Seme.PICCHE),
	CINQUE_DI_PICCHE(Valore.CINQUE, Seme.PICCHE), SEI_DI_PICCHE(Valore.SEI, Seme.PICCHE),
	SETTE_DI_PICCHE(Valore.SETTE, Seme.PICCHE), OTTO_DI_PICCHE(Valore.OTTO, Seme.PICCHE),
	NOVE_DI_PICCHE(Valore.NOVE, Seme.PICCHE), DIECI_DI_PICCHE(Valore.DIECI, Seme.PICCHE),
	JACK_DI_PICCHE(Valore.JACK, Seme.PICCHE), DONNA_DI_PICCHE(Valore.DONNA, Seme.PICCHE),
	RE_DI_PICCHE(Valore.RE, Seme.PICCHE), ASSO_DI_FIORI(Valore.ASSO, Seme.FIORI), DUE_DI_FIORI(Valore.DUE, Seme.FIORI),
	TRE_DI_FIORI(Valore.TRE, Seme.FIORI), QUATTRO_DI_FIORI(Valore.QUATTRO, Seme.FIORI),
	CINQUE_DI_FIORI(Valore.CINQUE, Seme.FIORI), SEI_DI_FIORI(Valore.SEI, Seme.FIORI),
	SETTE_DI_FIORI(Valore.SETTE, Seme.FIORI), OTTO_DI_FIORI(Valore.OTTO, Seme.FIORI),
	NOVE_DI_FIORI(Valore.NOVE, Seme.FIORI), DIECI_DI_FIORI(Valore.DIECI, Seme.FIORI),
	JACK_DI_FIORI(Valore.JACK, Seme.FIORI), DONNA_DI_FIORI(Valore.DONNA, Seme.FIORI),
	RE_DI_FIORI(Valore.RE, Seme.FIORI), ASSO_DI_QUADRI(Valore.ASSO, Seme.QUADRI),
	DUE_DI_QUADRI(Valore.DUE, Seme.QUADRI), TRE_DI_QUADRI(Valore.TRE, Seme.QUADRI),
	QUATTRO_DI_QUADRI(Valore.QUATTRO, Seme.QUADRI), CINQUE_DI_QUADRI(Valore.CINQUE, Seme.QUADRI),
	SEI_DI_QUADRI(Valore.SEI, Seme.QUADRI), SETTE_DI_QUADRI(Valore.SETTE, Seme.QUADRI),
	OTTO_DI_QUADRI(Valore.OTTO, Seme.QUADRI), NOVE_DI_QUADRI(Valore.NOVE, Seme.QUADRI),
	DIECI_DI_QUADRI(Valore.DIECI, Seme.QUADRI), JACK_DI_QUADRI(Valore.JACK, Seme.QUADRI),
	DONNA_DI_QUADRI(Valore.DONNA, Seme.QUADRI), RE_DI_QUADRI(Valore.RE, Seme.QUADRI),
	ASSO_DI_CUORI(Valore.ASSO, Seme.CUORI), DUE_DI_CUORI(Valore.DUE, Seme.CUORI), TRE_DI_CUORI(Valore.TRE, Seme.CUORI),
	QUATTRO_DI_CUORI(Valore.QUATTRO, Seme.CUORI), CINQUE_DI_CUORI(Valore.CINQUE, Seme.CUORI),
	SEI_DI_CUORI(Valore.SEI, Seme.CUORI), SETTE_DI_CUORI(Valore.SETTE, Seme.CUORI),
	OTTO_DI_CUORI(Valore.OTTO, Seme.CUORI), NOVE_DI_CUORI(Valore.NOVE, Seme.CUORI),
	DIECI_DI_CUORI(Valore.DIECI, Seme.CUORI), JACK_DI_CUORI(Valore.JACK, Seme.CUORI),
	DONNA_DI_CUORI(Valore.DONNA, Seme.CUORI), RE_DI_CUORI(Valore.RE, Seme.CUORI), JOLLY1(), JOLLY2();

	Valore valore;
	Seme seme;

	/**
	 * Default constructor for Jolly cards.
	 */
	Carta() {

	}

	/**
	 * Constructor for creating a playing card with a specified value and suit.
	 *
	 * @param valore The value of the card.
	 * @param seme   The suit of the card.
	 */
	Carta(Valore valore, Seme seme) {
		this.valore = valore;
		this.seme = seme;
	}

	/**
	 * Checks if the given card is a Jolly card or has a value of RE.
	 *
	 * @param carta The card to check.
	 * @return True if the card is a Jolly card or has a value of RE, false
	 *         otherwise.
	 */
	public static boolean isJolly(Carta carta) {
		if (carta.equals(Carta.JOLLY1) || carta.equals(Carta.JOLLY2) || carta.getValore().equals(Valore.RE))
			return true;
		return false;
	}

	/**
	 * Gets the value of the card.
	 *
	 * @return The value of the card.
	 */
	public Valore getValore() {
		return valore;
	}

	/**
	 * Gets the suit of the card.
	 *
	 * @return The suit of the card.
	 */
	public Seme getSeme() {
		return seme;
	}

}