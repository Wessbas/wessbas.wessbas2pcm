/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/


package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

import m4jdsl.WorkloadModel;
import m4jdsl.impl.M4jdslPackageImpl;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.wessbassdsl.XmiEcoreHandler;

import de.uka.ipd.sdq.pcm.allocation.Allocation;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceEnvironment;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceRepository;
import de.uka.ipd.sdq.pcm.system.System;
import de.uka.ipd.sdq.pcm.usagemodel.UsageModel;
import de.uka.ipd.sdq.pcm.usagemodel.UsagemodelFactory;

/**
 * @author voegele
 * 
 */
public class CreatorTools {

	// singleton
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
	private Resource repositoryResource;
	private EObject rootRepository;

	private System thisSystem;
	private Resource systemResource;
	private EObject rootSystem;

	private Allocation thisAllocation;
	private Resource allocationResource;
	private EObject rootAllocation;

	private ResourceEnvironment thisResourceEnvironment;
	private Resource resourceEnvironmentResource;
	private EObject rootEnvironment;

	private UsageModel thisUsageModel;
	private Resource usageResource;

	private ResourceSet thisResourceSet;
	private WorkloadModel thisWorkloadModel;

	/**
	 * @return the thisWorkloadModel
	 */
	protected WorkloadModel getThisWorkloadModel() {
		if (thisWorkloadModel == null) {
			try {
				// initialize the model package;
				M4jdslPackageImpl.init();

				// Read Workload Model from XMI File
				// might throw an IOException;
				thisWorkloadModel = (WorkloadModel) XmiEcoreHandler
						.getInstance().xmiToEcore(Constants.XMI_FILE, "xmi");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return thisWorkloadModel;
	}

	/**
	 * @return the thisRepository
	 * @throws IOException
	 */
	protected final Repository getThisRepository() throws IOException {
		if (thisRepository == null) {
			repositoryResource = getResourceSet().getResource(
					URI.createFileURI(Configuration.getRepositoryFile()
							.getAbsolutePath()), true);
			repositoryResource.load(Collections.EMPTY_MAP);
			rootRepository = repositoryResource.getContents().get(0);
			thisRepository = (Repository) rootRepository;
		}
		return thisRepository;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveRepository() throws IOException {
		this.repositoryResource.save(null);
	}

	/**
	 * @return the thisSystem
	 * @throws IOException
	 */
	protected final System getThisSystem() throws IOException {
		if (thisSystem == null) {
			systemResource = getResourceSet().getResource(
					URI.createFileURI(Configuration.getSystemFile()
							.getAbsolutePath()), true);
			systemResource.load(Collections.EMPTY_MAP);
			rootSystem = systemResource.getContents().get(0);
			thisSystem = (System) rootSystem;
		}

		return thisSystem;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveSystem() throws IOException {
		this.systemResource.save(null);
	}

	/**
	 * @return the thisAllocation
	 * @throws IOException
	 */
	protected final Allocation getThisAllocation() throws IOException {
		if (thisAllocation == null) {
			// load Allocation
			allocationResource = getResourceSet().getResource(
					URI.createFileURI(Configuration.getAllocationFile()
							.getAbsolutePath()), true);
			allocationResource.load(Collections.EMPTY_MAP);
			rootAllocation = allocationResource.getContents().get(0);
			thisAllocation = (Allocation) rootAllocation;
		}
		return thisAllocation;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveAllocation() throws IOException {
		this.allocationResource.save(null);
	}

	/**
	 * @return the thisResourceEnvironment
	 * @throws IOException
	 */
	protected final ResourceEnvironment getThisResourceEnvironment()
			throws IOException {
		if (thisResourceEnvironment == null) {
			// load ResourceEnvironment
			resourceEnvironmentResource = getResourceSet().getResource(
					URI.createFileURI(Configuration
							.getResourceenvironmentFile().getAbsolutePath()),
					true);
			resourceEnvironmentResource.load(Collections.EMPTY_MAP);
			rootEnvironment = resourceEnvironmentResource.getContents().get(0);
			thisResourceEnvironment = (ResourceEnvironment) rootEnvironment;
		}
		return thisResourceEnvironment;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveResourceEnvironment() throws IOException {
		this.usageResource.save(null);
	}

	/**
	 * @return the thisUsageModel
	 */
	protected final UsageModel getThisUsageModel() {
		if (thisUsageModel == null) {
			// create usage model
			usageResource = getResourceSet().createResource(
					URI.createFileURI(Configuration.getUsageModelFile()
							.getAbsolutePath()));

			// create UsageModel
			thisUsageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
			usageResource.getContents().add(thisUsageModel);

		}
		return thisUsageModel;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveUsageModel() throws IOException {
		this.usageResource.save(null);
	}

	/**
	 * GetResourceSet
	 * 
	 * @return
	 */
	protected ResourceSet getResourceSet() {
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
	protected final ResourceRepository getResourceRepository(
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
	 * @param componentName
	 * @return String
	 */
	protected final String getAssemblyName(String componentName) {
		return "Assembly_" + componentName + " <" + componentName + ">";
	}

	/**
	 * @param file
	 */
	public static final void deleteFiles(File file) {
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
	 * @throws IOException
	 *             IOException
	 */
	public final static void copyPCMFilesSourceTarget() throws IOException {

		// allocation
		Path copySourcePathAllocation = Paths
				.get(Constants.MODEL_DIRECTORY_SOURCE + Constants.ALLOCATION_MODEL_NAME);
		Path copyTargetPathAllocation = Paths
				.get(Constants.MODEL_DIRECTORY + Constants.ALLOCATION_MODEL_NAME);
		Files.copy(copySourcePathAllocation, copyTargetPathAllocation);

		// resourceEnvironment
		Path copySourcePathEnvironment = Paths
				.get(Constants.MODEL_DIRECTORY_SOURCE + Constants.INFRASTRUCTURE_MODEL_NAME);
		Path copyTargetPathEnvironment = Paths
				.get(Constants.MODEL_DIRECTORY + Constants.INFRASTRUCTURE_MODEL_NAME);
		Files.copy(copySourcePathEnvironment, copyTargetPathEnvironment);

		// system
		Path copySourcePathSystem = Paths
				.get(Constants.MODEL_DIRECTORY_SOURCE + Constants.SYSTEM_MODEL_NAME);
		Path copyTargetPathSystem = Paths
				.get(Constants.MODEL_DIRECTORY + Constants.SYSTEM_MODEL_NAME);
		Files.copy(copySourcePathSystem, copyTargetPathSystem);

		// repository
		Path copySourcePathRepository = Paths
				.get(Constants.MODEL_DIRECTORY_SOURCE + Constants.REPOSITORY_MODEL_NAME);
		Path copyTargetPathRepository = Paths
				.get(Constants.MODEL_DIRECTORY + Constants.REPOSITORY_MODEL_NAME);
		Files.copy(copySourcePathRepository, copyTargetPathRepository);

	}

}
