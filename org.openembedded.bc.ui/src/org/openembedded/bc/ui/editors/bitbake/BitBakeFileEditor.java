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
package org.openembedded.bc.ui.editors.bitbake;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.openembedded.bc.ui.Activator;
import org.openembedded.bc.ui.builder.BitbakeCommanderNature;


/**
 * Editor for BB Recipe
 * 
 * @author kgilmer
 * 
 */
public class BitBakeFileEditor extends AbstractDecoratedTextEditor {

	public static final String EDITOR_ID = "org.openembedded.bc.ui.editors.BitBakeFileEditor";
	static final String CONTENT_ASSIST = "ContentAssist";
	private BitBakeSourceViewerConfiguration viewerConfiguration;
	private String targetFilePath;

	public BitBakeFileEditor() {
		super();
		viewerConfiguration = new BitBakeSourceViewerConfiguration(getSharedColors(), getPreferenceStore());
		setSourceViewerConfiguration(viewerConfiguration);
		setDocumentProvider(new BitBakeDocumentProvider());
	}

	@Override
	protected void createActions() {
		super.createActions();

		ResourceBundle bundle = RecipeEditorMessages.getBundle();
		ContentAssistAction action = new ContentAssistAction(bundle, "contentAssist.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction(CONTENT_ASSIST, action);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		if (input instanceof FileEditorInput) {
			IProject p = ((FileEditorInput) input).getFile().getProject();
			try {
				if (p != null && p.isOpen() && p.hasNature(BitbakeCommanderNature.NATURE_ID)) {
					targetFilePath = ((FileEditorInput) input).getFile().getLocationURI().getPath();
					viewerConfiguration.setTargetFilePath(targetFilePath);
					viewerConfiguration.setBBSession(Activator.getBBSession(p.getLocationURI().getPath()));
				}
			} catch (Exception e) {
				throw new PartInitException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "On " + input.getClass().getName() + " input, nable to load editor.", e));
			}
		} else if (input instanceof FileStoreEditorInput) {
			targetFilePath = ((FileStoreEditorInput)input).getURI().getPath();
			System.out.println("Target path:" + targetFilePath);
		} else {
			if (input == null) {
				System.out.println("The input is null!");
			}
			System.out.println("The input is not a FileEditorInput: " + input.getClass().getName());
		}
		
		super.init(site, input);
	}
}