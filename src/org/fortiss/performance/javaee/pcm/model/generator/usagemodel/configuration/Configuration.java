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
