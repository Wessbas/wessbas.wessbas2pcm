package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;

import m4jdsl.BehaviorModel;
import m4jdsl.WorkloadModel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.allocation.Allocation;
import de.uka.ipd.sdq.pcm.allocation.AllocationContext;
import de.uka.ipd.sdq.pcm.allocation.AllocationFactory;
import de.uka.ipd.sdq.pcm.core.composition.AssemblyContext;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceContainer;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceEnvironment;
import de.uka.ipd.sdq.pcm.system.System;

/**
 * @author voegele
 * 
 */
public class AllocationCreator extends CreatorTools {

	/**
	 * @param resourceSet
	 * @param workloadModel
	 * @throws IOException
	 */
	public final void updateAllocation(final ResourceSet resourceSet,
			WorkloadModel workloadModel) throws IOException {

		Resource allocationResource = null;
		Allocation allocation = null;
		EObject rootTarget;

		log.info("- UPDATE ALLOCATION MODEL");

		// check if Allocation exists, if yes load it
		if (Configuration.getAllocationFile().exists()) {
			// load TargetResource
			allocationResource = resourceSet.getResource(URI
					.createFileURI(Configuration.getAllocationFile()
							.getAbsolutePath()), true);
			allocationResource.load(Collections.EMPTY_MAP);
			rootTarget = allocationResource.getContents().get(0);
			allocation = (Allocation) rootTarget;

			// load System
			final Resource systemResource = resourceSet.getResource(URI
					.createFileURI(Configuration.getSystemFile()
							.getAbsolutePath()), true);
			systemResource.load(Collections.EMPTY_MAP);
			final EObject rootSystem = systemResource.getContents().get(0);
			final System system = (System) rootSystem;

			// load ResourceEnvironment
			final Resource resourceEnvironmentResource = resourceSet
					.getResource(URI.createFileURI(Configuration
							.getResourceenvironmentFile().getAbsolutePath()),
							true);
			resourceEnvironmentResource.load(Collections.EMPTY_MAP);
			final EObject rootEnvironment = resourceEnvironmentResource
					.getContents().get(0);
			final ResourceEnvironment resourceEnvironment = (ResourceEnvironment) rootEnvironment;

			// set Environments
			allocation.setSystem_Allocation(system);
			allocation
					.setTargetResourceEnvironment_Allocation(resourceEnvironment);

			// allocate components to behaviorModelContainer
			EList<BehaviorModel> behaviorModelList = workloadModel
					.getBehaviorModels();

			for (BehaviorModel behaviorModel : behaviorModelList) {
				createAllocationContext(system, allocation,
						resourceEnvironment,
						getAssemblyName(behaviorModel.getName()),
						Configuration.BEHAVIORMODELCONTAINER);
			}

			// save
			allocationResource.save(null);

		}
	}

	/**
	 * Create new allocationContext.
	 * 
	 * @param system
	 * @param allocation
	 * @param resourceEnvironment
	 * @param assemblyName
	 * @param containerName
	 */
	private void createAllocationContext(final System system,
			final Allocation allocation,
			final ResourceEnvironment resourceEnvironment,
			final String assemblyName, final String containerName) {
		// get all AllocationContexts
		final EList<AssemblyContext> assemblyContexts = system
				.getAssemblyContexts__ComposedStructure();
		for (final AssemblyContext assemblyContext : assemblyContexts) {
			if (assemblyContext.getEntityName().equals(assemblyName)) {

				boolean allocationExists = false;
				final EList<AllocationContext> allocationContexts = allocation
						.getAllocationContexts_Allocation();
				for (final AllocationContext allocationContext : allocationContexts) {
					if (allocationContext
							.getResourceContainer_AllocationContext()
							.getEntityName().equals(containerName)
							&& allocationContext.getEntityName().equals(
									assemblyName)) {
						allocationExists = true;
					}
				}

				if (!allocationExists) {
					final AllocationContext allocationContext = AllocationFactory.eINSTANCE
							.createAllocationContext();
					allocationContext
							.setAssemblyContext_AllocationContext(assemblyContext);
					allocationContext.setEntityName(assemblyName);
					// get all ResourceContainer
					final EList<ResourceContainer> resourceContainers = resourceEnvironment
							.getResourceContainer_ResourceEnvironment();
					for (final ResourceContainer resourceContainer : resourceContainers) {
						if (resourceContainer.getEntityName().equals(
								containerName)) {
							allocationContext
									.setResourceContainer_AllocationContext(resourceContainer);
						}
					}
					// add allocationContext to allocation
					allocation.getAllocationContexts_Allocation().add(
							allocationContext);
				}
			}
		}
	}

}
