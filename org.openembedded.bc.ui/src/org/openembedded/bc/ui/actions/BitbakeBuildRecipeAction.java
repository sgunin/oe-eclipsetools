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

import org.eclipse.core.resources.IFile;

public  class BitbakeBuildRecipeAction extends AbstractBitbakeCommandAction {

	@Override
	public String [] getCommands() {
		return new String[] {"bitbake " + getRecipeFromIFile(recipe)};
	}

	@Override
	public String getJobTitle() {
		return "Building " + recipe.getName();
	}
	
	/**
	 * @param path Path to recipe file
	 * @return The recipe name that bitbake will understand, based on a full path to a recipe file.
	 */
	private static String getRecipeFromIFile(IFile path) {
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