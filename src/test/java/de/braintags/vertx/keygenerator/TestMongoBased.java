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

import org.junit.Test;

import de.braintags.vertx.keygenerator.impl.MongoKeyGenerator;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class TestMongoBased extends KeyGenBaseTest {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(TestMongoBased.class);

  private static final String COLLECTION = "MySequenceCollection";
  private long currentCounter;

  @Test
  public void testOne(TestContext context) {
    currentCounter = requestNext(context, "TestMapper", currentCounter + 1);
  }

  @Test
  public void testMore(TestContext context) {
    for (int i = 1; i < 50; i++) {
      currentCounter = requestNext(context, "TestMapper", currentCounter + 1);
    }
  }

  @Test
  public void testError(TestContext context) {
    try {
      requestNext(context, "", -1);
      context.fail("expected an exception here");
    } catch (AssertionError e) {
      context.assertTrue(e.getMessage().contains("-1"), "The message should contain the code -1");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.KeyGenBaseTest#modifySettings(io.vertx.ext.unit.TestContext,
   * de.braintags.vertx.keygenerator.Settings)
   */
  @Override
  protected void modifyKeyGeneratorVerticleSettings(TestContext context, KeyGeneratorSettings settings) {
    settings.setKeyGeneratorClass(MongoKeyGenerator.class);
    settings.getGeneratorProperties().put(MongoKeyGenerator.COLLECTTION_PROP, COLLECTION);
    settings.getGeneratorProperties().put(MongoKeyGenerator.START_MONGO_LOCAL_PROP,
        System.getProperty(MongoKeyGenerator.START_MONGO_LOCAL_PROP));
    super.modifyKeyGeneratorVerticleSettings(context, settings);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.KeyGenBaseTest#initTest(io.vertx.ext.unit.TestContext)
   */
  @Override
  public void initTest(TestContext context) {
    super.initTest(context);
    currentCounter = requestNext(context, "TestMapper", -1);
  }

}
