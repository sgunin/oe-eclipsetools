package org.openembedded.bc.ui.wizards.install;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.openembedded.bc.ui.Activator;
import org.openembedded.bc.ui.model.ProjectInfo;
import org.openembedded.bc.ui.wizards.FiniteStateWizard;
import org.openembedded.bc.ui.wizards.newproject.CreateBBCProjectOperation;


/**
 * A wizard for installing a fresh copy of an OE system.
 * 
 * @author kgilmer
 * 
 */
public class InstallWizard extends FiniteStateWizard implements IWorkbenchWizard {

	static final String KEY_PINFO = "KEY_PINFO";
	private Map model;

	public InstallWizard() {
		this.model = new Hashtable();
		setWindowTitle("BitBake Commander");
		setNeedsProgressMonitor(false);
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/OE_logo_96.png"));
	}

	public InstallWizard(IStructuredSelection selection) {
		model = new Hashtable();
	}

	/*@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof WelcomePage) {
			if (model.containsKey(WelcomePage.ACTION_USE)) {
				return bbcProjectPage;
			}
		} else if (page instanceof ProgressPage) {
			return bitbakePage;
		}
		
		if (super.getNextPage(page) != null) {
			System.out.println("next page: " + super.getNextPage(page).getClass().getName());
		} else {
			System.out.println("end page");
		}

		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		System.out.println("can finish: " + super.canFinish());
		return super.canFinish();
	}
*/
	@Override
	public void addPages() {
	//	flavorPage = new FlavorPage(model);
	//	bitbakePage = new BitbakePage(model);
	//	bbcProjectPage = new BBCProjectPage(model);
		//addPage(new WelcomePage(model));
		addPage(new FlavorPage(model));
		addPage(new OptionsPage(model));
		addPage(new ProgressPage(model));
		//addPage(bbcProjectPage);
		addPage(new BitbakePage(model));
	}

	@Override
	public Map getModel() {
		return model;
	}

	@Override
	public boolean performFinish() {
		ProjectInfo pinfo = (ProjectInfo) model.get(KEY_PINFO);
		Activator.putProjInfo(pinfo.getRootPath(), pinfo);
		try {
			getContainer().run(false, false, new CreateBBCProjectOperation(pinfo));
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
			this.getContainer().getCurrentPage().setTitle("Failed to create project: " + e.getTargetException().getMessage());
			return false;
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
			this.getContainer().getCurrentPage().setTitle("Failed to create project: " + e.getMessage());
			return false;
		}

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
}
