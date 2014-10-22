package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.Collections;

import m4jdsl.BehaviorModel;
import m4jdsl.MarkovState;
import m4jdsl.WorkloadModel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingSEFF;
import de.uka.ipd.sdq.pcm.seff.SeffFactory;

/**
 * This class creates new basic components into the existing PCM Repository
 * Model.
 * 
 * @author voegele
 * 
 */
public class RepositoryCreator {

	CreatorTools creatorTools = CreatorTools.getInstance();
	private SeffCreator seffCreator = new SeffCreator();

	/**
	 * 
	 * creates PCM Repository Model.
	 * 
	 * @param resourceSet
	 * @param workloadModel
	 */
	public final void createWorkflowComponent(WorkloadModel workloadModel) {

		Resource repositoryResource = null;
		EObject rootTarget;

		try {

			creatorTools.log.info("- CREATE WORKLOAD SPECIFICATION COMPONENTS");

			// check if repository exists, if yes load it
			if (Configuration.getRepositoryFile().exists()) {

				// load TargetResource
				repositoryResource = creatorTools.getResourceSet().getResource(
						URI.createFileURI(Configuration.getRepositoryFile()
								.getAbsolutePath()), true);
				repositoryResource.load(Collections.EMPTY_MAP);
				rootTarget = repositoryResource.getContents().get(0);
				creatorTools.setThisRepository((Repository) rootTarget);

				// get behaviorModels of the workloadModel
				EList<BehaviorModel> behaviorModelList = workloadModel
						.getBehaviorModels();

				// create a new component per behaviorModel
				for (BehaviorModel behaviorModel : behaviorModelList) {
					createBehaviorModelComponent(behaviorModel);
				}

				// save
				repositoryResource.save(null);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Create new behaviorModel component.
	 * 
	 * @param componentName
	 * @param OperationSignature
	 * @param navigationRules
	 */
	private void createBehaviorModelComponent(BehaviorModel behaviorModel) {
		BasicComponent bc = null;
		OperationInterface myInterface = null;

		// check if component exists
		EList<RepositoryComponent> rc = creatorTools.getThisRepository()
				.getComponents__Repository();
		for (RepositoryComponent repositoryComponent : rc) {
			if (repositoryComponent.getEntityName().equals(
					behaviorModel.getName())) {
				bc = (BasicComponent) repositoryComponent;
				break;
			}
		}

		// if component does not exist
		if (bc == null) {

			// create basicComponent
			bc = RepositoryFactory.eINSTANCE.createBasicComponent();
			bc.setEntityName(behaviorModel.getName());
			creatorTools.getThisRepository().getComponents__Repository()
					.add(bc);

			// add interface
			myInterface = RepositoryFactory.eINSTANCE
					.createOperationInterface();
			myInterface.setEntityName(behaviorModel.getName());
			creatorTools.getThisRepository().getInterfaces__Repository()
					.add(myInterface);

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
		// TODO: Set required roles only the really required components, not to
		// all
		// createRequiredRoleBetweenComponents(bc.getEntityName(), null,
		// repository);

		// create a operationSignature for each markovState having outgoing
		// transitions in the behaviorModel
		for (MarkovState markovState : behaviorModel.getMarkovStates()) {
			if (markovState.getOutgoingTransitions().size() > 0) {
				createOperationSignature(markovState.getService().getName(),
						myInterface);
			}
		}

		// create a new seff for the new operationSignature of the
		// behaviorModelComponent
		EList<OperationSignature> operationSignatures = myInterface
				.getSignatures__OperationInterface();
		for (OperationSignature operationSignature : operationSignatures) {
			seffInitializer(operationSignature, bc, behaviorModel);
		}

	}

	/**
	 * Create a operationSignature.
	 * 
	 * @param signatureName
	 * @param interfaceInstance
	 * @return OperationSignature
	 */
	private OperationSignature createOperationSignature(
			final String signatureName,
			final OperationInterface interfaceInstance) {

		OperationSignature operationSignature = null;

		// check if operationSignature already exists
		EList<OperationSignature> operationSignatures = interfaceInstance
				.getSignatures__OperationInterface();
		boolean operationSignatureExists = false;
		for (OperationSignature os : operationSignatures) {
			if (os.getEntityName().equals(signatureName)) {
				operationSignatureExists = true;
				break;
			}
		}

		// if operationSignatureExists does not exist create
		if (!operationSignatureExists) {
			// create signature names
			operationSignature = RepositoryFactory.eINSTANCE
					.createOperationSignature();
			operationSignature.setEntityName(signatureName);
			interfaceInstance.getSignatures__OperationInterface().add(
					operationSignature);

		}
		return operationSignature;
	}

	/**
	 * Create a new seff in the basic component and pass it to the seff creator
	 * to initialize it.
	 * 
	 * @param operationSignature
	 * @param bc
	 * @param resourceDemand
	 */
	private void seffInitializer(final OperationSignature operationSignature,
			final BasicComponent bc, BehaviorModel behaviorModel) {
		// create SEFF
		ResourceDemandingSEFF seff = SeffFactory.eINSTANCE
				.createResourceDemandingSEFF();
		seff.setBasicComponent_ServiceEffectSpecification(bc);
		seff.setDescribedService__SEFF(operationSignature);
		seffCreator.createSeff(seff, creatorTools.getResourceSet(),
				creatorTools.getThisRepository(), behaviorModel);
	}

}
