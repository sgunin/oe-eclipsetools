/**
 * 
 */
package org.openembedded.bc.ui.wizards.install;

class InstallParameter {
	public static final int DT_TEXT = 1;
	public static final int DT_COMBO = 2;
	public static final int DT_LIST = 3;
	public static final int DT_NUMBER = 4;
	public static final int DT_DIRECTORY = 5;
	public static final int DT_FILE = 6;
	public static final int DT_CHECKBOX = 7;
	
	private boolean valid = false;
	private int type;
	private String label;
	private boolean required;
	private String data;
	private String helpURL;
	private String helpText;

	public int getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean isRequired() {
		return required;
	}

	public String getData() {
		return data;
	}

	public String getHelpURL() {
		return helpURL;
	}
	
	public String getHelpText() {
		return helpText;
	}

	public InstallParameter(String var) {
		// {|Datatype|Label|UnRequired|Data|Help|}
		// {|T|Distribution|R|angstrom-2008.1|http://wiki.openembedded.net/index.php/Getting_started#Create_local_configuration|}

		String[] elems = var.split("\\|");

		if (elems.length == 5 || elems.length == 6) {
			if (elems[0].equals("T")) {
				type = DT_TEXT;
			} else if (elems[0].equals("D")) {
				type = DT_DIRECTORY;
			} else if (elems[0].equals("F")) {
				type = DT_FILE;
			} else if (elems[0].equals("C")) {
				type = DT_COMBO;
			} else if (elems[0].equals("B")) {
				type = DT_CHECKBOX;
			} else {
				throw new RuntimeException("Invalid field format: " + var);
			}

			label = elems[1];

			if (elems[2].equals("R")) {
				required = true;
			} else if (elems[2].equals("U")) {
				required = false;
			} else {
				throw new RuntimeException("Invalid field format: " + var);
			}

			data = elems[3].trim();
			
			if (elems[4].trim().length() > 0) {
				helpURL = elems[4].trim();
			}
			
			if (elems.length == 6) {
				helpText = elems[5];
			}

			valid = true;
		} else {
			throw new RuntimeException("Invalid field format: " + var);
		}
	}

	public boolean isValid() {

		return valid;
	}

}