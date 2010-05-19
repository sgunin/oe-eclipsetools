package org.openembedded.bc.ui.wizards.variable;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.openembedded.bc.ui.wizards.FiniteStateWizard;


/**
 * This wizard is used to view, filter, and search for BitBake variables and variable contents.
 * @author kgilmer
 *
 */
public class VariableWizard extends FiniteStateWizard {

	private Map model;

	public VariableWizard(Map model) {
		this.model = model;
		setWindowTitle("BitBake Commander");
	}

	public VariableWizard(IStructuredSelection selection) {
		model = new Hashtable();
	}
	
	@Override
	public void addPages() {
		addPage(new VariablePage(model));
	}
	
	@Override
	public Map getModel() {
		return model;
	}

	@Override
	public boolean performFinish() {
		return true;
	}

}
