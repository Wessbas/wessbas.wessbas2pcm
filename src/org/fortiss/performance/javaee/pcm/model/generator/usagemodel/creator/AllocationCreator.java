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

import java.io.IOException;
import m4jdsl.BehaviorModel;
import org.eclipse.emf.common.util.EList;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import de.uka.ipd.sdq.pcm.allocation.AllocationContext;
import de.uka.ipd.sdq.pcm.allocation.AllocationFactory;
import de.uka.ipd.sdq.pcm.core.composition.AssemblyContext;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceContainer;

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

		creatorTools.log.info("- UPDATE ALLOCATION MODEL");

		// check if Allocation exists, if yes load it
		if (Configuration.getAllocationFile().exists()) {
			
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
			creatorTools.saveAllocation();

		}
	}

	/**
	 * Create new allocationContext.
	 * 
	 * @param assemblyName
	 * @param containerName
	 * @throws IOException 
	 */
	private void createAllocationContext(final String assemblyName,
			final String containerName) throws IOException {
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
