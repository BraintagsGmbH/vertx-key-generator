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

import de.braintags.vertx.keygenerator.impl.FileKeyGenerator;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class TestFileBased extends KeyGenBaseTest {
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
   * @see de.braintags.vertx.keygenerator.KeyGenBaseTest#initTest(io.vertx.ext.unit.TestContext)
   */
  @Override
  public void initTest(TestContext context) {
    super.initTest(context);
    currentCounter = requestNext(context, "TestMapper", -1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.vertx.keygenerator.KeyGenBaseTest#modifySettings(io.vertx.ext.unit.TestContext,
   * de.braintags.vertx.keygenerator.Settings)
   */
  @Override
  protected void modifyKeyGeneratorVerticleSettings(TestContext context, KeyGeneratorSettings settings) {
    settings.setKeyGeneratorClass(FileKeyGenerator.class);
    super.modifyKeyGeneratorVerticleSettings(context, settings);
    settings.getGeneratorProperties().put(FileKeyGenerator.DESTINATION_DIRECTORY_PROP, "tmp");
    settings.getGeneratorProperties().put(FileKeyGenerator.RESET_PROP, "true");
  }

}
