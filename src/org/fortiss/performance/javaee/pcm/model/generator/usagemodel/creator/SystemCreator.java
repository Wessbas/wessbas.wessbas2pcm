package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import m4jdsl.BehaviorModel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;

import de.uka.ipd.sdq.pcm.core.composition.AssemblyConnector;
import de.uka.ipd.sdq.pcm.core.composition.AssemblyContext;
import de.uka.ipd.sdq.pcm.core.composition.CompositionFactory;
import de.uka.ipd.sdq.pcm.core.composition.Connector;
import de.uka.ipd.sdq.pcm.core.composition.ProvidedDelegationConnector;
import de.uka.ipd.sdq.pcm.core.entity.ComposedProvidingRequiringEntity;
import de.uka.ipd.sdq.pcm.repository.Interface;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.repository.ProvidedRole;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.repository.RequiredRole;
import de.uka.ipd.sdq.pcm.system.System;

/**
 * This class updates the system model. For each newly created behaviorComponent
 * in the repository, create a new assembly.
 * 
 * @author voegele
 * 
 */
public class SystemCreator {

	CreatorTools creatorTools = CreatorTools.getInstance();

	/**
	 * updates System Model.
	 */
	public final void updateSystem() {

		Resource systemResource = null;
		EObject rootSystem;

		try {

			creatorTools.log.info("- UPDATE SYSTEM MODEL");

			// load system
			if (Configuration.getSystemFile().exists()) {

				systemResource = creatorTools.getResourceSet().getResource(
						URI.createFileURI(Configuration.getSystemFile()
								.getAbsolutePath()), true);
				systemResource.load(Collections.EMPTY_MAP);
				rootSystem = systemResource.getContents().get(0);
				creatorTools.setThisSystem((System) rootSystem);

				EList<BehaviorModel> behaviorModels = creatorTools.getThisWorkloadModel()
						.getBehaviorModels();

				// create an assembly for each behaviorModel
				for (BehaviorModel behaviorModel : behaviorModels) {

					// create new assembly
					createAssemblyContext(behaviorModel.getName(),
							creatorTools.getAssemblyName(behaviorModel
									.getName()));

					// create new OperationProvidedRole and connect with new
					// assembly
					createOperationProvidedRole(behaviorModel.getName());

				}

				// set connections between the new assembly and the other
				// assemblies
				connectAssemblies(creatorTools.getThisSystem());

				systemResource.save(null);

			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create new OperationProvidedRole.
	 * 
	 * @param componentName
	 */
	private final void createOperationProvidedRole(final String componentName) {

		// create new OperationProvidedRole
		final OperationProvidedRole operationProvidedRole = RepositoryFactory.eINSTANCE
				.createOperationProvidedRole();

		final EList<Interface> interfaces = creatorTools.getThisRepository()
				.getInterfaces__Repository();
		for (final Interface interfaceInstance : interfaces) {
			final OperationInterface oi = (OperationInterface) interfaceInstance;
			if (oi.getEntityName().equals(componentName)) {
				operationProvidedRole
						.setProvidedInterface__OperationProvidedRole(oi);
				operationProvidedRole
						.setEntityName("Provided_" + componentName);
				creatorTools.getThisSystem()
						.getProvidedRoles_InterfaceProvidingEntity()
						.add(operationProvidedRole);
			}
		}

		// set ProvidedDelegationConnector
		final ProvidedDelegationConnector pdc = CompositionFactory.eINSTANCE
				.createProvidedDelegationConnector();
		pdc.setEntityName("ProvDelegation_" + componentName);
		pdc.setOuterProvidedRole_ProvidedDelegationConnector(operationProvidedRole);
		final EList<RepositoryComponent> componentRepositorys = creatorTools
				.getThisRepository().getComponents__Repository();
		for (final RepositoryComponent componentRepository : componentRepositorys) {
			// get provided roles of component
			final EList<ProvidedRole> providedRoles = componentRepository
					.getProvidedRoles_InterfaceProvidingEntity();
			for (final ProvidedRole providedRole : providedRoles) {
				final OperationProvidedRole innerProvidedRole = (OperationProvidedRole) providedRole;
				if (innerProvidedRole
						.getProvidedInterface__OperationProvidedRole()
						.getEntityName().equals(componentName)) {
					pdc.setInnerProvidedRole_ProvidedDelegationConnector(innerProvidedRole);
				}
			}
		}

		// set assemblyContext
		final EList<AssemblyContext> assemblyContexts = creatorTools
				.getThisSystem().getAssemblyContexts__ComposedStructure();
		for (final AssemblyContext assemblyContext : assemblyContexts) {
			if (assemblyContext.getEntityName().equals(
					creatorTools.getAssemblyName(componentName))) {
				pdc.setAssemblyContext_ProvidedDelegationConnector(assemblyContext);
			}
		}
		creatorTools.getThisSystem().getConnectors__ComposedStructure()
				.add(pdc);
	}

	/**
	 * Create new assembly with the specified name.
	 * 
	 * @param assemblyName
	 * @param assemblyContextName
	 * @return AssemblyContext
	 */
	private AssemblyContext createAssemblyContext(final String assemblyName,
			final String assemblyContextName) {

		AssemblyContext assemblyContext = null;

		// load all repository components and add as AssemblyContext
		final EList<RepositoryComponent> componentRepositorys = creatorTools
				.getThisRepository().getComponents__Repository();
		for (RepositoryComponent componentRepository : componentRepositorys) {
			if (componentRepository.getEntityName().equals(assemblyName)) {
				assemblyContext = CompositionFactory.eINSTANCE
						.createAssemblyContext();
				assemblyContext.setEntityName(assemblyContextName);
				assemblyContext
						.setEncapsulatedComponent__AssemblyContext(componentRepository);
				creatorTools.getThisSystem()
						.getAssemblyContexts__ComposedStructure()
						.add(assemblyContext);
				break;
			}
		}

		return assemblyContext;
	}

	/**
	 * Connects all assemblies of a composed entity. Only the assemblies of this
	 * entity are connected; if some requirements are missing, because they are
	 * provided on another level, the assembly will not be connected.
	 * 
	 * @param composedEntity
	 *            the CompositeComponent or System which should be internally
	 *            connected
	 */
	private void connectAssemblies(
			ComposedProvidingRequiringEntity composedEntity) {
		for (AssemblyContext assemblyContext : composedEntity
				.getAssemblyContexts__ComposedStructure()) {
			RepositoryComponent repositoryComponent = assemblyContext
					.getEncapsulatedComponent__AssemblyContext();
			List<RequiredRole> requiredRoles = repositoryComponent
					.getRequiredRoles_InterfaceRequiringEntity();
			for (RequiredRole requiredRole : requiredRoles) {
				if (requiredRole instanceof OperationRequiredRole) {
					OperationRequiredRole operationRequiredRole = (OperationRequiredRole) requiredRole;
					OperationInterface requiredInterface = operationRequiredRole
							.getRequiredInterface__OperationRequiredRole();
					for (AssemblyContext providingAssemblyContext : composedEntity
							.getAssemblyContexts__ComposedStructure()) {
						RepositoryComponent providingComponent = providingAssemblyContext
								.getEncapsulatedComponent__AssemblyContext();
						OperationProvidedRole operationProvidedRole = findOperationProvidedRole(
								providingComponent, requiredInterface);
						if (operationProvidedRole != null) {
							AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE
									.createAssemblyConnector();
							assemblyConnector
									.setProvidedRole_AssemblyConnector(operationProvidedRole);
							assemblyConnector
									.setRequiredRole_AssemblyConnector(operationRequiredRole);
							assemblyConnector
									.setProvidingAssemblyContext_AssemblyConnector(providingAssemblyContext);
							assemblyConnector
									.setRequiringAssemblyContext_AssemblyConnector(assemblyContext);
							assemblyConnector
									.setEntityName(providingAssemblyContext
											.getEntityName()
											+ "_"
											+ assemblyContext.getEntityName()
											+ "_"
											+ operationProvidedRole
													.getEntityName());
							// Check if connector doesn't exist already
							boolean exists = false;
							for (Connector connector : composedEntity
									.getConnectors__ComposedStructure()) {
								if (connector instanceof AssemblyConnector) {
									AssemblyConnector aConnector = (AssemblyConnector) connector;
									if (aConnector
											.getProvidedRole_AssemblyConnector()
											.equals(operationProvidedRole)
											&& aConnector
													.getRequiredRole_AssemblyConnector()
													.equals(operationRequiredRole)
											&& aConnector
													.getRequiringAssemblyContext_AssemblyConnector()
													.equals(assemblyContext)) {
										exists = true;
									}
								}
							}
							if (!exists) {
								composedEntity
										.getConnectors__ComposedStructure()
										.add(assemblyConnector);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks whether a given RepositoryComponent provides a given interface.
	 * 
	 * @param repositoryComponent
	 *            the RepositoryComponent to be checked
	 * @param operationInterface
	 *            the OperationInterface which we are searching for
	 * @return an OperationProvidedRole, if the component provides the needed
	 *         interfaces, otherwise null
	 */
	private OperationProvidedRole findOperationProvidedRole(
			RepositoryComponent repositoryComponent,
			OperationInterface operationInterface) {
		for (ProvidedRole providedRole : repositoryComponent
				.getProvidedRoles_InterfaceProvidingEntity()) {
			if (providedRole instanceof OperationProvidedRole) {
				OperationProvidedRole operationProvidedRole = (OperationProvidedRole) providedRole;
				if (operationInterface.equals(operationProvidedRole
						.getProvidedInterface__OperationProvidedRole())) {
					return operationProvidedRole;
				}
			}
		}
		return null;
	}

}
