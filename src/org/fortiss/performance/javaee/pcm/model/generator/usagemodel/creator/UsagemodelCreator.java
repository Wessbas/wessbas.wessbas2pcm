package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;

import m4jdsl.BehaviorMix;
import m4jdsl.BehaviorModel;
import m4jdsl.RelativeFrequency;
import m4jdsl.WorkloadModel;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.core.CoreFactory;
import de.uka.ipd.sdq.pcm.core.PCMRandomVariable;
import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.ProvidedRole;
import de.uka.ipd.sdq.pcm.system.System;
import de.uka.ipd.sdq.pcm.usagemodel.Branch;
import de.uka.ipd.sdq.pcm.usagemodel.BranchTransition;
import de.uka.ipd.sdq.pcm.usagemodel.ClosedWorkload;
import de.uka.ipd.sdq.pcm.usagemodel.EntryLevelSystemCall;
import de.uka.ipd.sdq.pcm.usagemodel.ScenarioBehaviour;
import de.uka.ipd.sdq.pcm.usagemodel.Start;
import de.uka.ipd.sdq.pcm.usagemodel.Stop;
import de.uka.ipd.sdq.pcm.usagemodel.UsageModel;
import de.uka.ipd.sdq.pcm.usagemodel.UsageScenario;
import de.uka.ipd.sdq.pcm.usagemodel.UsagemodelFactory;

/**
 * This class creates a new Usage Model based on the input workload model.
 * 
 * @author voegele
 * 
 */
public class UsagemodelCreator extends CreatorTools {

	private ResourceSet thisResourceSet;

	/**
	 * @param resourceSet
	 * @param workloadModel
	 * @throws IOException
	 */
	public final void createUsageModel(final ResourceSet resourceSet,
			WorkloadModel workloadModel) throws IOException {

		log.info("- CREATE USAGE MODEL");

		thisResourceSet = resourceSet;

		// create usage model
		Resource usageResource = thisResourceSet.createResource(URI
				.createFileURI(Configuration.getUsageModelFile()
						.getAbsolutePath()));

		// create UsageModel
		UsageModel usageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
		usageResource.getContents().add(usageModel);

		// create new UsageScenario
		final UsageScenario usageScenario = UsagemodelFactory.eINSTANCE
				.createUsageScenario();
		usageScenario.setEntityName("GeneratedUsageScenario");

		// create ScenarioBehaviour and add to usageScenario
		final ScenarioBehaviour scenarioBehaviour = UsagemodelFactory.eINSTANCE
				.createScenarioBehaviour();
		scenarioBehaviour.setEntityName("DefaultScenario");
		createScenarioBehavior(scenarioBehaviour, workloadModel);
		usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);
		ClosedWorkload newWorkload = UsagemodelFactory.eINSTANCE
				.createClosedWorkload();
		// TODO: Formula of workloadModel must be integrated
		newWorkload.setPopulation(Integer.parseInt(workloadModel
				.getWorkloadIntensity().getFormula()));
		PCMRandomVariable pcmRandomVariableThinkTime = CoreFactory.eINSTANCE
				.createPCMRandomVariable();
		pcmRandomVariableThinkTime.setSpecification("0");
		newWorkload.setThinkTime_ClosedWorkload(pcmRandomVariableThinkTime);
		usageScenario.setWorkload_UsageScenario(newWorkload);
		usageModel.getUsageScenario_UsageModel().add(usageScenario);
		// save usageResource
		usageResource.save(null);
	}

	/**
	 * Create new ScenarioBehavior.
	 * 
	 * @param scenarioBehaviour
	 * @param workloadModel
	 * @throws IOException
	 */
	private final void createScenarioBehavior(
			ScenarioBehaviour scenarioBehaviour, WorkloadModel workloadModel)
			throws IOException {
		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		Branch branch = createBranch(workloadModel);

		// add elements to scenarioBehavior
		start.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		stop.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		branch.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);

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
	 * @param workloadModel
	 * @return Branch
	 * @throws IOException
	 */
	private Branch createBranch(WorkloadModel workloadModel) throws IOException {
		Branch branch = UsagemodelFactory.eINSTANCE.createBranch();
		branch.setEntityName("BehaviorMix");

		EList<BehaviorModel> behaviorModelsList = workloadModel
				.getBehaviorModels();
		BehaviorMix behaviorMix = workloadModel.getBehaviorMix();
		EList<RelativeFrequency> relativeFrequencyList = behaviorMix
				.getRelativeFrequencies();
		for (int i = 0; i < relativeFrequencyList.size(); i++) {
			createBranchTransition(branch, relativeFrequencyList.get(i)
					.getValue(), behaviorModelsList.get(i));
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
			BehaviorModel behaviorModel) throws IOException {

		// load System
		final System system = getSystem(Configuration.getSystemFile(),
				thisResourceSet);

		// create new EntryLevelSystemCall
		EntryLevelSystemCall entryLevelSystemCall = UsagemodelFactory.eINSTANCE
				.createEntryLevelSystemCall();

		// get available Operations and call each workflow operation once
		final EList<ProvidedRole> providedRoles = system
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

					if (behaviorModel.getInitialState().getService().getName()
							.equals(operationSignature.getEntityName())) {
						entryLevelSystemCall = UsagemodelFactory.eINSTANCE
								.createEntryLevelSystemCall();
						entryLevelSystemCall.setEntityName(behaviorModel
								.getInitialState().getService().getName());
						entryLevelSystemCall
								.setOperationSignature__EntryLevelSystemCall(operationSignature);
						entryLevelSystemCall
								.setProvidedRole_EntryLevelSystemCall(opr);
						break;
					}

				}
			}
		}
		return entryLevelSystemCall;
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
		entryLevelSystemCall.setScenarioBehaviour_AbstractUserAction(sb);

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
