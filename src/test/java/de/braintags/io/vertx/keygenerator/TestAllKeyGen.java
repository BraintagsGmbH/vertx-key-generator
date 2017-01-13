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
package de.braintags.io.vertx.keygenerator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ TestDebugGenerator.class, TestFileBased.class, TestMongoBased.class })
public class TestAllKeyGen {

}
