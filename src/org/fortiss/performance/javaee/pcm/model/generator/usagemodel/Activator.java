package org.fortiss.performance.javaee.pcm.model.generator.usagemodel;

import java.io.File;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.analyser.AnalyzeHttpRequests;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.AllocationCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.RepositoryCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.SystemCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.UsagemodelCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.files.ReadHttpRequestLogFiles;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.fortiss.performance.javaee.pcm.model.generator.usagemodel"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		// new resourceSet
		final ResourceSet resourceSet = new ResourceSetImpl();

		// Analyse HttpRequests
		ReadHttpRequestLogFiles readHttpRequestLogFiles = new ReadHttpRequestLogFiles();
		AnalyzeHttpRequests analyzeHttpRequests = new AnalyzeHttpRequests();
		analyzeHttpRequests.analyze(readHttpRequestLogFiles
				.getHttpRequests(new File(Configuration.DIRECTORY)));

		// create new component in repository
		RepositoryCreator repositoryCreator = new RepositoryCreator();
		repositoryCreator.createWorkflowComponent(resourceSet,
				analyzeHttpRequests.getTransitionMatrix());

		// create new assembly in system
		SystemCreator systemCreator = new SystemCreator();
		systemCreator.updateSystem(resourceSet);

		// update allocation model
		AllocationCreator allocationCreator = new AllocationCreator();
		allocationCreator.updateAllocation(resourceSet);

		// Create Usage Model and Update Performance Model
		UsagemodelCreator usagemodelCreator = new UsagemodelCreator();
		usagemodelCreator.createUsageModel(resourceSet,
				analyzeHttpRequests.getTransitionMatrix());

		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
