package view;

/**
 * A custom button for displaying face-up cards on the game board.
 * 
 * @author Lorenzo Sabatino
 */
public class FaceUpCardButton extends CardButton {

	private static final long serialVersionUID = 1L;
	private String cardName;

	/**
	 * Creates a new face-up card button with the specified card name and width.
	 *
	 * @param cardName  The name of the card (used for loading the card image).
	 * @param cardWidth The width of the card.
	 */
	public FaceUpCardButton(String cardName, int cardWidth) {
		super("assets/cards/" + cardName + ".png", cardWidth);
		this.cardName = cardName;
	}

	/**
	 * Returns the name of the card associated with this button.
	 *
	 * @return The card name.
	 */
	@Override
	public String toString() {
		return cardName;
	}

}
