package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;

import m4jdsl.BehaviorModel;
import m4jdsl.MarkovState;
import m4jdsl.NormallyDistributedThinkTime;
import m4jdsl.Transition;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.core.CoreFactory;
import de.uka.ipd.sdq.pcm.core.PCMRandomVariable;
import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.Interface;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.repository.RequiredRole;
import de.uka.ipd.sdq.pcm.resourcetype.ProcessingResourceType;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceRepository;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceType;
import de.uka.ipd.sdq.pcm.seff.BranchAction;
import de.uka.ipd.sdq.pcm.seff.ExternalCallAction;
import de.uka.ipd.sdq.pcm.seff.InternalAction;
import de.uka.ipd.sdq.pcm.seff.ProbabilisticBranchTransition;
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingBehaviour;
import de.uka.ipd.sdq.pcm.seff.ResourceDemandingSEFF;
import de.uka.ipd.sdq.pcm.seff.SeffFactory;
import de.uka.ipd.sdq.pcm.seff.StartAction;
import de.uka.ipd.sdq.pcm.seff.StopAction;
import de.uka.ipd.sdq.pcm.seff.seff_performance.ParametricResourceDemand;
import de.uka.ipd.sdq.pcm.seff.seff_performance.SeffPerformanceFactory;

/**
 * @author voegele
 * 
 */
public class SeffCreator {

	CreatorTools creatorTools = CreatorTools.getInstance();
	private ResourceSet thisResourceSet;
	private Repository thisRepository;

	/**
	 * Create seff for the markovState of the behaviorModel.
	 * 
	 * @param seff
	 * @param resourceSet
	 * @param repository
	 * @param behaviorModel
	 */
	public final void createSeff(final ResourceDemandingSEFF seff,
			final ResourceSet resourceSet, final Repository repository,
			final BehaviorModel behaviorModel) {

		creatorTools.log.info("- CREATE BEHAVIORMODEL SEFF: "
				+ seff.getDescribedService__SEFF().getEntityName());

		try {
			thisResourceSet = resourceSet;
			thisRepository = repository;

			// create start, stop and branchAction
			StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
			seff.getSteps_Behaviour().add(startAction);
			StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
			seff.getSteps_Behaviour().add(stopAction);
			BranchAction branchAction = createBranch(seff, behaviorModel);

			// connect new nodes
			if (branchAction != null) {
				seff.getSteps_Behaviour().add(branchAction);
				startAction.setSuccessor_AbstractAction(branchAction);
				branchAction.setPredecessor_AbstractAction(startAction);
				branchAction.setSuccessor_AbstractAction(stopAction);
				stopAction.setPredecessor_AbstractAction(branchAction);
			} else {
				startAction.setSuccessor_AbstractAction(stopAction);
				stopAction.setPredecessor_AbstractAction(startAction);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create a new branchAction in the seff.
	 * 
	 * @param seff
	 * @param behaviorModel
	 * @return BranchAction
	 * @throws IOException
	 */
	private BranchAction createBranch(final ResourceDemandingSEFF seff,
			final BehaviorModel behaviorModel) throws IOException {
		BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
		for (MarkovState markovState : behaviorModel.getMarkovStates()) {

			// identify markovState with seff name
			if (markovState.getService().getName()
					.equals(seff.getDescribedService__SEFF().getEntityName())) {

				// for each toutgoing transition of the markovState, create a
				// new probabilisticBranchTransition
				for (Transition transition : markovState
						.getOutgoingTransitions()) {
					ProbabilisticBranchTransition probabilisticBranchTransition = createProbabilisticBranchTransition(
							transition, seff, behaviorModel);
					probabilisticBranchTransition
							.setBranchAction_AbstractBranchTransition(branchAction);
				}
			}
		}
		return branchAction;
	}

	/**
	 * Create a new probabilisticBranchTransition for each outgoing transition
	 * of the markovState.
	 * 
	 * @param transition
	 * @param seff
	 * @param behaviorModel
	 * @return ProbabilisticBranchTransition
	 * @throws IOException
	 */
	private ProbabilisticBranchTransition createProbabilisticBranchTransition(
			Transition transition, final ResourceDemandingSEFF seff,
			BehaviorModel behaviorModel) throws IOException {
		ProbabilisticBranchTransition probabilisticBranchTransition = SeffFactory.eINSTANCE
				.createProbabilisticBranchTransition();
		ResourceDemandingBehaviour resourceDemandingBehaviour = SeffFactory.eINSTANCE
				.createResourceDemandingBehaviour();

		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		resourceDemandingBehaviour.getSteps_Behaviour().add(startAction);
		resourceDemandingBehaviour.getSteps_Behaviour().add(stopAction);

		// add thinkTime of transition as internal action with delay
		InternalAction internalAction = SeffFactory.eINSTANCE
				.createInternalAction();

		final NormallyDistributedThinkTime normallyDistributedThinkTime = (NormallyDistributedThinkTime) transition
				.getThinkTime();

		internalAction.getResourceDemand_Action().add(
				getParametricResourceDemand(thisResourceSet, "DELAY",
						normallyDistributedThinkTime.getMean(),
						normallyDistributedThinkTime.getDeviation()));

		resourceDemandingBehaviour.getSteps_Behaviour().add(internalAction);

		// add externalCall to the system
		// TODO: Create better solution for target component
		ExternalCallAction externalCallAction = createExternalCallAction(
				seff.getBasicComponent_ServiceEffectSpecification(), transition
						.getTargetState().getEId(), "app");

		if (externalCallAction != null) {
			resourceDemandingBehaviour.getSteps_Behaviour().add(
					externalCallAction);
		}

		// add next markov state, outgoingTransition
		ExternalCallAction targetState = createExternalCallAction(
				seff.getBasicComponent_ServiceEffectSpecification(), transition
						.getTargetState().getEId(), behaviorModel.getName());

		if (targetState != null) {
			resourceDemandingBehaviour.getSteps_Behaviour().add(targetState);
		}

		// connect new nodes
		startAction.setSuccessor_AbstractAction(internalAction);
		internalAction.setPredecessor_AbstractAction(startAction);

		if (externalCallAction != null && targetState != null) {
			internalAction.setSuccessor_AbstractAction(externalCallAction);
			externalCallAction.setPredecessor_AbstractAction(internalAction);
			externalCallAction.setSuccessor_AbstractAction(targetState);
			targetState.setPredecessor_AbstractAction(externalCallAction);
			targetState.setSuccessor_AbstractAction(stopAction);
			stopAction.setPredecessor_AbstractAction(targetState);
		} else if (externalCallAction == null && targetState != null) {
			internalAction.setSuccessor_AbstractAction(targetState);
			targetState.setPredecessor_AbstractAction(internalAction);
			targetState.setSuccessor_AbstractAction(stopAction);
			stopAction.setPredecessor_AbstractAction(targetState);
		} else if (externalCallAction != null && targetState == null) {
			internalAction.setSuccessor_AbstractAction(externalCallAction);
			externalCallAction.setPredecessor_AbstractAction(internalAction);
			externalCallAction.setSuccessor_AbstractAction(stopAction);
			stopAction.setPredecessor_AbstractAction(externalCallAction);
		} else {
			internalAction.setSuccessor_AbstractAction(stopAction);
		}

		probabilisticBranchTransition
				.setBranchBehaviour_BranchTransition(resourceDemandingBehaviour);
		probabilisticBranchTransition.setBranchProbability(transition
				.getProbability());
		return probabilisticBranchTransition;
	}

	/**
	 * @param resourceSet
	 * @param resourceDemandType
	 * @param resourceDemand
	 * @param deviation
	 * @return ParametricResourceDemand
	 * @throws IOException
	 */
	private ParametricResourceDemand getParametricResourceDemand(
			final ResourceSet resourceSet, final String resourceDemandType,
			final double resourceDemand, final double deviation)
			throws IOException {
		final ParametricResourceDemand parametricResourceDemand = SeffPerformanceFactory.eINSTANCE
				.createParametricResourceDemand();

		// PCM Random Variable for processingRate
		final PCMRandomVariable pcmRandomVariable = CoreFactory.eINSTANCE
				.createPCMRandomVariable();

		// load ResourceRepository
		final ResourceRepository resourceRepository = creatorTools
				.getResourceRepository(resourceSet);
		for (final ResourceType resourceType : resourceRepository
				.getAvailableResourceTypes_ResourceRepository()) {
			if (resourceType instanceof ProcessingResourceType) {
				final ProcessingResourceType processingResourceType = (ProcessingResourceType) resourceType;
				if (processingResourceType.getEntityName().equals(
						resourceDemandType)) {
					parametricResourceDemand
							.setRequiredResource_ParametricResourceDemand(processingResourceType);

					// if deviation is zero just set the mean resource demand
					if (deviation == 0) {
						pcmRandomVariable.setSpecification(Double
								.toString(resourceDemand));
					} else {
						pcmRandomVariable.setSpecification("Norm ("
								+ resourceDemand + "," + deviation + ")");
					}
				}
			}
		}
		parametricResourceDemand
				.setSpecification_ParametericResourceDemand(pcmRandomVariable);
		return parametricResourceDemand;
	}

	/**
	 * Create externalCallAction to the system.
	 * 
	 * @param bc
	 * @param operationName
	 * @return ExternalCallAction
	 */
	private ExternalCallAction createExternalCallAction(
			final BasicComponent bc, final String operationName,
			final String componentName) {
		ExternalCallAction externalAction = null;
		EList<Interface> interfaceList = thisRepository
				.getInterfaces__Repository();
		for (Interface interfaceInstance : interfaceList) {
			// TODO: remove and a general solution.
			if (interfaceInstance.getEntityName().equals(componentName)) {
				OperationInterface oi = (OperationInterface) interfaceInstance;
				EList<OperationSignature> operationSignatureList = oi
						.getSignatures__OperationInterface();
				for (OperationSignature operationSignature : operationSignatureList) {
					if (operationName.contains(operationSignature
							.getEntityName())) {
						OperationRequiredRole opreq = createRequiredRoleBetweenComponents(
								bc.getEntityName(),
								interfaceInstance.getEntityName(),
								thisRepository);
						externalAction = SeffFactory.eINSTANCE
								.createExternalCallAction();
						externalAction.setEntityName(operationName);
						externalAction
								.setCalledService_ExternalService(operationSignature);
						externalAction.setRole_ExternalService(opreq);
					}
				}
			}
		}

		return externalAction;
	}

	/**
	 * 
	 * Set required role to all required interfaces. When toComponent is set to
	 * null then the required role will be set to all existing interfaces.
	 * 
	 * @param componentName
	 * 
	 * @param toInterface
	 * 
	 * @param repository
	 */
	private OperationRequiredRole createRequiredRoleBetweenComponents(
			final String fromComponent, final String toInterface,
			final Repository repository) {

		OperationRequiredRole opReqRole = null;

		final EList<RepositoryComponent> rc = repository
				.getComponents__Repository();
		final EList<Interface> interfaces = repository
				.getInterfaces__Repository();

		// set required role to other services
		for (final RepositoryComponent repositoryComponent : rc) {
			if (repositoryComponent.getEntityName().equals(fromComponent)) {
				for (final Interface interfaceInstance : interfaces) {

					// when toComponent null ist dann wird eine verbindung zu
					// alles interfaces hergestellt,
					// wenn toInterface einen Wert hat dann nur zu diesem
					// Interface
					if (toInterface == null
							|| interfaceInstance.getEntityName().equals(
									toInterface)) {

						final EList<RequiredRole> requiredRoles = repositoryComponent
								.getRequiredRoles_InterfaceRequiringEntity();

						// check if role already exists
						boolean requiredRoleExits = false;
						for (final RequiredRole requiredRole : requiredRoles) {
							if (requiredRole.getEntityName().equals(
									interfaceInstance.getEntityName())) {
								requiredRoleExits = true;
								opReqRole = (OperationRequiredRole) requiredRole;
								break;
							}
						}

						if (!requiredRoleExits) {
							// create required Role
							opReqRole = RepositoryFactory.eINSTANCE
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

		return opReqRole;
	}

}
