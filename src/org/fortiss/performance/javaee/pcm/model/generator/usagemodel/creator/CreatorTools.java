package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

import m4jdsl.WorkloadModel;
import m4jdsl.impl.M4jdslPackageImpl;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.wessbassdsl.XmiEcoreHandler;

import de.uka.ipd.sdq.pcm.allocation.Allocation;
import de.uka.ipd.sdq.pcm.repository.Repository;
import de.uka.ipd.sdq.pcm.resourceenvironment.ResourceEnvironment;
import de.uka.ipd.sdq.pcm.resourcetype.ResourceRepository;
import de.uka.ipd.sdq.pcm.system.System;
import de.uka.ipd.sdq.pcm.usagemodel.UsageModel;
import de.uka.ipd.sdq.pcm.usagemodel.UsagemodelFactory;

/**
 * @author voegele
 * 
 */
public class CreatorTools {

	// singleton
	private static CreatorTools instance = null;

	private CreatorTools() {
	}

	public static CreatorTools getInstance() {
		if (instance == null) {
			instance = new CreatorTools();
		}
		return instance;
	}

	/**
	 * Logger used in this project.
	 */
	public final Logger log = Logger.getAnonymousLogger();

	private Repository thisRepository;
	private Resource repositoryResource;
	private EObject rootRepository;

	private System thisSystem;
	private Resource systemResource;
	private EObject rootSystem;

	private Allocation thisAllocation;
	private Resource allocationResource;
	private EObject rootAllocation;

	private ResourceEnvironment thisResourceEnvironment;
	private Resource resourceEnvironmentResource;
	private EObject rootEnvironment;

	private UsageModel thisUsageModel;
	private Resource usageResource;

	private ResourceSet thisResourceSet;
	private WorkloadModel thisWorkloadModel;

	/**
	 * @return the thisWorkloadModel
	 */
	protected WorkloadModel getThisWorkloadModel() {
		if (thisWorkloadModel == null) {
			try {
				// initialize the model package;
				M4jdslPackageImpl.init();

				// Read Workload Model from XMI File
				// might throw an IOException;
				thisWorkloadModel = (WorkloadModel) XmiEcoreHandler
						.getInstance().xmiToEcore(Constants.XMI_FILE, "xmi");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return thisWorkloadModel;
	}

	/**
	 * @return the thisRepository
	 * @throws IOException
	 */
	protected final Repository getThisRepository() throws IOException {
		if (thisRepository == null) {
			repositoryResource = getResourceSet().getResource(
					URI.createFileURI(Configuration.getRepositoryFile()
							.getAbsolutePath()), true);
			repositoryResource.load(Collections.EMPTY_MAP);
			rootRepository = repositoryResource.getContents().get(0);
			thisRepository = (Repository) rootRepository;
		}
		return thisRepository;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveRepository() throws IOException {
		this.repositoryResource.save(null);
	}

	/**
	 * @return the thisSystem
	 * @throws IOException
	 */
	protected final System getThisSystem() throws IOException {
		if (thisSystem == null) {
			systemResource = getResourceSet().getResource(
					URI.createFileURI(Configuration.getSystemFile()
							.getAbsolutePath()), true);
			systemResource.load(Collections.EMPTY_MAP);
			rootSystem = systemResource.getContents().get(0);
			thisSystem = (System) rootSystem;
		}

		return thisSystem;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveSystem() throws IOException {
		this.systemResource.save(null);
	}

	/**
	 * @return the thisAllocation
	 * @throws IOException
	 */
	protected final Allocation getThisAllocation() throws IOException {
		if (thisAllocation == null) {
			// load Allocation
			allocationResource = getResourceSet().getResource(
					URI.createFileURI(Configuration.getAllocationFile()
							.getAbsolutePath()), true);
			allocationResource.load(Collections.EMPTY_MAP);
			rootAllocation = allocationResource.getContents().get(0);
			thisAllocation = (Allocation) rootAllocation;
		}
		return thisAllocation;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveAllocation() throws IOException {
		this.allocationResource.save(null);
	}

	/**
	 * @return the thisResourceEnvironment
	 * @throws IOException
	 */
	protected final ResourceEnvironment getThisResourceEnvironment()
			throws IOException {
		if (thisResourceEnvironment == null) {
			// load ResourceEnvironment
			resourceEnvironmentResource = getResourceSet().getResource(
					URI.createFileURI(Configuration
							.getResourceenvironmentFile().getAbsolutePath()),
					true);
			resourceEnvironmentResource.load(Collections.EMPTY_MAP);
			rootEnvironment = resourceEnvironmentResource.getContents().get(0);
			thisResourceEnvironment = (ResourceEnvironment) rootEnvironment;
		}
		return thisResourceEnvironment;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveResourceEnvironment() throws IOException {
		this.usageResource.save(null);
	}

	/**
	 * @return the thisUsageModel
	 */
	protected final UsageModel getThisUsageModel() {
		if (thisUsageModel == null) {
			// create usage model
			usageResource = getResourceSet().createResource(
					URI.createFileURI(Configuration.getUsageModelFile()
							.getAbsolutePath()));

			// create UsageModel
			thisUsageModel = UsagemodelFactory.eINSTANCE.createUsageModel();
			usageResource.getContents().add(thisUsageModel);

		}
		return thisUsageModel;
	}

	/**
	 * @throws IOException
	 */
	protected final void saveUsageModel() throws IOException {
		this.usageResource.save(null);
	}

	/**
	 * GetResourceSet
	 * 
	 * @return
	 */
	protected ResourceSet getResourceSet() {
		if (thisResourceSet == null) {
			thisResourceSet = new ResourceSetImpl();
		}
		return thisResourceSet;
	}

	/**
	 * @param resourceSet
	 *            resourceSet
	 * @return ResourceRepository
	 * @throws IOException
	 *             IOException
	 */
	protected final ResourceRepository getResourceRepository(
			final ResourceSet resourceSet) throws IOException {
		Resource resourceTypeResource = resourceSet.getResource(
				URI.createURI("pathmap://PCM_MODELS/Palladio.resourcetype "),
				true);
		resourceTypeResource.load(Collections.EMPTY_MAP);
		EObject rootTypeResource = resourceTypeResource.getContents().get(0);
		ResourceRepository resourceRepository = (ResourceRepository) rootTypeResource;
		return resourceRepository;
	}

	/**
	 * @param file
	 */
	protected final void deleteFiles(File file) {
		File[] finlist = file.listFiles();
		for (int n = 0; n < finlist.length; n++) {
			if (finlist[n].isFile()
					&& !finlist[n].getName().substring(0, 1).equals(".")) {
				java.lang.System.out.println("Delete File: "
						+ finlist[n].getName());
				java.lang.System.gc();
				finlist[n].delete();
			}
		}
	}

	/**
	 * @param componentName
	 * @return String
	 */
	protected final String getAssemblyName(String componentName) {
		return "Assembly_" + componentName + " <" + componentName + ">";
	}

	/**
	 * returns the levenshtein distance of two strings<br>
	 * Der Levenshtein-Algorithmus (auch Edit-Distanz genannt) errechnet die
	 * Mindestanzahl von Editierungsoperationen, die notwendig sind, um eine
	 * bestimmte Zeichenkette soweit abzuädern, um eine andere bestimmte
	 * Zeichenkette zu erhalten.<br>
	 * Die wohl bekannteste Weise die Edit-Distanz zu berechnen erfolgt durch
	 * den sogenannten Dynamic-Programming-Ansatz. Dabei wird eine Matrix
	 * initialisiert, die für jede (m, N)-Zelle die Levenshtein-Distanz
	 * (levenshtein distance) zwischen dem m-Buchstabenpräfix des einen Wortes
	 * und des n-Präfix des anderen Wortes enthält.<br>
	 * Die Tabelle kann z.B. von der oberen linken Ecke zur untereren rechten
	 * Ecke gefüllt werden. Jeder Sprung horizontal oder vertikal entspricht
	 * einer Editieroperation (Einfügen bzw. Löschen eines Zeichens) und
	 * "kostet" einen bestimmte virtuellen Betrag.<br>
	 * Die Kosten werden normalerweise auf 1 für jede der Editieroperationen
	 * eingestellt. Der diagonale Sprung kostet 1, wenn die zwei Buchstaben in
	 * die Reihe und Spalte nicht bereinstimmen, oder im Falle einer
	 * Übereinstimmung 0.<br>
	 * Jede Zelle minimiert jeweils die lokalen Kosten. Daher entspricht die
	 * Zahl in der untereren rechten Ecke dem Levenshtein-Abstand zwischen den
	 * beiden Wörtern.
	 * 
	 * @param s
	 * @param t
	 * @return the levenshtein dinstance
	 */
	public int levenshteinDistance(String s, String t) {
		final int sLen = s.length(), tLen = t.length();

		if (sLen == 0)
			return tLen;
		if (tLen == 0)
			return sLen;

		int[] costsPrev = new int[sLen + 1]; // previous cost array, horiz.
		int[] costs = new int[sLen + 1]; // cost array, horizontally
		int[] tmpArr; // helper to swap arrays
		int sIndex, tIndex; // current s and t index
		int cost; // current cost value
		char tIndexChar; // char of t at tIndexth pos.

		for (sIndex = 0; sIndex <= sLen; sIndex++)
			costsPrev[sIndex] = sIndex;

		for (tIndex = 1; tIndex <= tLen; tIndex++) {
			tIndexChar = t.charAt(tIndex - 1);
			costs[0] = tIndex;

			for (sIndex = 1; sIndex <= sLen; sIndex++) {
				cost = (s.charAt(sIndex - 1) == tIndexChar) ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, to the
				// diagonally left and to the up +cost
				costs[sIndex] = Math.min(
						Math.min(costs[sIndex - 1] + 1, costsPrev[sIndex] + 1),
						costsPrev[sIndex - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			tmpArr = costsPrev;
			costsPrev = costs;
			costs = tmpArr;
		}

		// we just switched costArr and prevCostArr, so prevCostArr now actually
		// has the most recent cost counts
		return costsPrev[sLen];
	}

}
