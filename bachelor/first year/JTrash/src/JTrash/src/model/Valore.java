package model;

/**
 * Enumeration representing the values of playing cards.
 * 
 * @author Lorenzo Sabatino
 */
public enum Valore {

	ASSO(1), DUE(2), TRE(3), QUATTRO(4), CINQUE(5), SEI(6), SETTE(7), OTTO(8), NOVE(9), DIECI(10), JACK(11), DONNA(12),
	RE(13);

	int valoreIntero;

	/**
	 * Constructs a Valore enum value with the specified integer value.
	 *
	 * @param valoreIntero The integer value associated with the Valore enum value.
	 */
	Valore(int valoreIntero) {
		this.valoreIntero = valoreIntero;
	}

	/**
	 * Returns the integer value associated with the Valore enum value.
	 *
	 * @return The integer value of the Valore enum value.
	 */
	public int getValoreIntero() {
		return valoreIntero;
	}

	/**
	 * Returns the Valore enum value corresponding to the given integer index.
	 *
	 * @param index The integer index for which to retrieve the Valore enum value.
	 * @return The Valore enum value corresponding to the given index, or null if
	 *         not found.
	 */
	public static Valore getValore(int index) {
		for (Valore valore : Valore.values()) {
			if (valore.getValoreIntero() == index) {
				return valore;
			}
		}
		return null;
	}

}