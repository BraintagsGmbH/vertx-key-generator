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
/**
 * :numbered:
 * :toc: left
 * :toclevels: 3
 *
 * == Generator for unique identifyers
 *
 * Keygenerator is a Verticle to create unique IDs in different ways and is used by the project Vertx-Pojo-Mapper, for
 * instance, to locally create primary keys for new records.
 *
 * ### Using Keygenerator in Maven or Gradle project
 *
 * Add a dependency to the artifact:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 * <groupId>${maven.groupId}</groupId>
 * <artifactId>${maven.artifactId}</artifactId>
 * <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 *
 * === Initialization of the verticle
 * The verticle is initialized by reading a local Json file. The location of this file can be defined by adding the
 * location into the deployment options like shown in the example above. If this property is not set, then a default
 * location is used, which refers to the local user directory and there inside the path
 * ".braintags/vertx/KeyGenerator/KeyGeneratorVerticle.settings.json"
 * If the settings file does not exist, it will be created with some default data and then the application will exit
 * with an exception, which shows the location of the created settings. You will have to edit the settings file and will
 * have at a minimum to set the property "edited" to true, otherwise the application won't launch.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#initKeyGeneratorVerticle(io.vertx.core.Vertx, io.vertx.core.Future) }
 * ----
 *
 * Above is displayed an example settings file, which uses MongoDb as a source of key generation.
 *
 * [source,java]
 * ----
 * {
 * "edited": true,
 * "keyGeneratorClass": "de.braintags.vertx.keygenerator.impl.MongoKeyGenerator",
 * "generatorProperties": {
 *   "db_name": "KeygeneratoDb",
 *   "startMongoLocal": "false",
 *   "localPort": "27017",
 *   "connection_string": "mongodb://192.168.42.129:27017",
 *   "shared": "false"
 * }
 *}
 * ----
 * The main information of the settings file is the property "keyGeneratorClass", which must refer to an existing
 * implementation of {@link de.braintags.vertx.keygenerator.IKeyGenerator}. Some implementations are described below.
 * Aside this, the field generatorProperties exists, where inside some properties must or can be defined, which are
 * depending on the defined keyGeneratorClass.
 *
 * === Requesting a key from an application
 *
 * [source,java]
 * ----
 * {@link examples.Examples#requestNextKey(String) }
 * ----
 *
 * To retrive a key, one is sending an event to the vertx eventbus, where a name can be sent as a parameter. For each
 * different name, which is sent here, one unique id sequence is generated. The returned id is then of type long.
 *
 *
 * === The different versions of {@link de.braintags.vertx.keygenerator.IKeyGenerator}
 *
 * {@link de.braintags.vertx.keygenerator.impl.DebugGenerator}
 * Can be used for debugging or testing purpose. For each application start it starts with 0.
 *
 * {@link de.braintags.vertx.keygenerator.impl.FileKeyGenerator}
 * This implementation stores the current state of the id generator as local file. By using the property
 * {@link de.braintags.vertx.keygenerator.impl.FileKeyGenerator#DESTINATION_DIRECTORY_PROP} you can define the
 * location of this file.
 *
 * {@link de.braintags.vertx.keygenerator.impl.MongoKeyGenerator}
 * MongoKeyGenerator is using MongoDb as a source to store the state of the id generator. In the properties one will
 * define the location and the name of the database which shall be used.
 *
 *
 * @author Michael Remme
 *
 */

@Document(fileName = "index.adoc")
package de.braintags.vertx.keygenerator;

import io.vertx.docgen.Document;
