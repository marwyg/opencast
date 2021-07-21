/**
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 *
 * The Apereo Foundation licenses this file to you under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at:
 *
 *   http://opensource.org/licenses/ecl2.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.opencastproject.speechtotext.impl.engine;

import org.opencastproject.speechtotext.api.SpeechToTextEngine;
import org.opencastproject.speechtotext.api.SpeechToTextEngineException;
import org.opencastproject.util.ConfigurationException;
import org.opencastproject.util.IoSupport;

import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/** Vosk implementation of the Speech-to-text engine interface. */
@Component(
    immediate = true,
    service = {
        SpeechToTextEngine.class,
        ManagedService.class
    },
    property = {
        "service.description=Vosk implementation of the SpeechToTextEngine interface",
        "service.pid=org.opencastproject.speechtotext.impl.engine.VoskEngine"
    }
)
public class VoskEngine implements SpeechToTextEngine, ManagedService {

  private static final Logger logger = LoggerFactory.getLogger(VoskEngine.class);
  private static final String engineName = "Vosk";
  private static final String VOSK_ROOT_PATH_CONFIG_KEY = "vosk.root.path";
  private File voskRootDirectory;

  @Override
  public String getEngineName() {
    return engineName;
  }

  @Activate
  public void activate(ComponentContext cc) {
    logger.debug("Activating Vosk as viable speech-to-text engine...");
  }

  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
    if (properties == null) {
      return;
    }
    logger.debug("Start updating Vosk configuration.");

    String voskPath = (String) properties.get(VOSK_ROOT_PATH_CONFIG_KEY);
    if (voskPath == null) {
      logger.error("Path to Vosk root directory not set");
      return;
    } else {
      File voskWorkingDirectory = new File(voskPath);
      if (!voskWorkingDirectory.isDirectory()) {
        logger.error("Path to Vosk root directory is not valid. The Path: '{}'", voskPath);
        return;
      } else {
        this.voskRootDirectory = voskWorkingDirectory;
      }
    }
    logger.debug("Finished updating Vosk configuration");
  }

  /**
   * {@inheritDoc}
   *
   * @see org.opencastproject.speechtotext.api.SpeechToTextEngine#generateSubtitlesFile(URI, File, String)
   */
  @Override
  public File generateSubtitlesFile(URI mediaFile, File preparedOutputFile, String language)
          throws SpeechToTextEngineException {

    final List<String> command = new ArrayList<>();
    command.add(voskRootDirectory.getAbsolutePath() + "/venv/bin/python3"); // TODO: change this
    command.add(voskRootDirectory.getAbsolutePath() + "/test_webvtt.py");   // TODO: change this
    command.add("-i");
    command.add(mediaFile.toString());
    command.add("-o");
    command.add(preparedOutputFile.getAbsolutePath());
    command.add("-l");
    command.add(language);
    logger.info("Executing Vosk's transcription command: {}", command);

    Process process = null;
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.redirectErrorStream(true);
      process = processBuilder.start();

      // wait until the task is finished
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new SpeechToTextEngineException(
                String.format("Vosk exited abnormally with status %d (command: %s)", exitCode, command));
      }
      if (!preparedOutputFile.isFile()) {
        throw new SpeechToTextEngineException("Vosk produced no output");
      }
      logger.info("Subtitles file generated successfully: {}", preparedOutputFile);
    } catch (Exception e) {
      logger.debug("Transcription failed closing Vosk transcription process for: {}", mediaFile);
      throw new SpeechToTextEngineException(e);
    } finally {
      IoSupport.closeQuietly(process);
    }

    return preparedOutputFile; // now containing subtitles data
  }

}
