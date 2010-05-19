package org.openembedded.bc.ui.wizards.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openembedded.bc.ui.Activator;


/**
 * Helper for loading install scripts.
 * @author kgilmer
 *
 */
public class InstallScriptHelper {
	
	public static List getFlavors(String filePath) throws IOException {
		InputStream is = Activator.getDefault().getBundle().getResource(filePath).openStream();
		List list = new ArrayList();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;		
		
		while ((line = br.readLine()) != null) {
			line = line.trim();
			
			if (line.length() > 0 && !line.startsWith("#")) {
				list.add(new Flavor(line));
			}
		}
		
		return list;
	}

	public static String loadFile(String fileName) throws IOException {		
		InputStream is = Activator.getDefault().getBundle().getResource(fileName).openStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		StringBuffer sb = new StringBuffer();
		
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		
		return sb.toString();
	}
}
