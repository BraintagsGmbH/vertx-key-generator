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
package de.braintags.io.vertx.keygenerator.impl;

import de.braintags.io.vertx.keygenerator.IKeyGenerator;
import de.braintags.io.vertx.keygenerator.KeyGeneratorSettings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * An abstract implementation of {@link IKeyGenerator}
 * 
 * @author Michael Remme
 * 
 */
public abstract class AbstractKeyGenerator implements IKeyGenerator {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(AbstractKeyGenerator.class);

  private String name;
  private Vertx vertx;

  /**
   * 
   * @param name
   *          the name of the generator
   */
  public AbstractKeyGenerator(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#shutdown(io.vertx.core.Handler)
   */
  @Override
  public void shutdown(Handler<AsyncResult<Void>> handler) {
    LOGGER.info("Shutdown called");
    handler.handle(Future.succeededFuture());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#init(de.braintags.io.vertx.keygenerator.Settings,
   * io.vertx.core.Vertx)
   */
  @Override
  public final void init(KeyGeneratorSettings settings, Vertx vertx, Handler<AsyncResult<Void>> handler) throws Exception {
    this.vertx = vertx;
    init(settings, handler);
  }

  /**
   * Internal init after the instance of vertx was stored
   * 
   * @param settings
   * @param handler
   *          the handler to be informed
   */
  protected abstract void init(KeyGeneratorSettings settings, Handler<AsyncResult<Void>> handler) throws Exception;

  /**
   * @return the vertx
   */
  protected final Vertx getVertx() {
    return vertx;
  }

}
