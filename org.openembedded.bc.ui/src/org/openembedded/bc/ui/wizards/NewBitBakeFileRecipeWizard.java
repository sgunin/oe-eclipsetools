/*****************************************************************************
 * Copyright (c) 2009 Ken Gilmer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Gilmer - initial API and implementation
 *******************************************************************************/
package org.openembedded.bc.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.openembedded.bc.bitbake.BBLanguageHelper;


public class NewBitBakeFileRecipeWizard extends Wizard implements INewWizard {
	private NewBitBakeFileRecipeWizardPage page;
	private ISelection selection;

	public NewBitBakeFileRecipeWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new NewBitBakeFileRecipeWizardPage(selection);
		addPage(page);
	}

	private void doFinish(String containerName, String fileName, String description, String license, String homepage, String author, String srcuri, IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		
		// If the extension wasn't specified, assume .bb
		if (!fileName.endsWith(BBLanguageHelper.BITBAKE_RECIPE_FILE_EXTENSION) || !fileName.endsWith(".inc") || !fileName.endsWith(".conf")) {
			fileName = fileName + ".bb";
		}
		
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(fileName, description, license, homepage, author, srcuri);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	/**
	 * We will initialize file contents with a sample text.
	 * @param srcuri 
	 * @param author 
	 * @param homepage 
	 * @param license 
	 * @param description 
	 * @param fileName 
	 * @param newPage 
	 */

	private InputStream openContentStream(String fileName, String description, String license, String homepage, String author, String srcuri) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("DESCRIPTION = \"" + description + "\"\n");
		
		if (author.length() > 0) {
			sb.append("AUTHOR = \"" + author + "\"\n");
		}

		if (homepage.length() > 0) {
			sb.append("HOMEPAGE = \"" + homepage + "\"\n");
		}
		
		if (license.length() > 0) {
			sb.append("LICENSE = \"" + license + "\"\n");
		}

		if (srcuri.length() > 0) {
			sb.append("SRC_URI = \"" + srcuri + "\"\n");
		}
		
		sb.append("\n");

		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String description = page.getDescriptionText();
		final String license = page.getLicenseText();
		final String homepage = page.getHomepageText();
		final String author = page.getAuthorText();
		final String srcuri = page.getSrcuriText();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, description, license, homepage, author, srcuri, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "org.openembedded.bc.ui", IStatus.OK, message, null);
		throw new CoreException(status);
	}
}