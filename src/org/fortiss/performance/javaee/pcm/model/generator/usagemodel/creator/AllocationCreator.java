package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;

import m4jdsl.BehaviorModel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;

import de.uka.ipd.sdq.pcm.allocation.Allocation;
import de.uka.ipd.sdq.pcm.allocation.AllocationContext;
import de.uka.ipd.sdq.pcm.allocation.AllocationFactory;
import de.uka.ipd.sdq.pcm.core.composition.AssemblyContext;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceContainer;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceEnvironment;

/**
 * Allocated the new assemblyContexts to the resourceenvironment.
 * 
 * @author voegele
 * 
 */
public class AllocationCreator {

	private CreatorTools creatorTools = CreatorTools.getInstance();

	/**
	 * @param resourceSet
	 * @param workloadModel
	 * @throws IOException
	 */
	public final void updateAllocation()
			throws IOException {

		Resource allocationResource = null;
		EObject rootTarget;

		creatorTools.log.info("- UPDATE ALLOCATION MODEL");

		// check if Allocation exists, if yes load it
		if (Configuration.getAllocationFile().exists()) {

			// load TargetResource
			allocationResource = creatorTools.getResourceSet().getResource(
					URI.createFileURI(Configuration.getAllocationFile()
							.getAbsolutePath()), true);
			allocationResource.load(Collections.EMPTY_MAP);
			rootTarget = allocationResource.getContents().get(0);
			creatorTools.setThisAllocation((Allocation) rootTarget);

			// load ResourceEnvironment
			final Resource resourceEnvironmentResource = creatorTools
					.getResourceSet().getResource(
							URI.createFileURI(Configuration
									.getResourceenvironmentFile()
									.getAbsolutePath()), true);
			resourceEnvironmentResource.load(Collections.EMPTY_MAP);
			final EObject rootEnvironment = resourceEnvironmentResource
					.getContents().get(0);
			creatorTools
					.setThisResourceEnvironment((ResourceEnvironment) rootEnvironment);

			// set Environments
			creatorTools.getThisAllocation().setSystem_Allocation(
					creatorTools.getThisSystem());
			creatorTools.getThisAllocation()
					.setTargetResourceEnvironment_Allocation(
							creatorTools.getThisResourceEnvironment());

			// allocate components to behaviorModelContainer
			EList<BehaviorModel> behaviorModelList = creatorTools.getThisWorkloadModel()
					.getBehaviorModels();

			for (BehaviorModel behaviorModel : behaviorModelList) {
				createAllocationContext(
						creatorTools.getAssemblyName(behaviorModel.getName()),
						Configuration.BEHAVIORMODELCONTAINER);
			}

			// save
			allocationResource.save(null);

		}
	}

	/**
	 * Create new allocationContext.
	 * 
	 * @param assemblyName
	 * @param containerName
	 */
	private void createAllocationContext(final String assemblyName,
			final String containerName) {
		// get all AllocationContexts
		final EList<AssemblyContext> assemblyContexts = creatorTools
				.getThisSystem().getAssemblyContexts__ComposedStructure();
		for (final AssemblyContext assemblyContext : assemblyContexts) {
			if (assemblyContext.getEntityName().equals(assemblyName)) {

				boolean allocationExists = false;
				final EList<AllocationContext> allocationContexts = creatorTools
						.getThisAllocation().getAllocationContexts_Allocation();
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
					final EList<ResourceContainer> resourceContainers = creatorTools
							.getThisResourceEnvironment()
							.getResourceContainer_ResourceEnvironment();
					for (final ResourceContainer resourceContainer : resourceContainers) {
						if (resourceContainer.getEntityName().equals(
								containerName)) {
							allocationContext
									.setResourceContainer_AllocationContext(resourceContainer);
						}
					}
					// add allocationContext to allocation
					creatorTools.getThisAllocation()
							.getAllocationContexts_Allocation()
							.add(allocationContext);
				}
			}
		}
	}

}
