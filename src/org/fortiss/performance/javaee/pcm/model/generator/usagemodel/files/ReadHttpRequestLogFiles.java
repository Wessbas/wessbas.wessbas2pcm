package org.fortiss.performance.javaee.pcm.model.generator.usagemodel.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.HttpRequestInstance;
import org.fortiss.performance.javaee.pcm.model.generator.usagemodel.configuration.Configuration;

public class ReadHttpRequestLogFiles {

	// Column indices
	static final int STARTTIME = 0;
	static final int ENDTIME = 1;
	static final int CALLORDER = 2;
	static final int CALLSTACKDEPTH = 3;
	static final int METHOD = 4;
	static final int REQUESTURL = 5;
	static final int LOCALADDR = 6;
	static final int LOCALNAME = 7;
	static final int LOCALPORT = 8;
	static final int AUTHTYPE = 9;
	static final int CHARACTERENCODING = 10;
	static final int REFERER = 11;
	static final int ACCEPT = 12;
	static final int ACCEPTENCODING = 13;
	static final int SESSIONID = 14;
	static final int PARAMETER = 15;

	// List of HttpRequests
	private List<HttpRequestInstance> httpRequests;

	/**
	 * file filter for .csv files
	 */
	private static final FileFilter csvfilefilter = new FileFilter() {
		public boolean accept(File file) {
			// if the file extension is .csv return true, else false
			if (file.getName().endsWith(".csv")
					&& file.getName()
							.contains(Configuration.HTTPREQUEST_PREFIX)) {
				return true;
			}
			return false;
		}
	};

	/**
	 * @return
	 */
	public List<HttpRequestInstance> getHttpRequests(File directory) {
		httpRequests = new ArrayList<HttpRequestInstance>();
		readFiles(directory);
		return this.httpRequests;
	}

	/**
	 * read all logged httpRequest files and store in a list.
	 */
	private void readFiles(File directory) {
		try {
			String line;
			File[] finlist = directory.listFiles(csvfilefilter);
			for (int n = 0; n < finlist.length; n++) {
				Boolean firstLine = true;
				FileReader fr = new FileReader(finlist[n]);
				BufferedReader br = new BufferedReader(fr);
				if (finlist[n].isFile()) {
					while ((line = br.readLine()) != null) {
						if (firstLine) {
							firstLine = false;
						} else {
							HttpRequestInstance httpRequestInstance = new HttpRequestInstance();
							final String[] data1 = line
									.split(Configuration.INPUT_FILE_DELIMITER);
							httpRequestInstance.setStartHttpRequest(Long
									.parseLong(data1[STARTTIME]));
							httpRequestInstance.setRequestDuration(Long
									.parseLong(data1[ENDTIME])
									- Long.parseLong(data1[STARTTIME]));
							httpRequestInstance.setCallOrder(Integer
									.parseInt(data1[CALLORDER]));
							httpRequestInstance.setCallOrder(Integer
									.parseInt(data1[CALLSTACKDEPTH]));
							httpRequestInstance.setMethod(data1[METHOD]);
							httpRequestInstance
									.setRequestURL(data1[REQUESTURL]);
							httpRequestInstance.setLocalAddr(data1[LOCALADDR]);
							httpRequestInstance.setLocalName(data1[LOCALNAME]);
							httpRequestInstance.setLocalPort(data1[LOCALPORT]);
							httpRequestInstance
									.setCharacterEncoding(data1[CHARACTERENCODING]);
							httpRequestInstance.setAuthType(data1[AUTHTYPE]);
							httpRequestInstance.setReferer(data1[REFERER]);
							httpRequestInstance.setAccept(data1[ACCEPT]);
							httpRequestInstance
									.setAcceptencoding(data1[ACCEPTENCODING]);

							httpRequestInstance.setSessionId(data1[SESSIONID]);
							if (data1.length > 15) {
								if (data1[PARAMETER]
										.contains(Configuration.INPUT_FILE_DELIMITER_PARAMETERPAIR)
										&& data1[PARAMETER]
												.contains(Configuration.INPUT_FILE_DELIMITER_PARAMETER)) {
									String[] extractedParameterPairs = data1[PARAMETER]
											.split(Configuration.INPUT_FILE_DELIMITER_PARAMETERPAIR);
									HashMap<String, String> parameters = new HashMap<String, String>();
									for (String parameterPair : extractedParameterPairs) {
										String[] extractedParameters = parameterPair
												.split(Configuration.INPUT_FILE_DELIMITER_PARAMETER);
										if (extractedParameters.length > 1) {
											String key = extractedParameters[0];
											String value = extractedParameters[1];
											parameters.put(key, value);
										}
									}
									httpRequestInstance
											.setParameters(parameters);
								}
							}
							httpRequests.add(httpRequestInstance);
						}
					}
				}
				br.close();
				fr.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
