package org.openembedded.bc.ui.wizards.install;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openembedded.bc.ui.wizards.FiniteStateWizardPage;


/**
 * Select which flavor of OE is to be installed.
 * 
 * @author kgilmer
 * 
 */
public class OptionsPage extends FiniteStateWizardPage {

	private Map vars;
	private Composite c1;
	private Composite top;
	protected static final String OPTION_MAP = "OPTION_MAP";
	private static final String DEFAULT_MESSAGE = "Enter these parameters to install.";
	private List controlList;
	private boolean controlsCreated = false;

	protected OptionsPage(Map model) {
		super("Options", model);
		setTitle("Installing...");
		setMessage(DEFAULT_MESSAGE);
	}

	@Override
	public void createControl(Composite parent) {
		top = new Composite(parent, SWT.None);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		c1 = new Composite(top, SWT.None);
		c1.setLayout(new GridLayout(3, false));
		c1.setLayoutData(new GridData(GridData.FILL_BOTH));

		setControl(top);
	}

	private void createControls(Composite comp, Map v, List cl) {
		ValidationListener listener = new ValidationListener();

		for (Iterator i = v.keySet().iterator(); i.hasNext();) {

			String label = (String) i.next();
			final InstallParameter ip = (InstallParameter) v.get(label);

			if (ip.getType() != InstallParameter.DT_CHECKBOX) {
				Label title = new Label(comp, SWT.None);
				title.setText(ip.getLabel() + ": ");
				if (ip.getHelpText() != null) {
					title.setToolTipText(ip.getHelpText());
				}
			} else {
				//If we are creating checkbox, layout differently.
				new Label(comp, SWT.None);
			}
			
			Control toolTipControl = null;
			switch (ip.getType()) {
			case InstallParameter.DT_TEXT:
				Text field = new Text(comp, SWT.BORDER);
				field.setData(ip);
				field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				field.addModifyListener(listener);
				field.setText(ip.getData());
				cl.add(field);
				toolTipControl = field;
				break;
			case InstallParameter.DT_DIRECTORY:
				Composite locComposite = new Composite(comp, SWT.NONE);
				GridData gd = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
				gd.horizontalIndent = 0;
				locComposite.setLayoutData(gd);
				GridLayout gl = new GridLayout(2, false);
				gl.marginWidth = 0;
				locComposite.setLayout(gl);

				final Text location = new Text(locComposite, SWT.BORDER);
				location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				location.setText(ip.getData());
				location.addModifyListener(listener);
				location.setData(ip);
				toolTipControl = location;
				cl.add(location);

				Button button = new Button(locComposite, SWT.PUSH);
				button.setText("...");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						DirectoryDialog fd = new DirectoryDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OPEN);

						fd.setText(ip.getLabel());

						String selected = fd.open();

						if (selected != null) {
							location.setText(selected);
							// updateModel();
						}
					}
				});
				break;
			case InstallParameter.DT_FILE:
				Composite fileComposite = new Composite(comp, SWT.NONE);
				gd = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
				gd.horizontalIndent = 0;
				fileComposite.setLayoutData(gd);
				gl = new GridLayout(2, false);
				gl.marginWidth = 0;
				fileComposite.setLayout(gl);

				final Text fileLocation = new Text(fileComposite, SWT.BORDER);
				fileLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				fileLocation.setText(ip.getData());
				fileLocation.addModifyListener(listener);
				fileLocation.setData(ip);
				toolTipControl = fileLocation;
				cl.add(fileLocation);

				button = new Button(fileComposite, SWT.PUSH);
				button.setText("...");
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						FileDialog fd = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.OPEN);

						fd.setText(ip.getLabel());

						String selected = fd.open();

						if (selected != null) {
							fileLocation.setText(selected);
							// updateModel();
						}
					}
				});
				break;
			case InstallParameter.DT_COMBO:
				Combo cfield = new Combo(comp, SWT.READ_ONLY);
				cfield.setData(ip);
				// cfield.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				cfield.addModifyListener(listener);
				cfield.setItems(ip.getData().split(","));
				cfield.setText(ip.getData().split(",")[0]);
				toolTipControl = cfield;
				cl.add(cfield);
				break;
			case InstallParameter.DT_CHECKBOX:
				Button bfield = new Button(comp, SWT.CHECK);
				bfield.setData(ip);
				bfield.setText(ip.getLabel());
				// bfield.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				bfield.addSelectionListener(listener);
				if (ip.getData().trim().toUpperCase().equals("TRUE")) {
					bfield.setSelection(true);
				}
				toolTipControl = bfield;
				cl.add(bfield);
				break;
			default:
				throw new RuntimeException("Unknown or unimplemented field: " + ip.getType());
			}

			// Setup tool tips and wizard help.
			if (toolTipControl != null) {
				if (ip.getHelpText() != null) {
					toolTipControl.setToolTipText(ip.getHelpText());
					toolTipControl.addFocusListener(new FocusListener() {

						public void focusLost(FocusEvent e) {
						}

						public void focusGained(FocusEvent e) {
							setMessage(ip.getHelpText());
						}
					});
				} else {
					toolTipControl.addFocusListener(new FocusListener() {

						public void focusLost(FocusEvent e) {
						}

						public void focusGained(FocusEvent e) {
							setMessage(DEFAULT_MESSAGE);
							;
						}
					});
				}
			}

			// Create a hyperlink to help content if exists, or invisible label if not.
			if (ip.getHelpURL() != null) {
				Link l = new Link(comp, SWT.None);
				l.setText("<a>Help...</a>");
				l.addSelectionListener(new SelectionListener() {

					public void widgetSelected(SelectionEvent e) {
						org.eclipse.swt.program.Program.launch(ip.getHelpURL());
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub

					}
				});
			} else {
				new Label(comp, SWT.None);
			}
		}
	}

	@Override
	public void pageCleanup() {

	}

	@Override
	public void pageDisplay() {
		if (!controlsCreated) {
			controlList = new ArrayList();

			vars = parseVars((String) model.get(FlavorPage.INSTALL_SCRIPT));

			createControls(c1, vars, controlList);
			c1.layout();

			controlsCreated = true;
			setTitle(((String) model.get(FlavorPage.OE_FLAVOR_TITLE)).trim() + " Options");
		}
		updateModel();
	}

	public static Map parseVars(String line) {
		Map l = new LinkedHashMap();

		int i = 0;

		while ((i = line.indexOf("{|", i)) > -1) {
			int i2 = line.indexOf("|}", i);

			String var = line.subSequence(i + 2, i2).toString().trim();

			if (var.length() > 0) {
				InstallParameter ip = new InstallParameter(var + " ");

				if (ip.isValid() && !l.containsKey(ip.getLabel())) {
					l.put(ip.getLabel(), ip);
				}
			}
			i++;
		}

		return l;
	}

	@Override
	protected void updateModel() {
		Map m = new Hashtable();

		for (Iterator i = controlList.iterator(); i.hasNext();) {
			Control t = (Control) i.next();
			String val = null;
			InstallParameter ip = (InstallParameter) t.getData();

			if (t instanceof Text) {
				val = ((Text) t).getText();
			} else if (t instanceof Combo) {
				val = ((Combo) t).getText();
			} else if (t instanceof Button) {
				val = Boolean.toString(((Button) t).getSelection());
			} else {
				throw new RuntimeException("Unknown control type: " + t.getClass().getName());
			}

			m.put(ip.getLabel(), val);
		}
		model.put(OPTION_MAP, m);
	}

	@Override
	protected boolean validatePage() {
		return true;
	}

}
