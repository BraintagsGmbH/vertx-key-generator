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

import de.braintags.io.vertx.keygenerator.KeyGeneratorSettings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

/**
 * The {@link DebugGenerator} is reset to 0 by each start and can be used for testing. It generates an
 * identifyer as long for each request
 * 
 * @author Michael Remme
 * 
 */
public class DebugGenerator extends AbstractKeyGenerator {

  public static final String NAME = "DEBUG";
  public static final String RESET = "reset";
  private long counter = 1;

  /**
   * @param name
   * @param datastore
   */
  public DebugGenerator() {
    super(NAME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#init(de.braintags.io.vertx.keygenerator.Settings)
   */
  @Override
  public void init(KeyGeneratorSettings settings, Handler<AsyncResult<Void>> handler) {
    handler.handle(Future.succeededFuture());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#generateKey(java.lang.String)
   */
  @Override
  public void generateKey(Message<?> message) {
    if (message.body().equals(RESET)) {
      counter = 1;
      message.reply(counter);
    } else {
      message.reply(counter++);
    }
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
