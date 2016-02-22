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


package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.old;

import java.util.ArrayList;


public class Transition {

	String fromRequestURL;
	String toRequestURL;
	double probability = 0.0;
	int nrbOfTransitions = 0;
	double sumThinkTime = 0.0;
	ArrayList<HttpRequestInstance> httpRequests;

	/**
	 * @return the averageThinkTime
	 */
	public final double getAverageThinkTime() {
		if (nrbOfTransitions > 0) {
			return sumThinkTime / nrbOfTransitions;
		} else {
			return 0.0;
		}
	}

	/**
	 * @return the fromRequestURL
	 */
	public final String getFromRequestURL() {
		return fromRequestURL;
	}

	/**
	 * @param fromRequestURL
	 *            the fromRequestURL to set
	 */
	public final void setFromRequestURL(String fromRequestURL) {
		this.fromRequestURL = fromRequestURL;
	}

	/**
	 * @return the toRequestURL
	 */
	public final String getToRequestURL() {
		return toRequestURL;
	}

	/**
	 * @param toRequestURL
	 *            the toRequestURL to set
	 */
	public final void setToRequestURL(String toRequestURL) {
		this.toRequestURL = toRequestURL;
	}

	/**
	 * @return the probability
	 */
	public final double getProbability() {
		return probability;
	}

	/**
	 * @param probability
	 *            the probability to set
	 */
	public final void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * @return the nrbOfTransitions
	 */
	public final int getNrbOfTransitions() {
		return nrbOfTransitions;
	}

	/**
	 * @param nrbOfTransitions
	 *            the nrbOfTransitions to set
	 */
	public final void increaseNrbOfTransitions() {
		this.nrbOfTransitions++;
	}

	/**
	 * @return the thinkTime
	 */
	public final double getSumThinkTime() {
		return sumThinkTime;
	}

	/**
	 * @param thinkTime
	 *            the thinkTime to set
	 */
	public final void increaseSumThinkTime(double thinkTime) {
		this.sumThinkTime = this.sumThinkTime + thinkTime;
	}

	/**
	 * @return the httpRequests
	 */
	public final ArrayList<HttpRequestInstance> getHttpRequests() {
		return httpRequests;
	}

	/**
	 * @param httpRequests
	 *            the httpRequests to set
	 */
	public final void addHttpRequests(HttpRequestInstance httpRequest) {
		if (this.httpRequests == null) {
			this.httpRequests = new ArrayList<HttpRequestInstance>();
		}
		this.httpRequests.add(httpRequest);
	}

}
