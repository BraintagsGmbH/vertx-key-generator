/*
 * #%L
 * Verticle for generation of IDs
 * %%
 * Copyright (C) 2017 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.vertx.keygenerator.impl;

import java.io.IOException;
import java.util.Properties;

import de.braintags.vertx.keygenerator.KeyGeneratorSettings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

/**
 * This generator generates keys and stores the current counter inside a local file
 * 
 * @author Michael Remme
 * 
 */
public class FileKeyGenerator extends AbstractKeyGenerator {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(FileKeyGenerator.class);
  public static final String NAME = "FileKeyGenerator";

  /**
   * The property, which defines the destination directory, where the local file is stored
   */
  public static final String DESTINATION_DIRECTORY_PROP = "destinationDirectory";

  /**
   * The property, which defines, wether the file, where the current values are stored, shall be reset
   */
  public static final String RESET_PROP = "doReset";

  private static final String FILENAME = FileKeyGenerator.class.getSimpleName();

  private JsonObject keyMap;
  private String destinationDir;
  private String fileDestination;
  private FileSystem fileSystem;
  private boolean reset;

  /**
   * @param name
   * @param datastore
   */
  public FileKeyGenerator() {
    super(NAME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.IKeyGenerator#init(de.braintags.vertx.keygenerator.Settings)
   */
  @Override
  public void init(KeyGeneratorSettings settings, Handler<AsyncResult<Void>> handler) throws Exception {
    try {
      destinationDir = settings.getGeneratorProperties().getProperty(DESTINATION_DIRECTORY_PROP,
          KeyGeneratorSettings.LOCAL_USER_DIRECTORY);
      fileDestination = destinationDir + (destinationDir.endsWith("/") ? "" : "/") + FILENAME;
      fileSystem = getVertx().fileSystem();
      reset = Boolean.valueOf(settings.getGeneratorProperties().getProperty(RESET_PROP, "false"));
      LOGGER.info("Storing file into " + fileDestination);
      loadKeyMap();
      handler.handle(Future.succeededFuture());
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.IKeyGenerator#createDefaultProperties()
   */
  @Override
  public Properties createDefaultProperties() {
    Properties props = new Properties();
    props.put(DESTINATION_DIRECTORY_PROP, KeyGeneratorSettings.LOCAL_USER_DIRECTORY);
    return props;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.IKeyGenerator#generateKey(java.lang.String)
   */
  @Override
  public void generateKey(Message<?> message) {
    String key = (String) message.body();
    if (key == null || key.hashCode() == 0) {
      message.fail(-1, "no keyname sent!");
    }
    message.reply(generateKey(key));
  }

  public long generateKey(String keyName) {
    long key = getNextKey(keyName);
    storeKeyMap();
    return key;
  }

  private long getNextKey(String keyName) {
    long key = keyMap.getLong(keyName, (long) 0);
    keyMap.put(keyName, ++key);
    return key;
  }

  /**
   * Not blocking, but no one is waiting for
   *
   * @param fs
   */
  private void storeKeyMap() {
    fileSystem.writeFile(fileDestination, Buffer.buffer(keyMap.encode()), result -> {
      if (result.failed()) {
        LOGGER.error("Error on saving file", result.cause());
      }
    });
  }

  /**
   * BLOCKING
   *
   * @param fs
   * @throws IOException
   */
  private void loadKeyMap() throws IOException {
    if (!fileSystem.existsBlocking(this.destinationDir)) {
      fileSystem.mkdirsBlocking(destinationDir);
    }
    if (fileSystem.existsBlocking(fileDestination)) {
      if (reset) {
        fileSystem.deleteBlocking(fileDestination);
        keyMap = new JsonObject();
      } else {
        Buffer buffer = fileSystem.readFileBlocking(fileDestination);
        keyMap = new JsonObject(buffer.toString());
      }
    } else {
      keyMap = new JsonObject();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.IKeyGenerator#shutdown(io.vertx.core.Handler)
   */
  @Override
  public void shutdown(Handler<AsyncResult<Void>> handler) {
    super.shutdown(handler);
  }

}
