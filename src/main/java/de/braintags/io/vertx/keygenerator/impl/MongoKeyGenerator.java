/*
 * #%L
 * vertx-pojongo
 * %%
 * Copyright (C) 2015 Braintags GmbH
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package de.braintags.io.vertx.keygenerator.impl;

import java.util.Properties;

import de.braintags.io.vertx.keygenerator.Settings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * This generator generates keys and stores the current counter inside a local file
 * 
 * @author Michael Remme
 * 
 */
public class MongoKeyGenerator extends AbstractKeyGenerator {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(MongoKeyGenerator.class);
  public static final String NAME = "FileKeyGenerator";

  private static final String FILENAME = "pojomapperKeys";
  private JsonObject keyMap;
  private String fileDestination;

  /**
   * @param name
   * @param datastore
   */
  public MongoKeyGenerator() {
    super(NAME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#init(de.braintags.io.vertx.keygenerator.Settings)
   */
  @Override
  public void init(Settings settings) throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#generateKey(java.lang.String)
   */
  @Override
  public void generateKey(Message<?> message) {
    message.reply("errr");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#shutdown(io.vertx.core.Handler)
   */
  @Override
  public void shutdown(Handler<AsyncResult<Void>> handler) {
    super.shutdown(handler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#createDefaultProperties()
   */
  @Override
  public Properties createDefaultProperties() {
    return new Properties();
  }

}
