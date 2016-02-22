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
import java.util.List;


/**
 * @author voegele
 * 
 */
public class AnalyzeHttpRequests {

	// transition Matrix, stores all possible transitions to represent the
	// CBMG
	private Transition[][] transitionMatrix;

	/**
	 * @return
	 */
	public Transition[][] getTransitionMatrix() {
		return transitionMatrix;
	}

	/**
	 * @param httpInstanceList
	 */
	public void analyze(List<HttpRequestInstance> httpInstanceList) {
		List<HttpRequestInstance> filteredHttpInstanceList = filterHttpRequests(httpInstanceList);
		List<String> uniqueURLS = getUniqueURLS(filteredHttpInstanceList);

		// init matrix with found URLs
		transitionMatrix = new Transition[uniqueURLS.size()][uniqueURLS.size()];
		for (int i = 0; i < transitionMatrix.length; i++) {
			for (int s = 0; s < transitionMatrix[i].length; s++) {
				transitionMatrix[i][s] = new Transition();
				transitionMatrix[i][s].setFromRequestURL(uniqueURLS.get(i));
				transitionMatrix[i][s].setToRequestURL(uniqueURLS.get(s));
			}
		}
		fillTransitionMatrix(uniqueURLS, filteredHttpInstanceList);
		calculateProbability();
		printTransitionMatrix();
	}

	/**
	 * @param httpInstanceList
	 * @return
	 */
	private List<HttpRequestInstance> filterHttpRequests(
			List<HttpRequestInstance> httpInstanceList) {
		// pro referer den ersten Request
		List<HttpRequestInstance> httpRequestList = new ArrayList<HttpRequestInstance>();
		String lastReferer = null;
		String newReferer = null;

		// filter requests
		for (HttpRequestInstance httpInstance : httpInstanceList) {

			// filter images
			if (httpInstance.getAccept().contains("image/png,image/*")) {
				continue;
			}

			// immer den ersten eintrag pro referer speichern
			newReferer = httpInstance.getReferer();
			if (lastReferer == null || !lastReferer.equals(newReferer)) {
				if (!httpRequestList.contains(httpInstance)) {

					// getStartTimeOfLastRequest
					if (httpRequestList.size() > 0) {
						httpInstance.setThinkTime(httpInstance
								.getStartHttpRequest()
								- httpRequestList.get(
										httpRequestList.size() - 1)
										.getStartHttpRequest());
					}
					httpRequestList.add(httpInstance);
				}
			}
			lastReferer = newReferer;
		}
		return httpRequestList;
	}

	/**
	 * 
	 */
	private void calculateProbability() {
		for (int i = 0; i < transitionMatrix.length; i++) {
			int sumTransitions = 0;
			for (int s = 0; s < transitionMatrix[i].length; s++) {
				sumTransitions += transitionMatrix[i][s].getNrbOfTransitions();
			}

			if (sumTransitions > 0) {
				for (int s = 0; s < transitionMatrix[i].length; s++) {
					double probability = (double) transitionMatrix[i][s]
							.getNrbOfTransitions() / (double) sumTransitions;
					transitionMatrix[i][s].setProbability(probability);
				}
			}
		}
	}

	/**
	 * @param filteredHttpInstanceList
	 * @return
	 */
	private List<String> getUniqueURLS(
			List<HttpRequestInstance> filteredHttpInstanceList) {
		List<String> uniqueURLS = new ArrayList<String>();
		for (HttpRequestInstance httpRequestInstance : filteredHttpInstanceList) {
			if (!uniqueURLS.contains(httpRequestInstance.getRequestURL())) {
				uniqueURLS.add(httpRequestInstance.getRequestURL());
			}
		}
		return uniqueURLS;
	}

	/**
	 * 
	 */
	private void printTransitionMatrix() {
		// test print
		for (int i = 0; i < transitionMatrix.length; i++) {
			System.out.print(transitionMatrix[i][0].getFromRequestURL() + ";");
			for (int s = 0; s < transitionMatrix[i].length; s++) {
				System.out
						.print(transitionMatrix[i][s].getProbability() + " ;");
			}
			System.out.println("");
		}
	}

	/**
	 * @param uniqueURLS
	 * @param filteredHttpInstanceList
	 */
	private void fillTransitionMatrix(List<String> uniqueURLS,
			List<HttpRequestInstance> filteredHttpInstanceList) {
		// iterate all uniqueURLs
		for (int i = 0; i < uniqueURLS.size(); i++) {
			// iterate all httpRequestInstances
			for (HttpRequestInstance httpRequestInstance : filteredHttpInstanceList) {
				String refererSubString = getRefererSubstring(httpRequestInstance
						.getReferer());
				if (uniqueURLS.get(i).equals(refererSubString)) {
					int s = uniqueURLS.indexOf(httpRequestInstance
							.getRequestURL());
					if (s > -1) {
						transitionMatrix[i][s].increaseNrbOfTransitions();
						transitionMatrix[i][s]
								.increaseSumThinkTime(httpRequestInstance
										.getThinkTime());
						transitionMatrix[i][s]
								.addHttpRequests(httpRequestInstance);
					}
				}
			}
		}
	}

	/**
	 * @param referer
	 * @return
	 */
	private String getRefererSubstring(String referer) {
		int indexQuestionMark = referer.indexOf("?");
		if (indexQuestionMark > -1) {
			return referer.substring(0, indexQuestionMark);
		} else {
			return referer;
		}
	}

}
