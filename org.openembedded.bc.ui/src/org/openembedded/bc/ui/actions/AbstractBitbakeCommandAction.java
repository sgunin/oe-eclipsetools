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
package org.openembedded.bc.ui.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.openembedded.bc.bitbake.BBLanguageHelper;
import org.openembedded.bc.bitbake.BBSession;
import org.openembedded.bc.bitbake.ICommandResponseHandler;
import org.openembedded.bc.ui.Activator;
import org.openembedded.bc.ui.builder.BitbakeCommanderNature;


public abstract class AbstractBitbakeCommandAction implements IWorkbenchWindowActionDelegate {

	private class CommandJob extends Job {

		public CommandJob() {
			super(getJobTitle());
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			String cmds[] = getCommands();
			return execCommands(cmds, monitor);
		}

	}
	protected IAction action;
	protected IFile recipe;
	protected BBSession bbs;

	private Color commandColor, responseColor, errorColor;
	private boolean errorOccurred = false;

	public AbstractBitbakeCommandAction() {
		commandColor = JFaceResources.getColorRegistry().get(JFacePreferences.ACTIVE_HYPERLINK_COLOR);
		responseColor = JFaceResources.getColorRegistry().get(JFacePreferences.HYPERLINK_COLOR);
		errorColor = JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
	}

	private void checkEnabled(IFile file) {
		try {
			if (file.getFileExtension() == null || !file.getFileExtension().equals(BBLanguageHelper.BITBAKE_RECIPE_FILE_EXTENSION)) {
				action.setEnabled(false);
				return;
			}

			IProject project = file.getProject();
			if (!(project.hasNature(BitbakeCommanderNature.NATURE_ID))) {
				action.setEnabled(false);
				return;
			}

			bbs = Activator.getBBSession(project.getLocationURI().getPath());

			if (bbs != null) {
				recipe = file;
				action.setEnabled(true);
			}

		} catch (CoreException e) {
			action.setEnabled(false);
			e.printStackTrace();
		} catch (Exception e) {
			action.setEnabled(false);
			e.printStackTrace();
		}
	}

	public void dispose() {
	}

	/**
	 * Execute array of commands with bitbake and put output in console.
	 * 
	 * @param cmds
	 * @param monitor
	 * @return
	 */
	protected IStatus execCommands(String[] cmds, final IProgressMonitor monitor) {
		MessageConsole mc = bbs.getConsole();
		final MessageConsoleStream cmd = mc.newMessageStream();		
		final MessageConsoleStream out = mc.newMessageStream();
		final MessageConsoleStream err = mc.newMessageStream();
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			
			@Override
			public void run() {
				cmd.setColor(commandColor);
				err.setColor(errorColor);
			}
		});
		
		
		try {
			for (int i = 0; i < cmds.length; ++i) {
				cmd.println(cmds[i]);
				monitor.subTask(cmds[i]);
				bbs.getShell().execute(cmds[i], new ICommandResponseHandler() {

					public void response(String line, boolean isError) {
						if (monitor.isCanceled()) {
							cmd.println("Interrupting process by user request.");
							bbs.getShell().interrupt();
						}
						
						if (isError) {
							err.println(line);
							errorOccurred();
						} else if (line.startsWith("ERROR:")) {
							err.println(line);
						} else {
							out.println(line);
						}
					}
				});
			}
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
		} finally {
			try {
				if (errorOccurred) {
					cmd.println("At least one error occured while executing this command.  Check output for more details.");
				}
				cmd.close();
				out.close();
				err.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return Status.OK_STATUS;
	}

	protected void errorOccurred() {
		errorOccurred = true;
	}

	/**
	 * Return the command to be executed.
	 * 
	 * @return
	 */
	public abstract String[] getCommands();

	public Job getJob() {
		return new CommandJob();
	}

	/**
	 * Return the title of the job.
	 * 
	 * @return
	 */
	public abstract String getJobTitle();

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		Job job = getJob();
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		if (selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();

			if (sel instanceof IFile) {
				checkEnabled((IFile) sel);
				return;
			}
		}

		action.setEnabled(false);
	}
	
	/**
	 * @param path Path to recipe file
	 * @return The recipe name that bitbake will understand, based on a full path to a recipe file.
	 */
	protected static String getRecipeFromIFile(IFile path) {
		String bbRecipeExtension = ".bb";
		if (!path.getName().endsWith(bbRecipeExtension))
			throw new RuntimeException("File is not a bitbake recipe: " + path.getName());
		
		//Extract the filename without the extension.
		String name = path.getName().substring(0, path.getName().length() - bbRecipeExtension.length());
		
		String [] nvp = name.split("_");
		
		if (nvp.length == 0)
			throw new RuntimeException("Unable to parse recipe name from filename: " + name);
		
		//No version information embedded in the filename
		if (nvp.length == 1)
			return nvp[0];
		
		//Use bitbake's convention for specifying the version with a "-"
		if (nvp.length == 2)
			return nvp[0] + "-" + nvp[1];
		
		//Unknown format, just return the name
		return nvp[0];
	}

}