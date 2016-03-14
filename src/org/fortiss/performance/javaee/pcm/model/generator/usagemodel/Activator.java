/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/


package org.fortiss.performance.javaee.pcm.model.generator.usagemodel;

import java.io.File;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Constants;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.AllocationCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.CreatorTools;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.RepositoryCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.SystemCreator;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.creator.UsagemodelCreator;
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
		
		// delete MODEL_DIRECTORY and add content of MODEL_DIRECTORY_SOURCE to MODEL_DIRECTORY
		CreatorTools.deleteFiles(new File(Constants.MODEL_DIRECTORY));
		CreatorTools.copyPCMFilesSourceTarget();
		
		// create new component in repository
		RepositoryCreator repositoryCreator = new RepositoryCreator();
		repositoryCreator.createBehaviorModelComponents();

		// create new assembly in system
		SystemCreator systemCreator = new SystemCreator();
		systemCreator.updateSystem();

		// update allocation model
		AllocationCreator allocationCreator = new AllocationCreator();
		allocationCreator.updateAllocation();

		// Create Usage Model and Update Performance Model
		UsagemodelCreator usagemodelCreator = new UsagemodelCreator();
		usagemodelCreator.createUsageModel();

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
