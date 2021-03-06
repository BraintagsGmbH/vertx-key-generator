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
package de.braintags.vertx.keygenerator;

import java.util.Objects;

import de.braintags.vertx.keygenerator.impl.MongoKeyGenerator;
import de.braintags.vertx.util.ExceptionUtil;
import de.braintags.vertx.util.exception.InitException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * A verticle which is generating unique IDs, which can be used by external applications.
 * 
 * @author Michael Remme
 * 
 */
public class KeyGeneratorVerticle extends AbstractVerticle {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(KeyGeneratorVerticle.class);
  public static final String SERVICE_NAME = KeyGeneratorVerticle.class.getName();

  private KeyGeneratorSettings settings;

  /**
   * The verticle knows only one IKeyGenerator. If different generators shall be used, then the implementation of a
   * multi generator is required
   */
  private IKeyGenerator keyGenerator;

  /**
   * Create a new instance by reading the settings from local file
   */
  public KeyGeneratorVerticle() {
    // empty
  }

  /**
   * Create a new instance by using the given settings
   * 
   * @param settings
   */
  public KeyGeneratorVerticle(KeyGeneratorSettings settings) {
    this.settings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.vertx.core.AbstractVerticle#start()
   */
  @Override
  public void start(Future<Void> startFuture) {
    try {
      if (settings == null) {
        settings = initSettings();
      }
      init(startFuture);
    } catch (Exception e) {
      startFuture.fail(e);
    }
  }

  private void init(Future<Void> startFuture) {
    Objects.requireNonNull(settings.getKeyGeneratorClass(), "The keygenerator class must be set in the settings");
    try {
      keyGenerator = settings.getKeyGeneratorClass().newInstance();
      keyGenerator.init(settings, vertx, result -> {
        if (result.failed()) {
          startFuture.fail(result.cause());
        } else {
          vertx.eventBus().consumer(SERVICE_NAME, message -> keyGenerator.generateKey(message));
          startFuture.complete();
        }
      });
    } catch (Exception e) {
      startFuture.fail(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.vertx.core.AbstractVerticle#stop(io.vertx.core.Future)
   */
  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    LOGGER.info("Stop called");
    keyGenerator.shutdown(result -> {
      if (result.failed()) {
        stopFuture.fail(new RuntimeException(result.cause()));
      } else {
        stopFuture.complete();
      }
    });
  }

  /**
   * Initialize the {@link KeyGeneratorSettings} which are used to init the current instance
   * 
   * @return
   * @throws Exception
   */
  protected KeyGeneratorSettings initSettings() {
    try {
      KeyGeneratorSettings s = KeyGeneratorSettings.loadSettings(vertx, MongoKeyGenerator.class, context);
      if (!s.isEdited()) {
        throw new InitException(
            "The settings are not yet edited. Change the value of property 'edited' to true inside the appropriate file");
      }
      return s;
    } catch (Exception e) {
      LOGGER.error("", e);
      throw ExceptionUtil.createRuntimeException(e);
    }
  }

}
