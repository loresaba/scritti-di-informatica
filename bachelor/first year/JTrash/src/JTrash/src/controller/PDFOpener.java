package controller;

import java.awt.*;
import java.io.*;

/**
 * A utility class for opening PDF files using the default PDF viewer.
 * 
 * @author Lorenzo Sabatino
 */
public class PDFOpener {

	/**
	 * Opens a PDF file using the default PDF viewer.
	 *
	 * @param pdfFilePath The path to the PDF file to be opened.
	 */
	public static void openPDFFile(String pdfFilePath) {
		try {
			File file = new File(pdfFilePath);
			if (!file.exists()) {
				System.err.println("File not found: " + pdfFilePath);
				return;
			}

			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.OPEN)) {
					desktop.open(file);
				} else {
					System.err.println("Open action not supported on this platform.");
				}
			} else {
				System.err.println("Desktop not supported on this platform.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}