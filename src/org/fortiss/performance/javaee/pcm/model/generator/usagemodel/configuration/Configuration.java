package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration;

import java.io.File;

public class Configuration extends Constants {
	public static File getUsageModelFile() {
		return new File(MODEL_DIRECTORY + "/" + USAGE_MODEL_NAME);
	}

	public static File getRepositoryFile() {
		return new File(MODEL_DIRECTORY + "/" + REPOSITORY_MODEL_NAME);
	}

	public static File getAllocationFile() {
		return new File(MODEL_DIRECTORY + "/" + ALLOCATION_MODEL_NAME);
	}

	public static File getSystemFile() {
		return new File(MODEL_DIRECTORY + "/" + SYSTEM_MODEL_NAME);
	}

	public static File getResourceenvironmentFile() {
		return new File(MODEL_DIRECTORY + "/" + INFRASTRUCTURE_MODEL_NAME);
	}
}
