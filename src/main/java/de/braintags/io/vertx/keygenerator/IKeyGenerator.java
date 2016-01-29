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
package de.braintags.io.vertx.keygenerator;

import java.util.Properties;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * The IKeyGenerator is used to generate key for use with external applications
 * 
 * @author Michael Remme
 * 
 */
public interface IKeyGenerator {

  /**
   * Initialize the keygenerator
   * 
   * @param settings
   *          the settings to be used
   * @param vertx
   *          the current instance of Vertx
   * @param handler
   *          the handler to be informed
   */
  void init(Settings settings, Vertx vertx, Handler<AsyncResult<Void>> handler) throws Exception;

  /**
   * This method is requested, when the {@link Settings} are created new
   * 
   * @return
   */
  Properties createDefaultProperties();

  /**
   * Generates a key and returns it
   * 
   * @param message
   *          the message received by the eventbus
   */
  void generateKey(Message<?> message);

  /**
   * Finish the key generator
   * 
   * @param handler
   *          the handler to be informed
   */
  void shutdown(Handler<AsyncResult<Void>> handler);

  /**
   * Get the name of the generator
   * 
   * @return the name
   */
  String getName();

}
