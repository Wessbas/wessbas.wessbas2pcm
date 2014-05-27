package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.Transition;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.Interface;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.repository.RequiredRole;
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingSEFF;
import de.uka.ipd.sdq.pcm.seff.SeffFactory;

/**
 * This class creates the PCM Repository
 */
public class RepositoryCreator extends CreatorTools {

	private ResourceSet thisResourceSet;
	private Repository repository;
	private SeffCreator seffCreator = new SeffCreator();
	private Transition[][] thisTransitionMatrix;

	/**
	 * 
	 * creates PCM Repository Model.
	 * 
	 * @param resourceSet
	 * @param navigationRules
	 */
	public final void createWorkflowComponent(final ResourceSet resourceSet,
			Transition[][] transitionMatrix) {

		thisResourceSet = resourceSet;
		thisTransitionMatrix = transitionMatrix;
		Resource repositoryResource = null;
		EObject rootTarget;

		try {

			log.info("- UPDATE WORKFLOW COMPONENT");

			// check if repository exists, if yes load it
			if (Configuration.getRepositoryFile().exists()) {
				// load TargetResource
				repositoryResource = thisResourceSet.getResource(URI
						.createFileURI(Configuration.getRepositoryFile()
								.getAbsolutePath()), true);
				repositoryResource.load(Collections.EMPTY_MAP);
				rootTarget = repositoryResource.getContents().get(0);
				repository = (Repository) rootTarget;

				createNewComponent();

				// save
				repositoryResource.save(null);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * create new component.
	 * 
	 * @param componentName
	 * @param OperationSignature
	 * @param navigationRules
	 */
	private void createNewComponent() {
		BasicComponent bc = null;
		OperationInterface myInterface = null;

		// check if component exists
		EList<RepositoryComponent> rc = repository.getComponents__Repository();
		for (RepositoryComponent repositoryComponent : rc) {
			if (repositoryComponent.getEntityName().equals(
					Constants.WORKFLOWCOMPONENT)) {
				bc = (BasicComponent) repositoryComponent;
				break;
			}
		}

		if (bc == null) {

			// create basicComponent
			bc = RepositoryFactory.eINSTANCE.createBasicComponent();
			bc.setEntityName(Constants.WORKFLOWCOMPONENT);
			repository.getComponents__Repository().add(bc);

			// add interface
			myInterface = RepositoryFactory.eINSTANCE
					.createOperationInterface();
			myInterface.setEntityName(Constants.WORKFLOWCOMPONENT);
			repository.getInterfaces__Repository().add(myInterface);

			// provided role
			OperationProvidedRole opProvRole = RepositoryFactory.eINSTANCE
					.createOperationProvidedRole();
			opProvRole.setEntityName("ProvidedRole");

			// set the interface for the role:
			opProvRole.setProvidedInterface__OperationProvidedRole(myInterface);

			// set/add the role for basic component:
			bc.getProvidedRoles_InterfaceProvidingEntity().add(opProvRole);

		}

		// setRequiredRoles to all other components
		createRequiredRoleBetweenComponents(bc.getEntityName(), null,
				repository);

		// create a operationSignature for each URL
		for (int i = 0; i < thisTransitionMatrix.length; i++) {
			createOperationSignature(
					thisTransitionMatrix[i][0].getFromRequestURL(), myInterface);
		}

		// initSeffs for the new operationSignatures of workflowcomponent
		EList<OperationSignature> operationSignatures = myInterface
				.getSignatures__OperationInterface();
		for (OperationSignature operationSignature : operationSignatures) {
			seffInitializer(operationSignature, bc);
		}

	}

	/**
	 * @param signatureName
	 * @param interfaceInstance
	 * @return
	 */
	private OperationSignature createOperationSignature(
			final String signatureName,
			final OperationInterface interfaceInstance) {

		OperationSignature operationSignature = null;

		// adjust signatureName
		String signatureNameNew = replaceNotAllowedCharacters(signatureName);

		// check if operationSignature already exists
		EList<OperationSignature> operationSignatures = interfaceInstance
				.getSignatures__OperationInterface();
		boolean operationSignatureExists = false;
		for (OperationSignature os : operationSignatures) {
			if (os.getEntityName().equals(signatureNameNew)) {
				operationSignatureExists = true;
				break;
			}
		}

		// if operationSignatureExists does not exist create
		if (!operationSignatureExists) {
			// create signature names
			operationSignature = RepositoryFactory.eINSTANCE
					.createOperationSignature();
			operationSignature.setEntityName(signatureNameNew);
			interfaceInstance.getSignatures__OperationInterface().add(
					operationSignature);

		}
		return operationSignature;
	}

	/**
	 * @param operationSignature
	 * @param bc
	 * @param resourceDemand
	 */
	private void seffInitializer(final OperationSignature operationSignature,
			final BasicComponent bc) {
		// create SEFF
		ResourceDemandingSEFF seff = SeffFactory.eINSTANCE
				.createResourceDemandingSEFF();
		seff.setBasicComponent_ServiceEffectSpecification(bc);
		seff.setDescribedService__SEFF(operationSignature);
		seffCreator.createSeff(seff, thisResourceSet, repository,
				thisTransitionMatrix);
	}

	/**
	 * 
	 * set required role to all required interfaces. When toComponent is set to
	 * null then the required role will be set to all existing interfaces.
	 * 
	 * @param componentName
	 * @param toComponent
	 * @param repository
	 */
	private void createRequiredRoleBetweenComponents(
			final String componentName, final String toComponent,
			final Repository repository) {

		final EList<RepositoryComponent> rc = repository
				.getComponents__Repository();
		final EList<Interface> interfaces = repository
				.getInterfaces__Repository();

		// set required role to other services
		for (final RepositoryComponent repositoryComponent : rc) {
			if (repositoryComponent.getEntityName().equals(componentName)) {
				for (final Interface interfaceInstance : interfaces) {

					// when toComponent null ist dann wird eine verbindung zu
					// alles interfaces hergestellt,
					// wenn toComponent einen Wert hat dann nur zu diesem
					// Interface
					if (toComponent == null
							|| interfaceInstance.getEntityName().equals(
									toComponent)
							|| interfaceInstance.getEntityName().equals(
									componentName)) {

						final EList<RequiredRole> requiredRoles = repositoryComponent
								.getRequiredRoles_InterfaceRequiringEntity();

						// check if role already existst
						boolean requiredRoleExits = false;
						for (final RequiredRole requiredRole : requiredRoles) {
							if (requiredRole.getEntityName().equals(
									interfaceInstance.getEntityName())) {
								requiredRoleExits = true;
								break;
							}
						}

						if (!requiredRoleExits) {
							// create required Role
							final OperationRequiredRole opReqRole = RepositoryFactory.eINSTANCE
									.createOperationRequiredRole();
							opReqRole.setEntityName(interfaceInstance
									.getEntityName());
							repositoryComponent
									.getRequiredRoles_InterfaceRequiringEntity()
									.add(opReqRole);
							final OperationInterface oi = (OperationInterface) interfaceInstance;
							opReqRole
									.setRequiredInterface__OperationRequiredRole(oi);
						}
					}
				}
			}
		}
	}

}
