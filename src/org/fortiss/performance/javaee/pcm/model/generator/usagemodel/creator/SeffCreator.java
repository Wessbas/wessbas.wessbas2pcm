package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.Transition;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.core.CoreFactory;
import de.uka.ipd.sdq.pcm.core.PCMRandomVariable;
import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.Repository;
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
public class SeffCreator extends CreatorTools {

	private ResourceSet thisResourceSet;
	private Transition[][] thisTransitionMatrix;

	/**
	 * create Seff method.
	 * 
	 * @param seff
	 * @param resourceSet
	 * @param repository
	 * @param transitionMatrix
	 */
	public final void createSeff(final ResourceDemandingSEFF seff,
			final ResourceSet resourceSet, final Repository repository,
			final Transition[][] transitionMatrix) {

		log.info("- CREATE WORKFLOW SEFF: "
				+ seff.getDescribedService__SEFF().getEntityName());

		try {
			thisResourceSet = resourceSet;
			thisTransitionMatrix = transitionMatrix;
			StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
			seff.getSteps_Behaviour().add(startAction);
			StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
			seff.getSteps_Behaviour().add(stopAction);
			BranchAction branchAction = createBranch(seff);

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

	private BranchAction createBranch(final ResourceDemandingSEFF seff)
			throws IOException {
		BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
		String compareNameOfSeff = null;
		int nbrOfTransitions = 0;
		for (int i = 0; i < thisTransitionMatrix.length; i++) {
			compareNameOfSeff = replaceNotAllowedCharacters(thisTransitionMatrix[i][0]
					.getFromRequestURL());
			if (compareNameOfSeff.equals(seff.getDescribedService__SEFF()
					.getEntityName())) {
				for (int s = 0; s < thisTransitionMatrix[i].length; s++) {
					if (thisTransitionMatrix[i][s].getProbability() > 0) {
						ProbabilisticBranchTransition probabilisticBranchTransition = createProbabilisticBranchTransition(
								thisTransitionMatrix[i][s], seff);
						probabilisticBranchTransition
								.setBranchAction_AbstractBranchTransition(branchAction);
						nbrOfTransitions++;
					}
				}
			}
		}

		if (nbrOfTransitions > 0) {
			return branchAction;
		} else {
			return null;
		}
	}

	private ProbabilisticBranchTransition createProbabilisticBranchTransition(
			Transition transition, final ResourceDemandingSEFF seff)
			throws IOException {
		ProbabilisticBranchTransition probabilisticBranchTransition = SeffFactory.eINSTANCE
				.createProbabilisticBranchTransition();
		ResourceDemandingBehaviour resourceDemandingBehaviour = SeffFactory.eINSTANCE
				.createResourceDemandingBehaviour();

		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		resourceDemandingBehaviour.getSteps_Behaviour().add(startAction);
		resourceDemandingBehaviour.getSteps_Behaviour().add(stopAction);

		// add delay
		InternalAction internalAction = SeffFactory.eINSTANCE
				.createInternalAction();
		internalAction.getResourceDemand_Action().add(
				getParametricResourceDemand(thisResourceSet, "DELAY",
						transition.getAverageThinkTime()));
		resourceDemandingBehaviour.getSteps_Behaviour().add(internalAction);

		String toRequestURL = replaceNotAllowedCharacters(transition
				.getToRequestURL());

		// add externalCall
		ExternalCallAction externalCallAction = createExternalCallAction(
				seff.getBasicComponent_ServiceEffectSpecification(),
				toRequestURL);

		if (externalCallAction != null) {
			resourceDemandingBehaviour.getSteps_Behaviour().add(
					externalCallAction);
		}

		// add next URL
		ExternalCallAction nextUserStep = createExternalCallAction(
				seff.getBasicComponent_ServiceEffectSpecification(),
				toRequestURL, Constants.WORKFLOWCOMPONENT);
		resourceDemandingBehaviour.getSteps_Behaviour().add(nextUserStep);

		// knoten verknüpfen
		startAction.setSuccessor_AbstractAction(internalAction);
		internalAction.setPredecessor_AbstractAction(startAction);

		if (externalCallAction != null) {
			internalAction.setSuccessor_AbstractAction(externalCallAction);
			externalCallAction.setPredecessor_AbstractAction(internalAction);
			externalCallAction.setSuccessor_AbstractAction(nextUserStep);
			nextUserStep.setPredecessor_AbstractAction(externalCallAction);
		} else {
			internalAction.setSuccessor_AbstractAction(nextUserStep);
		}

		nextUserStep.setSuccessor_AbstractAction(stopAction);
		stopAction.setPredecessor_AbstractAction(nextUserStep);

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
	 * @return ParametricResourceDemand
	 * @throws IOException
	 */
	private ParametricResourceDemand getParametricResourceDemand(
			final ResourceSet resourceSet, final String resourceDemandType,
			final double resourceDemand) throws IOException {
		final ParametricResourceDemand parametricResourceDemand = SeffPerformanceFactory.eINSTANCE
				.createParametricResourceDemand();

		// PCM Random Variable for processingRate
		final PCMRandomVariable pcmRandomVariable = CoreFactory.eINSTANCE
				.createPCMRandomVariable();

		// load ResourceRepository
		final ResourceRepository resourceRepository = getResourceRepository(resourceSet);
		for (final ResourceType resourceType : resourceRepository
				.getAvailableResourceTypes_ResourceRepository()) {
			if (resourceType instanceof ProcessingResourceType) {
				final ProcessingResourceType processingResourceType = (ProcessingResourceType) resourceType;
				if (processingResourceType.getEntityName().equals(
						resourceDemandType)) {
					parametricResourceDemand
							.setRequiredResource_ParametricResourceDemand(processingResourceType);
					pcmRandomVariable.setSpecification(new Double(
							resourceDemand).toString());
				}
			}
		}
		parametricResourceDemand
				.setSpecification_ParametericResourceDemand(pcmRandomVariable);
		return parametricResourceDemand;
	}

	/**
	 * @param bc
	 * @param operationName
	 * @return ExternalCallAction
	 */
	private ExternalCallAction createExternalCallAction(
			final BasicComponent bc, final String operationName) {
		ExternalCallAction externalAction = null;
		// find the required role of external call and create external call
		final EList<RequiredRole> requiredRoles = bc
				.getRequiredRoles_InterfaceRequiringEntity();
		for (final RequiredRole requiredRole : requiredRoles) {
			final OperationRequiredRole opreq = (OperationRequiredRole) requiredRole;
			final OperationInterface oi = opreq
					.getRequiredInterface__OperationRequiredRole();
			if (operationName.contains(oi.getEntityName())) {
				final EList<OperationSignature> operationSignatures = oi
						.getSignatures__OperationInterface();
				for (final OperationSignature operationSignature : operationSignatures) {
					externalAction = SeffFactory.eINSTANCE
							.createExternalCallAction();
					externalAction.setEntityName(operationName);
					externalAction
							.setCalledService_ExternalService(operationSignature);
					externalAction.setRole_ExternalService(opreq);
				}
			}
		}
		return externalAction;
	}

	/**
	 * @param bc
	 * @param operationName
	 * @return ExternalCallAction
	 */
	private ExternalCallAction createExternalCallAction(
			final BasicComponent bc, final String operationName,
			final String parentName) {
		ExternalCallAction externalAction = null;
		// find the required role of external call and create external call
		final EList<RequiredRole> requiredRoles = bc
				.getRequiredRoles_InterfaceRequiringEntity();
		for (final RequiredRole requiredRole : requiredRoles) {
			final OperationRequiredRole opreq = (OperationRequiredRole) requiredRole;
			final OperationInterface oi = opreq
					.getRequiredInterface__OperationRequiredRole();
			if (oi.getEntityName().equals(parentName)) {
				final EList<OperationSignature> operationSignatures = oi
						.getSignatures__OperationInterface();
				for (final OperationSignature operationSignature : operationSignatures) {
					if (operationName.contains(operationSignature
							.getEntityName())) {
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

}
