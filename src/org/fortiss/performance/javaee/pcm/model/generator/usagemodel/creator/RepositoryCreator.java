package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import m4jdsl.BehaviorModel;
import m4jdsl.MarkovState;
import org.eclipse.emf.common.util.EList;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingSEFF;
import de.uka.ipd.sdq.pcm.seff.SeffFactory;

/**
 * This class creates new basic components into the existing PCM Repository
 * Model representing the behaviorModels of the workload model.
 * 
 * @author voegele
 * 
 */
public class RepositoryCreator {

	private CreatorTools creatorTools = CreatorTools.getInstance();
	private SeffCreator seffCreator = new SeffCreator();

	/**
	 * 
	 * Initial method. Reads the existing repository model.
	 * 
	 * @param resourceSet
	 * @param workloadModel
	 * @throws IOException 
	 */
	public final void createWorkflowComponent() throws IOException {
		creatorTools.log.info("- CREATE WORKLOAD SPECIFICATION COMPONENTS");

		// check if repository exists, if yes load it
		if (Configuration.getRepositoryFile().exists()) {			

			// get behaviorModels of the workloadModel
			EList<BehaviorModel> behaviorModelList = creatorTools.getThisWorkloadModel()
					.getBehaviorModels();

			// create a new component per behaviorModel
			for (BehaviorModel behaviorModel : behaviorModelList) {
				createBehaviorModelComponent(behaviorModel);
			}

			// save
			creatorTools.saveRepository();

		}
	}

	/**
	 * Create new behaviorModel component.
	 * 
	 * @param behaviorModel
	 * @throws IOException 
	 */
	private void createBehaviorModelComponent(final BehaviorModel behaviorModel) throws IOException {
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
	 * @param behaviorModel
	 * @throws IOException 
	 */
	private void seffInitializer(final OperationSignature operationSignature,
			final BasicComponent bc, final BehaviorModel behaviorModel) throws IOException {
		// create SEFF
		ResourceDemandingSEFF seff = SeffFactory.eINSTANCE
				.createResourceDemandingSEFF();
		seff.setBasicComponent_ServiceEffectSpecification(bc);
		seff.setDescribedService__SEFF(operationSignature);
		seffCreator.createSeff(seff, creatorTools.getResourceSet(),
				creatorTools.getThisRepository(), behaviorModel);
	}

}
