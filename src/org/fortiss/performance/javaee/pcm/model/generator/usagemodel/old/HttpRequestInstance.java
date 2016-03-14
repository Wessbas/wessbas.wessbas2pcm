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

import java.util.HashMap;

public class HttpRequestInstance {

	private long startHttpRequest;
	private long requestDuration;
	private String method;
	private int callOrder;
	private int callStackDepth;
	private String requestURL;
	private String localAddr;
	private String localName;
	private String localPort;
	private String authType;
	private String characterEncoding;
	private String referer;
	private String accept;
	private String acceptencoding;
	private String sessionId;
	private HashMap<String, String> parameters;
	private double thinkTime;

	/**
	 * @return the callOrder
	 */
	public final int getCallOrder() {
		return callOrder;
	}

	/**
	 * @param callOrder
	 *            the callOrder to set
	 */
	public final void setCallOrder(int callOrder) {
		this.callOrder = callOrder;
	}

	/**
	 * @return the callStackDepth
	 */
	public final int getCallStackDepth() {
		return callStackDepth;
	}

	/**
	 * @param callStackDepth
	 *            the callStackDepth to set
	 */
	public final void setCallStackDepth(int callStackDepth) {
		this.callStackDepth = callStackDepth;
	}

	/**
	 * @return the requestDuration
	 */
	public final long getRequestDuration() {
		return requestDuration;
	}

	/**
	 * @param requestDuration
	 *            the requestDuration to set
	 */
	public final void setRequestDuration(long requestDuration) {
		this.requestDuration = requestDuration;
	}

	/**
	 * @return the thinkTime
	 */
	public final double getThinkTime() {
		return thinkTime;
	}

	/**
	 * @param thinkTime
	 *            the thinkTime to set
	 */
	public final void setThinkTime(double thinkTime) {
		this.thinkTime = thinkTime;
	}

	/**
	 * @return the accept
	 */
	public final String getAccept() {
		return accept;
	}

	/**
	 * @param accept
	 *            the accept to set
	 */
	public final void setAccept(String accept) {
		this.accept = accept;
	}

	/**
	 * @return the acceptencoding
	 */
	public final String getAcceptencoding() {
		return acceptencoding;
	}

	/**
	 * @param acceptencoding
	 *            the acceptencoding to set
	 */
	public final void setAcceptencoding(String acceptencoding) {
		this.acceptencoding = acceptencoding;
	}

	/**
	 * @return the startHttpRequest
	 */
	public final long getStartHttpRequest() {
		return startHttpRequest;
	}

	/**
	 * @param startHttpRequest
	 *            the startHttpRequest to set
	 */
	public final void setStartHttpRequest(long startHttpRequest) {
		this.startHttpRequest = startHttpRequest;
	}

	/**
	 * @return the method
	 */
	public final String getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public final void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the requestURL
	 */
	public final String getRequestURL() {
		return requestURL;
	}

	/**
	 * @param requestURL
	 *            the requestURL to set
	 */
	public final void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	/**
	 * @return the localAddr
	 */
	public final String getLocalAddr() {
		return localAddr;
	}

	/**
	 * @param localAddr
	 *            the localAddr to set
	 */
	public final void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	/**
	 * @return the localName
	 */
	public final String getLocalName() {
		return localName;
	}

	/**
	 * @param localName
	 *            the localName to set
	 */
	public final void setLocalName(String localName) {
		this.localName = localName;
	}

	/**
	 * @return the localPort
	 */
	public final String getLocalPort() {
		return localPort;
	}

	/**
	 * @param localPort
	 *            the localPort to set
	 */
	public final void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	/**
	 * @return the authType
	 */
	public final String getAuthType() {
		return authType;
	}

	/**
	 * @param authType
	 *            the authType to set
	 */
	public final void setAuthType(String authType) {
		this.authType = authType;
	}

	/**
	 * @return the characterEncoding
	 */
	public final String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * @param characterEncoding
	 *            the characterEncoding to set
	 */
	public final void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	/**
	 * @return the referer
	 */
	public final String getReferer() {
		return referer;
	}

	/**
	 * @param referer
	 *            the referer to set
	 */
	public final void setReferer(String referer) {
		this.referer = referer;
	}

	/**
	 * @return the sessionId
	 */
	public final String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the parameters
	 */
	public final HashMap<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public final void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

}
