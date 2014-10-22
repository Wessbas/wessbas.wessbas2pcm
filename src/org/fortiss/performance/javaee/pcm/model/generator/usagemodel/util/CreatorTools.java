package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import de.uka.ipd.sdq.pcm.allocation.Allocation;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceEnvironment;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceRepository;
import de.uka.ipd.sdq.pcm.system.System;
import de.uka.ipd.sdq.pcm.usagemodel.UsageModel;

/**
 * @author voegele
 * 
 */
public class CreatorTools {

	private static CreatorTools instance = null;

	private CreatorTools() {
	}

	public static CreatorTools getInstance() {
		if (instance == null) {
			instance = new CreatorTools();
		}
		return instance;
	}

	/**
	 * Logger used in this project.
	 */
	public final Logger log = Logger.getAnonymousLogger();

	private Repository thisRepository;
	private System thisSystem;
	private Allocation thisAllocation;
	private ResourceEnvironment thisResourceEnvironment;
	private UsageModel thisUsageModel;
	private ResourceSet thisResourceSet;

	/**
	 * @return the thisRepository
	 */
	public final Repository getThisRepository() {
		return thisRepository;
	}

	/**
	 * @param thisRepository
	 *            the thisRepository to set
	 */
	public final void setThisRepository(Repository thisRepository) {
		this.thisRepository = thisRepository;
	}

	/**
	 * @return the thisSystem
	 */
	public final System getThisSystem() {
		return thisSystem;
	}

	/**
	 * @param thisSystem
	 *            the thisSystem to set
	 */
	public final void setThisSystem(System thisSystem) {
		this.thisSystem = thisSystem;
	}

	/**
	 * @return the thisAllocation
	 */
	public final Allocation getThisAllocation() {
		return thisAllocation;
	}

	/**
	 * @param thisAllocation
	 *            the thisAllocation to set
	 */
	public final void setThisAllocation(Allocation thisAllocation) {
		this.thisAllocation = thisAllocation;
	}

	/**
	 * @return the thisResourceEnvironment
	 */
	public final ResourceEnvironment getThisResourceEnvironment() {
		return thisResourceEnvironment;
	}

	/**
	 * @param thisResourceEnvironment
	 *            the thisResourceEnvironment to set
	 */
	public final void setThisResourceEnvironment(
			ResourceEnvironment thisResourceEnvironment) {
		this.thisResourceEnvironment = thisResourceEnvironment;
	}

	/**
	 * @return the thisUsageModel
	 */
	public final UsageModel getThisUsageModel() {
		return thisUsageModel;
	}

	/**
	 * @param thisUsageModel
	 *            the thisUsageModel to set
	 */
	public final void setThisUsageModel(UsageModel thisUsageModel) {
		this.thisUsageModel = thisUsageModel;
	}

	/**
	 * GetResourceSet
	 * 
	 * @return
	 */
	public ResourceSet getResourceSet() {
		if (thisResourceSet == null) {
			thisResourceSet = new ResourceSetImpl();
		}
		return thisResourceSet;
	}

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
	 * @param componentName
	 * @return String
	 */
	public final String getAssemblyName(String componentName) {
		return "Assembly_" + componentName + " <" + componentName + ">";
	}

}
