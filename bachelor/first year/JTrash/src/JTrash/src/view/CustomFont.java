package view;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.awt.FontFormatException;

/**
 * Utility class for loading custom fonts from external font files.
 * 
 * @author Lorenzo Sabatino
 */
public class CustomFont {
	/**
	 * Loads a custom font with the specified style and size from an external font
	 * file.
	 *
	 * @param style The style of the font (e.g., Font.BOLD, Font.ITALIC).
	 * @param size  The size of the font.
	 * @return The loaded custom font.
	 */
	public static Font loadFont(int style, float size) {
		try {
			String fontPath = "assets/Orbitron-VariableFont_wght.ttf";

			Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
			customFont = customFont.deriveFont(style, size);

			return customFont;
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

		return new Font("Arial", style, (int) size);
	}
}
