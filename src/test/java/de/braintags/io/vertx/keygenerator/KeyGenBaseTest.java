/*
 * #%L
 * vertx-key-generator
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

import de.braintags.io.vertx.BtVertxTestBase;
import de.braintags.io.vertx.util.ResultObject;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public abstract class KeyGenBaseTest extends BtVertxTestBase {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(KeyGenBaseTest.class);

  protected KeyGeneratorVerticle keyGenVerticle;

  protected long requestNext(TestContext context, String request, long expected) {
    Async async = context.async();
    ResultObject ro = new ResultObject<>(null);
    vertx.eventBus().send(KeyGeneratorVerticle.SERVICE_NAME, request, result -> {
      if (result.failed()) {
        LOGGER.error(result.cause());
        ro.setThrowable(result.cause());
        async.complete();
      } else {
        try {
          ro.setResult(checkReply(context, result.result(), expected));
        } catch (Exception e) {
          ro.setThrowable(e);
        } finally {
          async.complete();
        }
      }
    });
    async.await();
    if (ro.isError()) {
      createAssertionError(ro.getThrowable());
    }
    return (long) ro.getResult();
  }

  protected Long checkReply(TestContext context, Message<Object> reply, long expect) {
    context.assertTrue(reply.body().getClass() == Long.class,
        "reply is not a long: " + reply.body().getClass() + " | " + reply.body().toString());
    if (expect >= 0) {
      context.assertEquals(expect, reply.body(), "not the expected value");
    }
    return (long) reply.body();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.BtVertxTestBase#initBeforeTest(io.vertx.ext.unit.TestContext)
   */
  @Override
  public void initTest(TestContext context) {
    super.initTest(context);
    if (keyGenVerticle == null) {
      LOGGER.info("init Keygenerator");
      Async async = context.async();
      keyGenVerticle = createKeyGenerator(context);
      vertx.deployVerticle(keyGenVerticle, result -> {
        if (result.failed()) {
          context.fail(result.cause());
          async.complete();
        } else {
          async.complete();
        }
      });
      async.awaitSuccess();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.BtVertxTestBase#stopTest(io.vertx.ext.unit.TestContext)
   */
  @Override
  protected void stopTest(TestContext context) {
    undeployVerticle(context, keyGenVerticle);
    keyGenVerticle = null;
    super.stopTest(context);
  }

  public KeyGeneratorVerticle createKeyGenerator(TestContext context) {
    KeyGeneratorSettings settings = new KeyGeneratorSettings();
    modifyKeyGeneratorVerticleSettings(context, settings);
    return new KeyGeneratorVerticle(settings);
  }

  /**
   * Possibility to adapt the settings to the needs of the test
   * 
   * @param context
   * @param settings
   */
  protected void modifyKeyGeneratorVerticleSettings(TestContext context, KeyGeneratorSettings settings) {
  }

}
