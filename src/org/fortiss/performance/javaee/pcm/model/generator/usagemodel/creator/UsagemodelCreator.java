package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;

import m4jdsl.BehaviorMix;
import m4jdsl.BehaviorModel;
import m4jdsl.GuardActionParameter;
import m4jdsl.GuardActionParameterType;
import m4jdsl.RelativeFrequency;
import org.eclipse.emf.common.util.EList;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import de.uka.ipd.sdq.pcm.core.CoreFactory;
import de.uka.ipd.sdq.pcm.core.PCMRandomVariable;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.ProvidedRole;
import de.uka.ipd.sdq.pcm.usagemodel.Branch;
import de.uka.ipd.sdq.pcm.usagemodel.BranchTransition;
import de.uka.ipd.sdq.pcm.usagemodel.ClosedWorkload;
import de.uka.ipd.sdq.pcm.usagemodel.EntryLevelSystemCall;
import de.uka.ipd.sdq.pcm.usagemodel.ScenarioBehaviour;
import de.uka.ipd.sdq.pcm.usagemodel.Start;
import de.uka.ipd.sdq.pcm.usagemodel.Stop;
import de.uka.ipd.sdq.pcm.usagemodel.UsageScenario;
import de.uka.ipd.sdq.pcm.usagemodel.UsagemodelFactory;

/**
 * This class creates a new Usage Model based on the input workload model.
 * 
 * @author voegele
 * 
 */
public class UsagemodelCreator extends AbstractCreator {

	private CreatorTools creatorTools = CreatorTools.getInstance();

	/**
	 * @throws IOException
	 */
	public final void createUsageModel()
			throws IOException {

		creatorTools.log.info("- CREATE USAGE MODEL");

		// create new UsageScenario
		final UsageScenario usageScenario = UsagemodelFactory.eINSTANCE
				.createUsageScenario();
		usageScenario.setEntityName("GeneratedUsageScenario");

		// create ScenarioBehaviour and add to usageScenario
		final ScenarioBehaviour scenarioBehaviour = UsagemodelFactory.eINSTANCE
				.createScenarioBehaviour();
		scenarioBehaviour.setEntityName("DefaultScenario");
		createScenarioBehavior(scenarioBehaviour);
		usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);
		ClosedWorkload newWorkload = UsagemodelFactory.eINSTANCE
				.createClosedWorkload();
		// TODO: Formula of workloadModel must be integrated
		newWorkload.setPopulation(Integer.parseInt(creatorTools.getThisWorkloadModel()
				.getWorkloadIntensity().getFormula()));
		PCMRandomVariable pcmRandomVariableThinkTime = CoreFactory.eINSTANCE
				.createPCMRandomVariable();
		pcmRandomVariableThinkTime.setSpecification("0");
		newWorkload.setThinkTime_ClosedWorkload(pcmRandomVariableThinkTime);
		usageScenario.setWorkload_UsageScenario(newWorkload);
		creatorTools.getThisUsageModel().getUsageScenario_UsageModel()
				.add(usageScenario);
		// save usageResource
		creatorTools.saveUsageModel();
	}

	/**
	 * Create new ScenarioBehavior.
	 * 
	 * @param scenarioBehaviour
	 * @throws IOException
	 */
	private final void createScenarioBehavior(
			final ScenarioBehaviour scenarioBehaviour)
			throws IOException {
		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		Branch branch = createBranch();

		scenarioBehaviour.getActions_ScenarioBehaviour().add(start);
		scenarioBehaviour.getActions_ScenarioBehaviour().add(stop);
		scenarioBehaviour.getActions_ScenarioBehaviour().add(branch);

		// connect Elements
		start.setSuccessor(branch);
		branch.setPredecessor(start);
		branch.setSuccessor(stop);
		stop.setPredecessor(branch);
	}

	/**
	 * Create new branch. Each Transition within this branch is a separate
	 * behaviorModel.
	 * 
	 * @return Branch
	 * @throws IOException
	 */
	private Branch createBranch() throws IOException {
		Branch branch = UsagemodelFactory.eINSTANCE.createBranch();
		branch.setEntityName("BehaviorMix");

		BehaviorMix behaviorMix = creatorTools.getThisWorkloadModel().getBehaviorMix();
		EList<RelativeFrequency> relativeFrequencyList = behaviorMix
				.getRelativeFrequencies();
		
		for (int i = 0; i < relativeFrequencyList.size(); i++) {
			createBranchTransition(branch, relativeFrequencyList.get(i)
					.getValue(), relativeFrequencyList.get(i).getBehaviorModel());
		}
		return branch;
	}

	/**
	 * Create a EntryLevelSystemCall to the initial state of the behaviorModel
	 * component in the repository model.
	 * 
	 * @param behaviorModel
	 * @return EntryLevelSystemCall
	 * @throws IOException
	 */
	private EntryLevelSystemCall createEntryLevelSystemCall(
			final BehaviorModel behaviorModel) throws IOException {

		// create new EntryLevelSystemCall
		EntryLevelSystemCall entryLevelSystemCall = UsagemodelFactory.eINSTANCE
				.createEntryLevelSystemCall();

		// get available Operations and call each workflow operation once
		final EList<ProvidedRole> providedRoles = creatorTools.getThisSystem()
				.getProvidedRoles_InterfaceProvidingEntity();
		for (final ProvidedRole providedRole : providedRoles) {
			final OperationProvidedRole opr = (OperationProvidedRole) providedRole;
			if (opr.getEntityName().equals(
					"Provided_" + behaviorModel.getName())) {

				final OperationInterface oi = opr
						.getProvidedInterface__OperationProvidedRole();
				final EList<OperationSignature> operationSignatures = oi
						.getSignatures__OperationInterface();

				for (OperationSignature operationSignature : operationSignatures) {
					if (operationSignature.getEntityName().equals(Constants.INITIAL_NAME)) {
						entryLevelSystemCall = UsagemodelFactory.eINSTANCE
								.createEntryLevelSystemCall();
						entryLevelSystemCall.setEntityName(behaviorModel
								.getInitialState().getService().getName());
						entryLevelSystemCall
								.setOperationSignature__EntryLevelSystemCall(operationSignature);
						entryLevelSystemCall
								.setProvidedRole_EntryLevelSystemCall(opr);
						setGuardActionParameter(entryLevelSystemCall);											
						break;
					}

				}
			}
		}
		return entryLevelSystemCall;
	}
	
	/**
	 * @param entryLevelSystemCall
	 */
	private void setGuardActionParameter(final EntryLevelSystemCall entryLevelSystemCall) {
		for (GuardActionParameter guardActionParameter : CreatorTools.getInstance().getThisWorkloadModel().getApplicationModel().getSessionLayerEFSM().
				getGuardActionParameterList().getGuardActionParameters()) {
			if (guardActionParameter.getParameterType() == GuardActionParameterType.INTEGER) {
				entryLevelSystemCall
		        .getInputParameterUsages_EntryLevelSystemCall().add(createVariableUsage(guardActionParameter.getGuardActionParameterName(), "0"));
			} else if (guardActionParameter.getParameterType() == GuardActionParameterType.BOOLEAN) {
				entryLevelSystemCall
		        .getInputParameterUsages_EntryLevelSystemCall().add(createVariableUsage(guardActionParameter.getGuardActionParameterName(), "false"));					
			}
		}	
	}

	/**
	 * Create a new transition for each behaviorModel.
	 * 
	 * @param branch
	 * @param probability
	 * @param behaviorModel
	 * @throws IOException
	 */
	private void createBranchTransition(final Branch branch,
			final double probability, BehaviorModel behaviorModel)
			throws IOException {

		BranchTransition branchTransition = UsagemodelFactory.eINSTANCE
				.createBranchTransition();

		ScenarioBehaviour sb = UsagemodelFactory.eINSTANCE
				.createScenarioBehaviour();

		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();

		// add to scenario
		sb.getActions_ScenarioBehaviour().add(start);
		sb.getActions_ScenarioBehaviour().add(stop);

		EntryLevelSystemCall entryLevelSystemCall = createEntryLevelSystemCall(behaviorModel);
		sb.getActions_ScenarioBehaviour().add(entryLevelSystemCall);

		// connect element
		start.setSuccessor(entryLevelSystemCall);
		entryLevelSystemCall.setPredecessor(start);
		entryLevelSystemCall.setSuccessor(stop);
		stop.setPredecessor(entryLevelSystemCall);

		// add transition and scenario to branch
		branchTransition.setBranchedBehaviour_BranchTransition(sb);
		branchTransition.setBranch_BranchTransition(branch);
		branchTransition.setBranchProbability(probability);
	}
}
