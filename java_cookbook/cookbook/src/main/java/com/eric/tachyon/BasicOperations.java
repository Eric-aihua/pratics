package com.eric.tachyon;

/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tachyon.Constants;
import tachyon.TachyonURI;
import tachyon.client.OutStream;
import tachyon.client.TachyonByteBuffer;
import tachyon.client.TachyonFile;
import tachyon.client.TachyonFS;
import tachyon.client.WriteType;
import tachyon.conf.TachyonConf;
import tachyon.util.CommonUtils;

public class BasicOperations implements Callable<Boolean> {
	private static final Logger LOG = LoggerFactory.getLogger(Constants.LOGGER_TYPE);

	private final TachyonURI mMasterLocation;
	private final TachyonURI mFilePath;
	private final WriteType mWriteType;
	private final int mNumbers = 100000;
	


	public BasicOperations(TachyonURI masterLocation, TachyonURI filePath, WriteType writeType) {
		mMasterLocation = masterLocation;
		mFilePath = filePath;
		mWriteType = writeType;
	}

	public Boolean call() throws Exception {
		TachyonFS tachyonClient = TachyonFS.get(mMasterLocation, new TachyonConf());
		createFile(tachyonClient);
		writeFile(tachyonClient);
		return readFile(tachyonClient);
	}

	private void createFile(TachyonFS tachyonClient) throws IOException {
		LOG.debug("Creating file...");
		long startTimeMs = CommonUtils.getCurrentMs();
		if(tachyonClient.exist(mFilePath)){
			tachyonClient.delete(mFilePath, true);
		}
		int fileId = tachyonClient.createFile(mFilePath);
		CommonUtils.printTimeTakenMs(startTimeMs, LOG, "createFile with fileId " + fileId);
	}

	private void writeFile(TachyonFS tachyonClient) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(mNumbers * 4);
		buf.order(ByteOrder.nativeOrder());
		for (int k = 0; k < mNumbers; k++) {
			buf.putInt(k);
		}

		buf.flip();
		LOG.info("Writing data...");
		buf.flip();

		long startTimeMs = CommonUtils.getCurrentMs();
		TachyonFile file = tachyonClient.getFile(mFilePath);
		OutStream os = file.getOutStream(mWriteType);
		os.write(buf.array());
		os.close();

		CommonUtils.printTimeTakenMs(startTimeMs, LOG, "############writeFile to file " + mFilePath);
	}

	private boolean readFile(TachyonFS tachyonClient) throws IOException {
		boolean pass = true;
		LOG.debug("Reading data...");

		final long startTimeMs = CommonUtils.getCurrentMs();
		TachyonFile file = tachyonClient.getFile(mFilePath);
		TachyonByteBuffer buf = file.readByteBuffer(0);
		if (buf == null) {
			file.recache();
			buf = file.readByteBuffer(0);
		}
		buf.mData.order(ByteOrder.nativeOrder());
		for (int k = 0; k < mNumbers; k++) {
			pass = pass && (buf.mData.getInt() == k);
		}
		buf.close();

		CommonUtils.printTimeTakenMs(startTimeMs, LOG, "#############readFile file " + mFilePath);
		return pass;
	}

	public static void main(String[] args) throws IllegalArgumentException {
		String masterIP="tachyon://192.168.20.212:19998";
		String filePath="/tmp/test123";
		Utils.runExample(
				new BasicOperations(new TachyonURI(masterIP), new TachyonURI(filePath), WriteType.CACHE_THROUGH));
	}
}
