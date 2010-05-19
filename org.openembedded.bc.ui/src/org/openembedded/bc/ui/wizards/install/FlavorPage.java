package org.openembedded.bc.ui.wizards.install;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.openembedded.bc.ui.Activator;
import org.openembedded.bc.ui.wizards.FiniteStateWizardPage;


/**
 * Select which flavor of OE is to be installed.
 * 
 * @author kgilmer
 * 
 */
public class FlavorPage extends FiniteStateWizardPage {
	protected static final String OE_FLAVOR = "FLAVOR";
	protected static final String OE_FLAVOR_TITLE = "OE_FLAVOR_TITLE";
	protected static final String INSTALL_SCRIPT = "INSTALL_SCRIPT";
	
	private boolean controlsCreated;
	private TableViewer flavorList;

	protected FlavorPage(Map model) {
		super("Flavor", model);
		setTitle("Select OpenEmbedded Flavor");
		setMessage("Select the flavor of OpenEmbedded you wish to install...");
	}

	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.None);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		flavorList = new TableViewer(top, SWT.BORDER);
		flavorList.setContentProvider(new FlavorContentProvider());
		flavorList.setLabelProvider(new FlavorLabelProvider());
		flavorList.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		flavorList.addSelectionChangedListener(new ValidationListener());
		
		TableColumn tc = new TableColumn(flavorList.getTable(), SWT.LEFT);
		tc.setText("Image");
		tc.setWidth(128);
		
		tc = new TableColumn(flavorList.getTable(), SWT.LEFT);
		tc.setText("Title");
		tc.setWidth(300);
		/*
		tc = new TableColumn(flavorList.getTable(), SWT.LEFT);
		tc.setText("Description");
		tc.setWidth(300);*/
		
		
		setControl(top);
	}

	
	public void pageCleanup() {

	}

	
	public void pageDisplay() {
		getContainer().getShell().setSize(getContainer().getShell().getSize().x, 600);
		if (!controlsCreated) {
			try {
				flavorList.setInput(InstallScriptHelper.getFlavors("scripts/install_flavors.txt"));
				controlsCreated = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	protected void updateModel() {
		Flavor f = (Flavor) ((IStructuredSelection) flavorList.getSelection()).getFirstElement();
		model.put(OE_FLAVOR, f);
		model.put(OE_FLAVOR_TITLE, f.getLabel());
		try {
			model.put(INSTALL_SCRIPT, InstallScriptHelper.loadFile(f.getScriptURL()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	protected boolean validatePage() {
		return flavorList.getSelection() != null;
	}

	private class FlavorContentProvider implements IStructuredContentProvider {

		
		public Object[] getElements(Object arg0) {
			return ((List) arg0).toArray();
		}

		
		public void dispose() {
		}

		
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {		
		}

	}

	private class FlavorLabelProvider implements ITableLabelProvider {
		private Map imageMap = new Hashtable();
		
		
		public Image getColumnImage(Object arg0, int arg1) {
			Flavor f = (Flavor) arg0;
			Image i = null;
			
			if (arg1 == 0) {
				i = (Image) imageMap.get(f);
				if (i == null) {
					i = Activator.getImageDescriptor(f.getImageURL()).createImage();
					imageMap.put(f, i);
				}
			}
			
			return i;
		}

		
		public String getColumnText(Object arg0, int arg1) {
			Flavor f = (Flavor) arg0;
			if (arg1 == 1) {
				return f.getLabel();
			} else if (arg1 == 2) {
				return f.getDescription();
			}
			
			return null;
		}

		
		public void addListener(ILabelProviderListener arg0) {

		}

		
		public void dispose() {
			// TODO Auto-generated method stub

		}

		
		public boolean isLabelProperty(Object arg0, String arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		
		public void removeListener(ILabelProviderListener arg0) {
			// TODO Auto-generated method stub

		}

	}

}
