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

import org.junit.Test;

import de.braintags.io.vertx.keygenerator.impl.DebugGenerator;
import io.vertx.ext.unit.TestContext;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class TestDebugGenerator extends KeyGenBaseTest {

  @Test
  public void test(TestContext context) {
    requestNext(context, "", 1);
  }

  @Test
  public void test2(TestContext context) {
    for (int i = 1; i < 50; i++) {
      requestNext(context, "", i);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.KeyGenBaseTest#modifySettings(io.vertx.ext.unit.TestContext,
   * de.braintags.io.vertx.keygenerator.Settings)
   */
  @Override
  protected void modifySettings(TestContext context, Settings settings) {
    settings.setKeyGeneratorClass(DebugGenerator.class);
    super.modifySettings(context, settings);
  }

}
