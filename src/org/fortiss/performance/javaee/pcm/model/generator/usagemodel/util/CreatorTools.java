package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import de.uka.ipd.sdq.pcm.resourcetype.ResourceRepository;
import de.uka.ipd.sdq.pcm.system.System;

/**
 * @author voegele
 * 
 */
public abstract class CreatorTools {

	/**
	 * Logger used in this project.
	 */
	public static final Logger log = Logger.getAnonymousLogger();

	/**
	 * @param resourceSet
	 *            resourceSet
	 * @return ResourceRepository
	 * @throws IOException
	 *             IOException
	 */
	public final ResourceRepository getResourceRepository(
			final ResourceSet resourceSet) throws IOException {
		Resource resourceTypeResource = resourceSet.getResource(
				URI.createURI("pathmap://PCM_MODELS/Palladio.resourcetype "),
				true);
		resourceTypeResource.load(Collections.EMPTY_MAP);
		EObject rootTypeResource = resourceTypeResource.getContents().get(0);
		ResourceRepository resourceRepository = (ResourceRepository) rootTypeResource;
		return resourceRepository;
	}

	/**
	 * @param file
	 */
	public final void deleteFiles(File file) {
		File[] finlist = file.listFiles();
		for (int n = 0; n < finlist.length; n++) {
			if (finlist[n].isFile()
					&& !finlist[n].getName().substring(0, 1).equals(".")) {
				java.lang.System.out.println("Delete File: "
						+ finlist[n].getName());
				java.lang.System.gc();
				finlist[n].delete();
			}
		}
	}

	/**
	 * Return a instance of the system model.
	 * 
	 * @param systemDirectory
	 *            systemDirectory
	 * @param resourceSet
	 *            resourceSet
	 * @return System
	 * @throws IOException
	 *             IOException
	 */
	public final System getSystem(final File systemDirectory,
			final ResourceSet resourceSet) throws IOException {
		final Resource systemResource = resourceSet.getResource(
				URI.createFileURI(systemDirectory.getAbsolutePath()), true);
		systemResource.load(Collections.EMPTY_MAP);
		final EObject rootSystem = systemResource.getContents().get(0);
		final System system = (System) rootSystem;
		return system;
	}

	// /**
	// * @param componentName
	// * @return String
	// */
	// public final String getAssemblyName(String componentName) {
	// return "Assembly_" + componentName + " <" + componentName + ">";
	// }

	/**
	 * @param componentName
	 * @return String
	 */
	public final String getAssemblyName(String componentName) {
		return componentName;
	}
}
