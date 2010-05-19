/**
 * 
 */
package org.openembedded.bc.ui.wizards.install;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.openembedded.bc.bitbake.ICommandResponseHandler;
import org.openembedded.bc.bitbake.ShellSession;
import org.openembedded.bc.ui.Activator;


class InstallJob extends Job {

	private final Map mod;
	private UICommandResponseHandler cmdOut;
	private boolean errorOccurred = false;
	private String errorMessage = "";

	public InstallJob(Map model, ProgressPage progressPage) {
		super("Install OpenEmbedded");
		mod = model;
		cmdOut = new UICommandResponseHandler(progressPage);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		BufferedReader reader = new BufferedReader(new StringReader(
				(String) mod.get(FlavorPage.INSTALL_SCRIPT)));
		String line = null;
		Map vars = loadVariables();

		try {
			ShellSession shell = new ShellSession(ShellSession.SHELL_TYPE_BASH,
					null, null, null);
			while ((line = reader.readLine()) != null && !errorOccurred) {
				line = line.trim();
				if (line.length() > 0 && !line.startsWith("#")) {
					line = substitute(line, vars);
					cmdOut.printCmd(line);
					System.out.println("Running: " + line);
					shell.execute(line, cmdOut);
				} else if (line.startsWith("#")) {
					cmdOut.printDialog(line.substring(1).trim());
				}
			}

			if (errorOccurred) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Failed to install OpenEmbedded: " + errorMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to install OpenEmbedded", e);
		}

		return Status.OK_STATUS;
	}

	private Map loadVariables() {
		return (Map) mod.get(OptionsPage.OPTION_MAP);
	}

	/**
	 * Return a string with variable substitutions in place.
	 * 
	 * @param expression
	 * @return Input string with any substitutions from this file.
	 * @throws ParameterValueConversionException
	 */
	public static String substitute(String expression, Map mo)
			throws ParameterValueConversionException {

		List vars;
		int literals = 0;

		while ((vars = parseVars(expression)).size() > literals) {
			for (Iterator i = vars.iterator(); i.hasNext();) {
				String varName = (String) i.next();
				String varToken = "${" + varName + "}";

				if (mo.containsKey(varName)) {
					expression = expression.replace(varToken, (String) mo
							.get(varName));
				} else if (System.getProperty(varName) != null) {
					expression = expression.replace(varToken, System
							.getProperty(varName));
				} else if (varName.toUpperCase().equals("HOME")) {
					expression = expression.replace(varToken, System
							.getProperty("user.home"));
				} else {
					//throw new ParameterValueConversionException(
					//		"Unable to match parameter: " + expression);
					// Leave the value, treat as a literal.
					literals++;
				}
			}
		}

		return expression;
	}

	/**
	 * 
	 * @param line
	 * @return A list of variables in $[variable name] format.
	 */
	public static List parseVars(String line) {
		List l = new ArrayList();

		int i = 0;

		while ((i = line.indexOf("${", i)) > -1) {
			int i2 = line.indexOf("}", i);

			String var = line.subSequence(i + 2, i2).toString().trim();

			if (var.length() > 0 && !l.contains(var)) {
				l.add(var);
			}
			i++;
		}

		return l;
	}

	private class UICommandResponseHandler implements ICommandResponseHandler {

		private final ProgressPage progressPage;

		public UICommandResponseHandler(ProgressPage progressPage) {
			this.progressPage = progressPage;
		}

		public void printDialog(String msg) {
			progressPage.printDialog(msg);
		}

		public void response(String line, boolean isError) {
			if (isError) {
				progressPage.printLine(line, ProgressPage.PRINT_ERR);
				//errorOccurred = true;
				//errorMessage = line;
			} else {
				progressPage.printLine(line, ProgressPage.PRINT_OUT);
				if (line.endsWith("!!OTEWIZARDSTOP")) {
					errorOccurred = true;
					errorMessage = line;
				}
			}
		}

		public void printCmd(String cmd) {
			progressPage.printLine(cmd, ProgressPage.PRINT_CMD);
		}

	}

}