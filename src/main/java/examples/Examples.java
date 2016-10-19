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
package examples;

import de.braintags.io.vertx.keygenerator.KeyGeneratorSettings;
import de.braintags.io.vertx.keygenerator.KeyGeneratorVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;

/**
 * 
 * 
 * @author Michael Remme
 * 
 */
public class Examples {
  private Vertx vertx;

  /**
   * 
   */
  public Examples() {
  }
  
  @Source(translate = false)
  public void initKeyGeneratorVerticle(Vertx vertx, Future<Void> startFuture) {
    DeploymentOptions options = new DeploymentOptions();
    String settingsLocation = "settings/kgSettings.json";
    options.setConfig(new JsonObject().put(KeyGeneratorSettings.SETTINGS_LOCATION_PROPERTY, settingsLocation));
    vertx.deployVerticle(KeyGeneratorVerticle.class.getName(), options, result -> {
      if (result.failed()) {
        startFuture.fail(result.cause());
      } else {
        startFuture.complete();
      }
    });
  }

  public void requestNextKey(String keyContext) {
    vertx.eventBus().send(KeyGeneratorVerticle.SERVICE_NAME, keyContext, result -> {
      if (result.failed()) {
        result.cause().printStackTrace();
      } else {
        Long id = (Long) result.result().body();
        System.out.println(id);
      }
    });
  }

}
