package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.core.composition.AssemblyConnector;
import de.uka.ipd.sdq.pcm.core.composition.AssemblyContext;
import de.uka.ipd.sdq.pcm.core.composition.CompositionFactory;
import de.uka.ipd.sdq.pcm.core.composition.ProvidedDelegationConnector;
import de.uka.ipd.sdq.pcm.repository.Interface;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.repository.ProvidedRole;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.repository.RequiredRole;
import de.uka.ipd.sdq.pcm.system.System;

public class SystemCreator extends CreatorTools {

	/**
	 * updates System Model.
	 */
	public final void updateSystem(final ResourceSet resourceSet) {

		Resource systemResource = null;
		Resource repositoryResource;
		System system = null;
		EObject rootRepository;
		EObject rootSystem;
		Repository repository;

		try {

			log.info("- UPDATE SYSTEM MODEL");

			// load system
			if (Configuration.getSystemFile().exists()) {
				systemResource = resourceSet.getResource(URI
						.createFileURI(Configuration.getSystemFile()
								.getAbsolutePath()), true);
				systemResource.load(Collections.EMPTY_MAP);
				rootSystem = systemResource.getContents().get(0);
				system = (System) rootSystem;

				// load repository
				repositoryResource = resourceSet.getResource(URI
						.createFileURI(Configuration.getRepositoryFile()
								.getAbsolutePath()), true);
				repositoryResource.load(Collections.EMPTY_MAP);
				rootRepository = repositoryResource.getContents().get(0);
				repository = (Repository) rootRepository;

				// create new assembly for the testcase component
				createAssemblyContext(system, repository,
						Configuration.WORKFLOWCOMPONENT,
						Configuration.WORKFLOWCOMPONENTASSEMBLY);

				// set connections between new component and the other
				// assemblies
				createAssemplyConnector(system, repository,
						Configuration.WORKFLOWCOMPONENT);

				// create new OperationProvidedRole and connect with new
				// assembly
				createOperationProvidedRole(system, repository,
						Configuration.WORKFLOWCOMPONENT);

				systemResource.save(null);

			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param system
	 * @param repository
	 */
	public final void createOperationProvidedRole(final System system,
			final Repository repository, final String componentName) {

		// create new OperationProvidedRole
		final OperationProvidedRole operationProvidedRole = RepositoryFactory.eINSTANCE
				.createOperationProvidedRole();

		final EList<Interface> interfaces = repository
				.getInterfaces__Repository();
		for (final Interface interfaceInstance : interfaces) {
			final OperationInterface oi = (OperationInterface) interfaceInstance;
			if (oi.getEntityName().equals(componentName)) {
				operationProvidedRole
						.setProvidedInterface__OperationProvidedRole(oi);
				operationProvidedRole
						.setEntityName("Provided_" + componentName);
				system.getProvidedRoles_InterfaceProvidingEntity().add(
						operationProvidedRole);
			}
		}

		// set ProvidedDelegationConnector
		final ProvidedDelegationConnector pdc = CompositionFactory.eINSTANCE
				.createProvidedDelegationConnector();
		pdc.setEntityName("ProvDelegation_" + componentName);
		pdc.setOuterProvidedRole_ProvidedDelegationConnector(operationProvidedRole);
		final EList<RepositoryComponent> componentRepositorys = repository
				.getComponents__Repository();
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
		final EList<AssemblyContext> assemblyContexts = system
				.getAssemblyContexts__ComposedStructure();
		for (final AssemblyContext assemblyContext : assemblyContexts) {
			if (assemblyContext.getEntityName().equals(
					Configuration.WORKFLOWCOMPONENTASSEMBLY)) {
				pdc.setAssemblyContext_ProvidedDelegationConnector(assemblyContext);
			}
		}
		system.getConnectors__ComposedStructure().add(pdc);
	}

	/**
	 * Create new assembly with the specified name String assembly
	 * 
	 * @param system
	 * @param repository
	 * @param assembly
	 */
	public final void createAssemblyContext(final System system,
			final Repository repository, final String assemblyName,
			final String assemblyContextName) {

		// load all repository components and add as AssemblyContext
		final EList<RepositoryComponent> componentRepositorys = repository
				.getComponents__Repository();
		for (final RepositoryComponent componentRepository : componentRepositorys) {
			if (componentRepository.getEntityName().equals(assemblyName)) {
				final AssemblyContext assemblyContext = CompositionFactory.eINSTANCE
						.createAssemblyContext();
				assemblyContext.setEntityName(assemblyContextName);
				assemblyContext
						.setEncapsulatedComponent__AssemblyContext(componentRepository);
				system.getAssemblyContexts__ComposedStructure().add(
						assemblyContext);
			}
		}

	}

	/**
	 * create new AssemplyConnector between the component and the required
	 * assemplies
	 * 
	 * @param system
	 * @param repository
	 * @param component
	 */
	public final void createAssemplyConnector(final System system,
			final Repository repository, final String component) {

		// set connections from required roles to provides roles
		final EList<RepositoryComponent> componentRepositorys = repository
				.getComponents__Repository();
		for (final RepositoryComponent componentRepository : componentRepositorys) {
			if (componentRepository.getEntityName().equals(component)) {
				// get required roles of component
				final EList<RequiredRole> requiredRoles = componentRepository
						.getRequiredRoles_InterfaceRequiringEntity();
				for (final RequiredRole requiredRole : requiredRoles) {
					final OperationRequiredRole operationRequiredRole = (OperationRequiredRole) requiredRole;

					// create new assemblyConnector
					final AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE
							.createAssemblyConnector();

					// 1) add operationRequiredRole to assemblyConnector
					assemblyConnector
							.setRequiredRole_AssemblyConnector(operationRequiredRole);

					// search components with provided role and than ad to the
					// new assemblyConnector
					for (final RepositoryComponent componentRepositoryInstance : componentRepositorys) {
						final EList<ProvidedRole> providedRoles = componentRepositoryInstance
								.getProvidedRoles_InterfaceProvidingEntity();
						for (final ProvidedRole providedRole : providedRoles) {
							final OperationProvidedRole opr = (OperationProvidedRole) providedRole;
							if (operationRequiredRole
									.getRequiredInterface__OperationRequiredRole()
									.getId()
									.equals(opr
											.getProvidedInterface__OperationProvidedRole()
											.getId())) {

								// 2) add operationProvidedRole to
								// assemblyConnector
								assemblyConnector
										.setProvidedRole_AssemblyConnector(opr);
							}
						}
					}

					final EList<AssemblyContext> assemblyContexts = system
							.getAssemblyContexts__ComposedStructure();
					for (final AssemblyContext assemblyContext : assemblyContexts) {
						final RepositoryComponent repositoryComponentInstance = assemblyContext
								.getEncapsulatedComponent__AssemblyContext();
						if (repositoryComponentInstance.getEntityName().equals(
								component)) {
							// 3) setRequiringAssemblyContext assemblyContext to
							// assemblyConnector
							assemblyConnector
									.setRequiringAssemblyContext_AssemblyConnector(assemblyContext);
						}
						final EList<ProvidedRole> providedRoles = repositoryComponentInstance
								.getProvidedRoles_InterfaceProvidingEntity();
						if (providedRoles.size() <= 1) {
							for (final ProvidedRole providedRole : providedRoles) {
								final OperationProvidedRole operationProvidedRoleInstance = (OperationProvidedRole) providedRole;
								if (operationRequiredRole
										.getRequiredInterface__OperationRequiredRole()
										.getId()
										.equals(operationProvidedRoleInstance
												.getProvidedInterface__OperationProvidedRole()
												.getId())) {

									// 4) setProvidingAssemblyContext to
									// assemblyConnector
									assemblyConnector
											.setProvidingAssemblyContext_AssemblyConnector(assemblyContext);
								}
							}
						}
					}

					// 5 add EntityName
					assemblyConnector.setEntityName("AssemblyConnector");
					system.getConnectors__ComposedStructure().add(
							assemblyConnector);
				}
			}
		}
	}

}
