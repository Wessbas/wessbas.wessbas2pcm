package org.fortiss.performance.javaee.pcm.model.generator.usagemodel;

import m4jdsl.WorkloadModel;
import m4jdsl.impl.M4jdslPackageImpl;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.AllocationCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.RepositoryCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.SystemCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.UsagemodelCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.wessbassdsl.XmiEcoreHandler;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author voegele
 * 
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

		// initialize the model package;
		M4jdslPackageImpl.init();

		// Read Workload Model from XMI File
		// might throw an IOException;
		final WorkloadModel workloadModel = (WorkloadModel) XmiEcoreHandler
				.getInstance().xmiToEcore(Constants.XMI_FILE, "xmi");

		// create new component in repository
		RepositoryCreator repositoryCreator = new RepositoryCreator();
		repositoryCreator.createWorkflowComponent(resourceSet, workloadModel);

		// create new assembly in system
		SystemCreator systemCreator = new SystemCreator();
		systemCreator.updateSystem(resourceSet, workloadModel);

		// update allocation model
		AllocationCreator allocationCreator = new AllocationCreator();
		allocationCreator.updateAllocation(resourceSet, workloadModel);

		// Create Usage Model and Update Performance Model
		UsagemodelCreator usagemodelCreator = new UsagemodelCreator();
		usagemodelCreator.createUsageModel(resourceSet, workloadModel);

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
