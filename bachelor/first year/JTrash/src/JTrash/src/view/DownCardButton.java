package view;

/**
 * A custom button for displaying facedown cards on the game board.
 * 
 * @author Lorenzo Sabatino
 */
public class DownCardButton extends CardButton {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new facedown card button with the specified card width.
	 *
	 * @param cardWidth The width of the card.
	 */
	public DownCardButton(int cardWidth) {
		super("assets/cards/card.jpg", cardWidth);
	}

}
