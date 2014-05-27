package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.IOException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.Transition;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.util.CreatorTools;

import de.uka.ipd.sdq.pcm.repository.OperationInterface;
import de.uka.ipd.sdq.pcm.repository.OperationProvidedRole;
import de.uka.ipd.sdq.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.pcm.repository.ProvidedRole;
import de.uka.ipd.sdq.pcm.system.System;
import de.uka.ipd.sdq.pcm.usagemodel.EntryLevelSystemCall;
import de.uka.ipd.sdq.pcm.usagemodel.ScenarioBehaviour;
import de.uka.ipd.sdq.pcm.usagemodel.Start;
import de.uka.ipd.sdq.pcm.usagemodel.Stop;
import de.uka.ipd.sdq.pcm.usagemodel.UsageModel;
import de.uka.ipd.sdq.pcm.usagemodel.UsageScenario;
import de.uka.ipd.sdq.pcm.usagemodel.UsagemodelFactory;
import de.uka.ipd.sdq.pcm.usagemodel.Workload;

public class UsagemodelCreator extends CreatorTools {

	private ResourceSet thisResourceSet;
	private Transition[][] thisTransitionMatrix;

	/**
	 * @param resourceSet
	 * @param transitionMatrix
	 * @throws IOException
	 */
	public final void createUsageModel(final ResourceSet resourceSet,
			Transition[][] transitionMatrix) throws IOException {

		log.info("- CREATE USAGE MODEL");

		thisResourceSet = resourceSet;
		thisTransitionMatrix = transitionMatrix;

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
		createScenarioBehavior(scenarioBehaviour);
		usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);
		Workload newWorkload = UsagemodelFactory.eINSTANCE
				.createClosedWorkload();
		usageScenario.setWorkload_UsageScenario(newWorkload);
		usageModel.getUsageScenario_UsageModel().add(usageScenario);
		// save usageResource
		usageResource.save(null);
	}

	private final void createScenarioBehavior(
			ScenarioBehaviour scenarioBehaviour) throws IOException {
		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		EntryLevelSystemCall entryLevelSystemCall = getEntryLevelSystemCall();

		// add elements to scenarioBehavior
		start.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		stop.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		entryLevelSystemCall
				.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);

		// connect Element
		start.setSuccessor(entryLevelSystemCall);
		entryLevelSystemCall.setPredecessor(start);
		entryLevelSystemCall.setSuccessor(stop);
		stop.setPredecessor(entryLevelSystemCall);
	}

	private EntryLevelSystemCall getEntryLevelSystemCall() throws IOException {

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
					"Provided_" + Configuration.WORKFLOWCOMPONENT)) {
				final OperationInterface oi = opr
						.getProvidedInterface__OperationProvidedRole();
				final EList<OperationSignature> operationSignatures = oi
						.getSignatures__OperationInterface();
				for (OperationSignature operationSignature : operationSignatures) {
					if (operationSignature.getEntityName().equals(
							thisTransitionMatrix[0][0].getFromRequestURL())) {
						entryLevelSystemCall = UsagemodelFactory.eINSTANCE
								.createEntryLevelSystemCall();
						entryLevelSystemCall
								.setEntityName(thisTransitionMatrix[0][0]
										.getFromRequestURL());
						entryLevelSystemCall
								.setOperationSignature__EntryLevelSystemCall(operationSignature);
						entryLevelSystemCall
								.setProvidedRole_EntryLevelSystemCall(opr);
					}
					break;
				}
			}
		}
		return entryLevelSystemCall;
	}
}
