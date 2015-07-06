package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import m4jdsl.Action;
import m4jdsl.ApplicationState;
import m4jdsl.ApplicationTransition;
import m4jdsl.BehaviorModel;
import m4jdsl.Guard;
import m4jdsl.GuardActionParameter;
import m4jdsl.GuardActionParameterType;
import m4jdsl.MarkovState;
import m4jdsl.NormallyDistributedThinkTime;
import m4jdsl.SessionLayerEFSM;
import m4jdsl.Transition;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;

import de.uka.ipd.sdq.pcm.core.PCMRandomVariable;
import de.uka.ipd.sdq.pcm.parameter.VariableUsage;
import de.uka.ipd.sdq.pcm.repository.BasicComponent;
import de.uka.ipd.sdq.pcm.repository.Interface;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.Parameter;
import de.uka.ipd.sdq.pcm.repository.ProvidedRole;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.repository.RepositoryFactory;
import de.uka.ipd.sdq.pcm.repository.RequiredRole;
import de.uka.ipd.sdq.pcm.resourcetype.ProcessingResourceType;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceRepository;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceType;
import de.uka.ipd.sdq.pcm.seff.BranchAction;
import de.uka.ipd.sdq.pcm.seff.ExternalCallAction;
import de.uka.ipd.sdq.pcm.seff.GuardedBranchTransition;
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
 * This class is called from the RepositoryCreator class. It created seff based
 * on the wmarkovStates of the workload model.
 * 
 * @author voegele
 * 
 */
public class SeffCreatorTree extends AbstractCreator {

	private CreatorTools creatorTools = CreatorTools.getInstance();
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

			ExternalCallAction firstExternalCallAction = null;
			ExternalCallAction nextExternalCallAction = null;
			BranchAction branchAction = null;			
			
			// check initial state
			if (seff.getDescribedService__SEFF().getEntityName().equals(Constants.INITIAL_NAME)) {
				firstExternalCallAction = createExternalCallActionSystem(
						seff.getBasicComponent_ServiceEffectSpecification(),
						behaviorModel.getInitialState().getService().getName());
				nextExternalCallAction = createExternalCallActionNext(
						seff.getBasicComponent_ServiceEffectSpecification(), 
						behaviorModel.getInitialState().getService().getName(),
						behaviorModel.getName(), null);
			} else {			
				branchAction = createGuardedBranchAction(seff, behaviorModel);
			}	
			
			// connect new nodes
			if (firstExternalCallAction != null && nextExternalCallAction != null) {
				seff.getSteps_Behaviour().add(firstExternalCallAction);
				seff.getSteps_Behaviour().add(nextExternalCallAction);
				startAction
						.setSuccessor_AbstractAction(firstExternalCallAction);
				firstExternalCallAction
						.setPredecessor_AbstractAction(startAction);
				firstExternalCallAction
						.setSuccessor_AbstractAction(nextExternalCallAction);
				nextExternalCallAction
						.setPredecessor_AbstractAction(firstExternalCallAction);
				nextExternalCallAction.setSuccessor_AbstractAction(stopAction);
				stopAction.setPredecessor_AbstractAction(nextExternalCallAction);
			} else if (branchAction != null) {
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
	private BranchAction createGuardedBranchAction(final ResourceDemandingSEFF seff,
			final BehaviorModel behaviorModel) throws IOException {
		BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
		for (MarkovState markovState : behaviorModel.getMarkovStates()) {
			// identify markovState with seff name
			if (markovState.getService().getName()
					.equals(seff.getDescribedService__SEFF().getEntityName())) {	
				
				// getActions of markovState
				LinkedHashMap<Transition, EList<Action>> actions = getActions(seff, behaviorModel, markovState);
				
				if (markovState
						.getOutgoingTransitions().size() > 1 && Constants.SET_GUARDS_ACTIONS) {
					
					// get guards of markovState 
					LinkedHashMap<Transition, EList<Guard>> guards = getGuards(seff, behaviorModel, markovState);					
							
					// get all combinations of guards except the combination all 
					HashMap<String, List<GuardedTransition>> guardCombinations = getCombinations(guards, actions);
					
					TreeMap<String, List<GuardedTransition>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER.reversed());
					result.putAll(guardCombinations);	    
								
					// for each combination create a new guarded transition 
					for (String combination : result.keySet()) {	
						List<GuardedTransition> guardedTransitions = guardCombinations.get(combination);
						GuardedBranchTransition guardedBranchTransition = createGuardedBranchTransition(markovState, behaviorModel, guardedTransitions, seff, combination);		
						guardedBranchTransition.setBranchAction_AbstractBranchTransition(branchAction);
					}						
					
				// in this case guards are not needed	
				} else {							
					for (Transition transition : markovState
							.getOutgoingTransitions()) {													
						// for each outgoing transition of the markovState, create a
						// new probabilisticBranchTransition
						ProbabilisticBranchTransition probabilisticBranchTransition = createProbabilisticBranchTransition(
								transition, seff, behaviorModel, 1.00, actions.get(transition));
						probabilisticBranchTransition
								.setBranchAction_AbstractBranchTransition(branchAction);							
					}						
				}				
			}
		}
		return branchAction;
	}	
	

	/**
	 * Get the strings for the guardedTransitions.
	 * 
	 * @param guardedTransitions
	 * @return String
	 */
	private String getGuardString(final List<GuardedTransition> guardedTransitionsList) {
		String guardString = "";	
		
		List<GuardedTransition> guardedTransitions = new ArrayList<GuardedTransition>();
					
		for (GuardedTransition guardedTransition : guardedTransitionsList) {
			if (!guardedTransition.guardIsTrue) {
				guardedTransitions.add(guardedTransition);
			}
		}	
		
		for (GuardedTransition guardedTransition : guardedTransitionsList) {
			if (guardedTransition.guardIsTrue) {
				guardedTransitions.add(guardedTransition);
			}
		}
		
			
		for (int i = 0; i < guardedTransitions.size(); i++) {
			if (!guardedTransitions.get(i).guardIsTrue) {
				guardString = guardString + " NOT ";
			}			
			guardString = guardString + " ( ";			
			for (int s = 0; s < guardedTransitions.get(i).getGuards().size() ; s++) {						
				if (guardedTransitions.get(i).getGuards().get(s).getGuardParameter().getParameterType() == GuardActionParameterType.BOOLEAN) {					
					if (guardedTransitions.get(i).getGuards().get(s).isNegate()) {
						guardString = guardString + guardedTransitions.get(i).getGuards().get(s).getGuardParameter().getGuardActionParameterName() + ".VALUE==true";
					} else {
						guardString = guardString + guardedTransitions.get(i).getGuards().get(s).getGuardParameter().getGuardActionParameterName() + ".VALUE==false";
					}				
				} else if (guardedTransitions.get(i).getGuards().get(s).getGuardParameter().getParameterType() == GuardActionParameterType.INTEGER) {					
					guardString = guardString + guardedTransitions.get(i).getGuards().get(s).getGuardParameter().getGuardActionParameterName() + ".VALUE >" + guardedTransitions.get(i).getGuards().get(s).getDiffMinimum();					
				}				
				if (s != guardedTransitions.get(i).getGuards().size() -1) {
					guardString = guardString + " AND ";
				}				
			}			
			guardString = guardString + " ) ";		
			if (i != guardedTransitions.size() -1) {
				guardString = guardString + " AND ";
			}
		}			
		return guardString;
	}
	
	/**
	 * @param guards
	 * @return HashMap<String, List<GuardedTransition>>
	 */
	private HashMap<String, List<GuardedTransition>> getCombinations(final LinkedHashMap<Transition, EList<Guard>> guards, final LinkedHashMap<Transition, EList<Action>> actions) {
		double size = (double) guards.size();
		int numberOfCombinations = (int) Math.pow(2, size);
		HashMap<String, List<GuardedTransition>> guardCombinations = new HashMap<String, List<GuardedTransition>>();
		for (int i = numberOfCombinations-1; i > 0; i--) {
			String binaryString = addMissingZeros(Integer.toBinaryString(i), (int) size);
			List<GuardedTransition> guardedTransitionList = new ArrayList<GuardedTransition>();
			int s = 0;
			for (Transition transition : guards.keySet()) {
				s++;
				GuardedTransition guardedTransition = new GuardedTransition(transition, guards.get(transition), actions.get(transition), transitionIsTrue(s, binaryString));
				guardedTransitionList.add(guardedTransition);
			}
			guardCombinations.put(binaryString, guardedTransitionList);
		}	
		return guardCombinations;
	}	
	
	/**
	 * @param binaryString
	 * @param size
	 * @return String
	 */
	private String addMissingZeros(final String binaryString, final int size) {
		String newBinaryString = binaryString;
		if (binaryString.length() < size) {
			int diff = size - binaryString.length();
			for (int i = 0; i < diff; i++) {
				newBinaryString = "0" + newBinaryString;
			}
		}
		return newBinaryString;
	}
	
	/**
	 * Returns if the transition is true at the position i of the binaryString. 
	 * 
	 * @param i
	 * @param binaryString
	 * @return boolean
	 */
	private boolean transitionIsTrue(final int i, final String binaryString) {
		if (i <= binaryString.length() && binaryString.substring(i-1, i).equals("1")) {
			return true;
		}		
		return false;
	}
	
	/**
	 * Get guards of the transitions of the markovState. 
	 * 
	 * @param seff
	 * @param behaviorModel
	 * @return HashMap<Transition, EList<Guard>> with the guards of each transitions
	 */
	private LinkedHashMap<Transition, EList<Guard>> getGuards(final ResourceDemandingSEFF seff, final BehaviorModel behaviorModel, final MarkovState markovState) {
		LinkedHashMap<Transition, EList<Guard>> guards = new LinkedHashMap<Transition, EList<Guard>>();		
		for (Transition transition : markovState
				.getOutgoingTransitions()) {					
			if (transition.getTargetState() instanceof MarkovState) {
				String sourceTransitionState = markovState.getService().toString();
				String targetTransitionState = ((MarkovState) transition.getTargetState()).getService().toString();					
				SessionLayerEFSM sessionLayerEFSM  = CreatorTools.getInstance().getThisWorkloadModel()
						.getApplicationModel().getSessionLayerEFSM();					
				for (ApplicationState applicationState : sessionLayerEFSM.getApplicationStates()) {
					if (applicationState.getService().toString().equals(sourceTransitionState)) {
						for (ApplicationTransition applicationTransition : applicationState.getOutgoingTransitions()) {
							if (applicationTransition.getTargetState() instanceof ApplicationState) {
								String targetApplicationState =  ((ApplicationState) applicationTransition.getTargetState()).getService().toString();
			    				if (targetTransitionState.equals(targetApplicationState)) {
			    					guards.put(transition, applicationTransition.getGuard());
			    				}
							}									
						}
					}
				}		
			}
		}
		return guards;
	}		
	
	/**
	 * Get actions of the transitions of the markovState. 
	 * 
	 * @param seff
	 * @param behaviorModel
	 * @return HashMap<Transition, EList<Guard>> with the guards of each transitions
	 */
	private LinkedHashMap<Transition, EList<Action>> getActions(final ResourceDemandingSEFF seff, final BehaviorModel behaviorModel, final MarkovState markovState) {
		LinkedHashMap<Transition, EList<Action>> actions = new LinkedHashMap<Transition, EList<Action>>();		
		for (Transition transition : markovState
				.getOutgoingTransitions()) {					
			if (transition.getTargetState() instanceof MarkovState) {
				String sourceTransitionState = markovState.getService().toString();
				String targetTransitionState = ((MarkovState) transition.getTargetState()).getService().toString();					
				SessionLayerEFSM sessionLayerEFSM  = CreatorTools.getInstance().getThisWorkloadModel()
						.getApplicationModel().getSessionLayerEFSM();					
				for (ApplicationState applicationState : sessionLayerEFSM.getApplicationStates()) {
					if (applicationState.getService().toString().equals(sourceTransitionState)) {
						for (ApplicationTransition applicationTransition : applicationState.getOutgoingTransitions()) {
							if (applicationTransition.getTargetState() instanceof ApplicationState) {
								String targetApplicationState =  ((ApplicationState) applicationTransition.getTargetState()).getService().toString();
			    				if (targetTransitionState.equals(targetApplicationState)) {
			    					actions.put(transition, applicationTransition.getAction());
			    				}
							}									
						}
					}
				}		
			}
		}
		return actions;
	}
	
	/**
	 * Create guarded branch transition. 
	 * 
	 * @param transition
	 * @param seff
	 * @param behaviorModel
	 * @return GuardedBranchTransition
	 * @throws IOException 
	 */
	private GuardedBranchTransition createGuardedBranchTransition(
			final MarkovState markovState, 
			final BehaviorModel behaviorModel,
			final List<GuardedTransition> guardedTransitions,
			final ResourceDemandingSEFF seff,
			final String guardName) throws IOException {
		GuardedBranchTransition guardedBranchTransition = SeffFactory.eINSTANCE.createGuardedBranchTransition();
		guardedBranchTransition.setBranchCondition_GuardedBranchTransition(createPCMRandomVariable(getGuardString(guardedTransitions)));	
		guardedBranchTransition.setEntityName(guardName);
		ResourceDemandingBehaviour resourceDemandingBehaviour = SeffFactory.eINSTANCE
				.createResourceDemandingBehaviour();
		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		startAction.setSuccessor_AbstractAction(stopAction);
		stopAction.setPredecessor_AbstractAction(startAction);
		guardedBranchTransition.setBranchBehaviour_BranchTransition(resourceDemandingBehaviour);		
		BranchAction branchAction = createProbabilisticBranchAction(markovState, guardedTransitions, behaviorModel, seff);
		resourceDemandingBehaviour.getSteps_Behaviour().add(branchAction);
		resourceDemandingBehaviour.getSteps_Behaviour().add(stopAction);
		resourceDemandingBehaviour.getSteps_Behaviour().add(startAction);
		startAction.setSuccessor_AbstractAction(branchAction);
		branchAction.setPredecessor_AbstractAction(startAction);
		branchAction.setSuccessor_AbstractAction(stopAction);
		stopAction.setPredecessor_AbstractAction(branchAction);
		return guardedBranchTransition;
	}
	
	/**
	 * @param markovState
	 * @param guardedTransitions
	 * @param behaviorModel
	 * @param seff
	 * @return BranchAction
	 * @throws IOException
	 */
	private BranchAction createProbabilisticBranchAction(final MarkovState markovState,
			final List<GuardedTransition> guardedTransitions,
			final BehaviorModel behaviorModel,
			final ResourceDemandingSEFF seff) throws IOException {
		BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
		
		double sumProbability = 0;
		for (GuardedTransition guardedTransition:guardedTransitions) {
			if (guardedTransition.guardIsTrue) {
				sumProbability += guardedTransition.getTransition().getProbability();
			}
		}
		
		for (GuardedTransition guardedTransition:guardedTransitions) {
			if (guardedTransition.guardIsTrue) {
				ProbabilisticBranchTransition probabilisticBranchTransition = 
						createProbabilisticBranchTransition(guardedTransition.getTransition(), seff, behaviorModel, 1.00/sumProbability, guardedTransition.getActions());
				probabilisticBranchTransition.setBranchAction_AbstractBranchTransition(branchAction);
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
	 * @param first
	 * @return ProbabilisticBranchTransition
	 * @throws IOException
	 */
	private ProbabilisticBranchTransition createProbabilisticBranchTransition(
			final Transition transition,
			final ResourceDemandingSEFF seff,
			final BehaviorModel behaviorModel,
			final double adjustfactor,
			final EList<Action> transitionActions) throws IOException {

		ProbabilisticBranchTransition probabilisticBranchTransition = SeffFactory.eINSTANCE
				.createProbabilisticBranchTransition();
		ResourceDemandingBehaviour resourceDemandingBehaviour = SeffFactory.eINSTANCE
				.createResourceDemandingBehaviour();

		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		resourceDemandingBehaviour.getSteps_Behaviour().add(startAction);
		resourceDemandingBehaviour.getSteps_Behaviour().add(stopAction);

		// add thinkTime of transition as internal action with delay
		InternalAction internalAction = createInternalAction(transition);
		resourceDemandingBehaviour.getSteps_Behaviour().add(internalAction);

		// add externalCall to the system
		ExternalCallAction externalCallAction = createExternalCallActionSystem(
				seff.getBasicComponent_ServiceEffectSpecification(), transition
						.getTargetState().getEId());

		if (externalCallAction != null) {
			resourceDemandingBehaviour.getSteps_Behaviour().add(
					externalCallAction);
		}

		// add next markov state, outgoingTransition
		ExternalCallAction targetState = createExternalCallActionNext(
				seff.getBasicComponent_ServiceEffectSpecification(), transition
						.getTargetState().getEId(), behaviorModel.getName(), transitionActions);

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
				.getProbability() * adjustfactor);
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
		
		// load ResourceRepository
		final ResourceRepository resourceRepository = creatorTools
				.getResourceRepository(resourceSet);
		PCMRandomVariable pcmRandomVariable = null;
		for (final ResourceType resourceType : resourceRepository
				.getAvailableResourceTypes_ResourceRepository()) {
			if (resourceType instanceof ProcessingResourceType) {
				final ProcessingResourceType processingResourceType = (ProcessingResourceType) resourceType;
				if (processingResourceType.getEntityName().equals(
						resourceDemandType)) {
					parametricResourceDemand
							.setRequiredResource_ParametricResourceDemand(processingResourceType);
					
					if (deviation == 0) { 
						pcmRandomVariable = createPCMRandomVariable(Double
						.toString(resourceDemand));
					} else {					
						pcmRandomVariable = createPCMRandomVariable("Norm ("
						 + resourceDemand + ", "  +   Math.sqrt(deviation)  +   " )");
					}
				}
			}
		}
		parametricResourceDemand
				.setSpecification_ParametericResourceDemand(pcmRandomVariable);
		return parametricResourceDemand;
	}
	
	/**
	 * @param transition
	 * @return InternalAction
	 * @throws IOException
	 */
	private InternalAction createInternalAction(final Transition transition)
			throws IOException {
		InternalAction internalAction = SeffFactory.eINSTANCE
				.createInternalAction();
		final NormallyDistributedThinkTime normallyDistributedThinkTime = (NormallyDistributedThinkTime) transition
				.getThinkTime();
		internalAction.getResourceDemand_Action().add(
				getParametricResourceDemand(thisResourceSet, "DELAY",
						normallyDistributedThinkTime.getMean(),
						normallyDistributedThinkTime.getDeviation()));
		return internalAction;
	}

	/**
	 * Create externalCallAction to the system.
	 * 
	 * @param bc
	 * @param operationName
	 * @return ExternalCallAction
	 * @throws IOException
	 */
	private ExternalCallAction createExternalCallActionSystem(
			final BasicComponent bc, final String operationName)
			throws IOException {
		ExternalCallAction externalAction = null;
		final EList<ProvidedRole> providedRoles = creatorTools.getThisSystem()
				.getProvidedRoles_InterfaceProvidingEntity();
		for (final ProvidedRole providedRole : providedRoles) {
			final OperationProvidedRole opr = (OperationProvidedRole) providedRole;
			final OperationInterface oi = opr
					.getProvidedInterface__OperationProvidedRole();
			final EList<OperationSignature> operationSignatures = oi
					.getSignatures__OperationInterface();
			int indexOfChar = operationName.indexOf("_", 1);
			String newOperationName = operationName.substring(indexOfChar + 1,
					operationName.length());
			for (OperationSignature operationSignature : operationSignatures) {
				if (operationSignature.getEntityName().startsWith(
						newOperationName)) {
					OperationRequiredRole opreq = createRequiredRoleBetweenComponents(
							bc.getEntityName(), oi.getEntityName(),
							thisRepository);
					externalAction = SeffFactory.eINSTANCE
							.createExternalCallAction();
					externalAction.setEntityName(operationName);
					externalAction
							.setCalledService_ExternalService(operationSignature);
					EList<Parameter> parameterList = operationSignature
							.getParameters__OperationSignature();
					VariableUsage variableUsage = createVariableUsage(
							parameterList.get(0).getParameterName(),
							"\"" + oi.getEntityName() + "_"
									+ operationSignature.getEntityName() + "\"");
					externalAction.getInputVariableUsages__CallAction().add(
							variableUsage);
					externalAction.setRole_ExternalService(opreq);
				}
			}
		}
		return externalAction;
	}

	/**
	 * Create externalCallAction to the next behaviorStep.
	 * 
	 * @param bc
	 * @param operationName
	 * @param componentName
	 * @return ExternalCallAction
	 */
	private ExternalCallAction createExternalCallActionNext(
			final BasicComponent bc,
			final String operationName,
			final String componentName,
			final EList<Action> transitionActions) {
		ExternalCallAction externalCallAction = null;
		EList<Interface> interfaceList = thisRepository
				.getInterfaces__Repository();
		for (Interface interfaceInstance : interfaceList) {
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
						externalCallAction = SeffFactory.eINSTANCE
								.createExternalCallAction();
						externalCallAction.setEntityName(operationName);
						externalCallAction
								.setCalledService_ExternalService(operationSignature);
						externalCallAction.setRole_ExternalService(opreq);
						setActions(externalCallAction, transitionActions);					
					}
				}
			}
		}
		return externalCallAction;
	}
	
	/**
	 * Create inputvariables in the seffs. If a action is defined in transitionActions use this one, otherwise take old value. 
	 * 
	 * @param externalCallAction
	 * @param transitionActions
	 */
	private void setActions(final ExternalCallAction externalCallAction, final EList<Action> transitionActions) {
		for (GuardActionParameter guardActionParameter : CreatorTools.getInstance().getThisWorkloadModel().getApplicationModel().getSessionLayerEFSM().
				getGuardActionParameterList().getGuardActionParameters()) {		
			Action action = guardActionParameterInActionList(transitionActions, guardActionParameter);				
			if (action != null) {
				if (action.getActionParameter().getParameterType() == GuardActionParameterType.BOOLEAN) {
					externalCallAction.getInputVariableUsages__CallAction().add(
							createVariableUsage(action.getActionParameter().getGuardActionParameterName(), 
									"true"));							
				} else if (action.getActionParameter().getParameterType() == GuardActionParameterType.INTEGER) {
					if (externalCallAction.getCalledService_ExternalService().getEntityName().equals(action.getActionParameter().getTargetName())) {
						externalCallAction.getInputVariableUsages__CallAction().add(
								createVariableUsage(action.getActionParameter().getGuardActionParameterName(), 
										action.getActionParameter().getGuardActionParameterName() + ".VALUE - 1"));	
					} else if (externalCallAction.getCalledService_ExternalService().getEntityName().equals(action.getActionParameter().getSourceName())) {
						externalCallAction.getInputVariableUsages__CallAction().add(
								createVariableUsage(action.getActionParameter().getGuardActionParameterName(), 
										action.getActionParameter().getGuardActionParameterName() + ".VALUE + 1"));	
					}		
				}	
			} else {				
				if (transitionActions == null) {
					if (guardActionParameter.getSourceName().equals(externalCallAction.getCalledService_ExternalService().getEntityName())) {
						if (guardActionParameter.getParameterType() == GuardActionParameterType.BOOLEAN) {
							externalCallAction.getInputVariableUsages__CallAction().add(
									createVariableUsage(guardActionParameter.getGuardActionParameterName(), 
											"true"));							
						} else if (guardActionParameter.getParameterType() == GuardActionParameterType.INTEGER) {
							if (externalCallAction.getCalledService_ExternalService().getEntityName().equals(guardActionParameter.getTargetName())) {
								externalCallAction.getInputVariableUsages__CallAction().add(
										createVariableUsage(guardActionParameter.getGuardActionParameterName(), 
												guardActionParameter.getGuardActionParameterName() + ".VALUE - 1"));	
							} else 	if (externalCallAction.getCalledService_ExternalService().getEntityName().equals(guardActionParameter.getSourceName())) {
								externalCallAction.getInputVariableUsages__CallAction().add(
										createVariableUsage(guardActionParameter.getGuardActionParameterName(), 
												guardActionParameter.getGuardActionParameterName() + ".VALUE + 1"));	
							} 
						}	
					} else {
						if (guardActionParameter.getParameterType() == GuardActionParameterType.INTEGER) {
							externalCallAction.getInputVariableUsages__CallAction().add(
									createVariableUsage(guardActionParameter.getGuardActionParameterName(), guardActionParameter.getGuardActionParameterName() + ".VALUE"));
						} else if (guardActionParameter.getParameterType() == GuardActionParameterType.BOOLEAN) {
							externalCallAction.getInputVariableUsages__CallAction().add(
									createVariableUsage(guardActionParameter.getGuardActionParameterName(), guardActionParameter.getGuardActionParameterName() + ".VALUE"));					
						}
					}	
				} else {
					if (guardActionParameter.getParameterType() == GuardActionParameterType.INTEGER) {
						externalCallAction.getInputVariableUsages__CallAction().add(
								createVariableUsage(guardActionParameter.getGuardActionParameterName(), guardActionParameter.getGuardActionParameterName() + ".VALUE"));
					} else if (guardActionParameter.getParameterType() == GuardActionParameterType.BOOLEAN) {
						externalCallAction.getInputVariableUsages__CallAction().add(
								createVariableUsage(guardActionParameter.getGuardActionParameterName(), guardActionParameter.getGuardActionParameterName() + ".VALUE"));					
					}
				}
			}
		}				
	}

	/**
	 * @param transitionActions
	 * @param guardActionParameter
	 * @return boolean
	 */
	private Action guardActionParameterInActionList(final EList<Action> transitionActions,
			final GuardActionParameter guardActionParameter) {
		
		if (transitionActions == null) {
			return null;
		}
		
		for (Action action : transitionActions) {
			if (action.getActionParameter().equals(guardActionParameter)) {
				return action;
			}
		}
		return null;
	}
	
	/**
	 * Set required role to all required interfaces. When toComponent is set to
	 * null then the required role will be set to all existing interfaces.
	 * 
	 * @param fromComponent
	 * @param toInterface
	 * @param repository
	 * @return OperationRequiredRole
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
	
	/**
	 * @author voegele
	 *
	 */
	class GuardedTransition {
		private Transition transition;
		private EList<Guard> guards;
		private EList<Action> actions;
		private boolean guardIsTrue;

		public GuardedTransition (final Transition transition, 
				final EList<Guard> guards,
				final EList<Action> actions,
				final boolean guardIsTrue) {
			this.transition = transition;
			this.guards = guards;
			this.actions = actions;
			this.guardIsTrue = guardIsTrue;
		}		
		
		/**
		 * @return the actions
		 */
		public final EList<Action> getActions() {
			return actions;
		}

		/**
		 * @param actions the actions to set
		 */
		public final void setActions(EList<Action> actions) {
			this.actions = actions;
		}
		
		/**
		 * @return the transition
		 */
		public final Transition getTransition() {
			return transition;
		}
		/**
		 * @param transition the transition to set
		 */
		public final void setTransition(Transition transition) {
			this.transition = transition;
		}
		/**
		 * @return the guards
		 */
		public final EList<Guard> getGuards() {
			return guards;
		}
		/**
		 * @param guards the guards to set
		 */
		public final void setGuards(EList<Guard> guards) {
			this.guards = guards;
		}
		/**
		 * @return the guardIsTrue
		 */
		public final boolean isGuardIsTrue() {
			return guardIsTrue;
		}
		/**
		 * @param guardIsTrue the guardIsTrue to set
		 */
		public final void setGuardIsTrue(boolean guardIsTrue) {
			this.guardIsTrue = guardIsTrue;
		}	
		
	}

}
