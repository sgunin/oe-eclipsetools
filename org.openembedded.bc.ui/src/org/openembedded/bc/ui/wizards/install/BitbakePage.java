package org.openembedded.bc.ui.wizards.install;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openembedded.bc.ui.Activator;
import org.openembedded.bc.ui.model.ProjectInfo;
import org.openembedded.bc.ui.wizards.FiniteStateWizard;
import org.openembedded.bc.ui.wizards.FiniteStateWizardPage;
import org.openembedded.bc.ui.wizards.importProject.ImportOEProjectWizard;
import org.openembedded.bc.ui.wizards.newproject.BBConfigurationInitializeOperation;


/**
 * Bitbake console view.
 * 
 * @author kgilmer
 * 
 */
public class BitbakePage extends FiniteStateWizardPage {

	private boolean valid = false;

	public BitbakePage(Map model) {
		super("Bitbake Console Page", model);
		setTitle("Extracting BitBake Environment");
		setMessage("Output of 'bitbake -e' command");
	}

	private Text txtConsole;

	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		top.setLayout(new GridLayout());

		txtConsole = new Text(top, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		txtConsole.setLayoutData(new GridData(GridData.FILL_BOTH));
		txtConsole.setEditable(false);
		txtConsole.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		setControl(top);
	}

	@Override
	public void pageCleanup() {
		Activator.resetBBSession((String) model.get(ImportOEProjectWizard.KEY_LOCATION));
	}

	@Override
	public void pageDisplay() {
		Map props = (Map) model.get(OptionsPage.OPTION_MAP);

		try {
			String initPath = InstallJob.substitute((String) props.get(BBCProjectPage.INIT_SCRIPT_KEY), model);
			String location = InstallJob.substitute((String) props.get(BBCProjectPage.INSTALL_DIRECTORY_KEY), model);
			String name = null;
			if (props.containsKey(BBCProjectPage.PROJECT_NAME_KEY)) {
				name = InstallJob.substitute((String) props.get(BBCProjectPage.PROJECT_NAME_KEY), model);
			} else {
				name = parseName((String) props.get("Install Directory"));
			}

			ProjectInfo pinfo = new ProjectInfo();
			pinfo.setInitScriptPath(initPath);
			pinfo.setLocation(location);
			pinfo.setName(name);

			ConsoleWriter cw = new ConsoleWriter();
			this.getContainer().run(false, false, new BBConfigurationInitializeOperation(pinfo, cw));
			txtConsole.setText(cw.getContents());
			valid = true;
			model.put(InstallWizard.KEY_PINFO, pinfo);
		} catch (InvocationTargetException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getTargetException().getMessage(), e));
			setErrorMessage("Failed to create project.");
			txtConsole.setText(e.getTargetException().getMessage());
			valid = false;
			setPageComplete(valid);
			return;
		} catch (InterruptedException e) {
		} catch (ParameterValueConversionException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
			setErrorMessage("Failed to create project: " + e.getMessage());
			valid = false;
			setPageComplete(valid);
			return;
		}

		setPageComplete(valid);
		((FiniteStateWizard) this.getWizard()).setCanFinish(true);
	}

	private String parseName(String name) {
		String[] e = name.split(File.separator);
		return e[e.length - 1];
	}

	@Override
	protected void updateModel() {

	}

	@Override
	protected boolean validatePage() {
		return valid;
	}

	private class ConsoleWriter extends Writer {

		private StringBuffer sb;

		public ConsoleWriter() {
			sb = new StringBuffer();
		}

		@Override
		public void close() throws IOException {
		}

		public String getContents() {
			return sb.toString();
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			txtConsole.getText().concat(new String(cbuf));
		}

		@Override
		public void write(String str) throws IOException {
			sb.append(str);
		}

	}

}
