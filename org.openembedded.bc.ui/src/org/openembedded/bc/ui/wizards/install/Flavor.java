/**
 * 
 */
package org.openembedded.bc.ui.wizards.install;

import java.io.IOException;

// # Flavor Label (shows in UI)| description text| image URL| script URL

/**
 * Data class for flavor definition.
 * @author kgilmer
 *
 */
public class Flavor {
	private final String label;
	private final String description;
	private final String imageURL;
	private final String scriptURL;
	
	public Flavor(String line) throws IOException {
		String [] e = line.split("\\|");
		
		if (e.length != 4) {
			throw new IOException("Invalid flavor line: " + line);
		}
		
		label = e[0];
		description = e[1];
		imageURL = e[2];
		scriptURL = e[3];
	}
	
	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public String getImageURL() {
		return imageURL;
	}

	public String getScriptURL() {
		return scriptURL;
	}
	
}