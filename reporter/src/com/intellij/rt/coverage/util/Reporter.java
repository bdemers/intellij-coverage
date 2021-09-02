/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.rt.coverage.util;

import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.instrumentation.SaveHook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Reporter {
  private final File myDataFile;
  private final File mySourceMapFile;
  private ProjectData myProjectData;

  public Reporter(File dataFile, File sourceMapFile) {
    myDataFile = dataFile;
    mySourceMapFile = sourceMapFile;
  }

  private ProjectData getProjectData() throws IOException {
    if (myProjectData == null) {
      myProjectData = ProjectDataLoader.load(myDataFile);
      if (mySourceMapFile != null && mySourceMapFile.exists()) {
        SaveHook.loadAndApplySourceMap(myProjectData, mySourceMapFile);
      }
    }
    return myProjectData;
  }

  public void createXMLReport(File xmlFile) throws IOException {
    final XMLCoverageReport report = new XMLCoverageReport();
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(xmlFile);
      report.write(out, getProjectData());
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
}