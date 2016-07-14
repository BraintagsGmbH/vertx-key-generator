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
package de.braintags.io.vertx.keygenerator.impl;

import java.io.IOException;
import java.util.Properties;

import de.braintags.io.vertx.keygenerator.KeyGeneratorSettings;
import de.braintags.io.vertx.util.exception.InitException;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * This generator uses MongoDb to generate and store keys
 * 
 * @author Michael Remme
 * 
 */
public class MongoKeyGenerator extends AbstractKeyGenerator {
  private static final io.vertx.core.logging.Logger LOGGER = io.vertx.core.logging.LoggerFactory
      .getLogger(MongoKeyGenerator.class);
  public static final String NAME = "mongoKeyGenerator";

  /**
   * The name of the property to define, wether a local instance of Mongo shall be started. Usable only for debugging
   * and testing purpose
   */
  public static final String START_MONGO_LOCAL_PROP = "startMongoLocal";

  /**
   * If START_MONGO_LOCAL_PROP is set to true, then this defines the local port to be used. Default is 27017
   */
  public static final String LOCAL_PORT_PROP = "localPort";

  /**
   * The property, which defines the connection string. Something like "mongodb://localhost:27017"
   */
  public static final String CONNECTION_STRING_PROPERTY = "connection_string";

  /**
   * The default connection to be used, if CONNECTION_STRING_PROPERTY is undefined
   */
  public static final String DEFAULT_CONNECTION = "mongodb://localhost:27017";

  /**
   * The name of the property in the config, which defines the database name
   */
  public static final String DBNAME_PROP = "db_name";

  private static final String DEFAULT_DB_NAME = "KeygeneratoDb";

  /**
   * The name of the property in the config, which defines the collection to be used
   */
  public static final String COLLECTTION_PROP = "collection";

  /**
   * The default name of the collection, which is used to store sequence informations
   */
  public static final String DEFAULT_COLLECTION_NAME = "SequenceCollection";

  /**
   * The name of the property in the config, which defines the reference name, which is used to identify the record to
   * be used. The system uses on recod to generate and update sequences. This record is identified by its reference.
   * Therefor the system genreates a field "reference", which it uses to search the record
   */
  public static final String REFERENCE_NAME_PROP = "referenceName";

  private static final String DEFAULT_REFERENCE_NAME = "reference";
  private static final String REFERENCE_FIELD_NAME = "reference";

  /**
   * The name of the property, which defines wether the MongoClient to be used is shared or not
   */
  public static final String SHARED_PROP = "shared";

  private static MongodExecutable exe;
  private boolean startMongoLocal = false;
  private int localPort = 27018;
  private String connectionString;
  private boolean shared = false;
  private String dbName = DEFAULT_DB_NAME;
  private MongoClient mongoClient;
  private String collectionName;
  private String referenceName;
  private JsonObject referenceQuery;

  /**
   * @param name
   * @param datastore
   */
  public MongoKeyGenerator() {
    super(NAME);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#init(de.braintags.io.vertx.keygenerator.Settings)
   */
  @Override
  public void init(KeyGeneratorSettings settings, Handler<AsyncResult<Void>> handler) throws Exception {
    try {
      LOGGER.info("init of MongoKeyGenerator");
      Properties props = settings.getGeneratorProperties();
      shared = Boolean.valueOf(props.getProperty(SHARED_PROP, "false"));
      startMongoLocal = Boolean.valueOf(props.getProperty(START_MONGO_LOCAL_PROP, "false"));
      localPort = Integer.parseInt(props.getProperty(LOCAL_PORT_PROP, String.valueOf(localPort)));

      if (startMongoLocal) {
        connectionString = "mongodb://localhost:" + localPort;
      } else {
        connectionString = props.getProperty(CONNECTION_STRING_PROPERTY, DEFAULT_CONNECTION);
      }
      dbName = props.getProperty(DBNAME_PROP, DEFAULT_DB_NAME);
      collectionName = props.getProperty(COLLECTTION_PROP, DEFAULT_COLLECTION_NAME);
      referenceName = props.getProperty(REFERENCE_NAME_PROP, DEFAULT_REFERENCE_NAME);
      referenceQuery = new JsonObject().put(REFERENCE_FIELD_NAME, referenceName);
      startMongoExe(startMongoLocal, localPort);
      initMongoClient(res -> {
        if (res.failed()) {
          handler.handle(res);
        } else {
          initCounterCollection(handler);
        }
      });
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  private void initCounterCollection(Handler<AsyncResult<Void>> handler) {
    LOGGER.info("Init of sequence collection with " + collectionName);
    mongoClient.count(collectionName, referenceQuery, result -> {
      if (result.failed()) {
        handler.handle(Future.failedFuture(result.cause()));
      } else {
        long count = result.result();
        if (count == 0) {
          LOGGER.info("Inserting initial sequence record into collection " + collectionName);
          this.mongoClient.insert(collectionName, referenceQuery, insertResult -> {
            if (result.failed()) {
              handler.handle(Future.failedFuture(insertResult.cause()));
            } else {
              LOGGER.info(result);
              handler.handle(Future.succeededFuture());
            }
          });
        } else {
          LOGGER.info("Record exists already");
          handler.handle(Future.succeededFuture());
        }
      }
    });
  }

  private void initMongoClient(Handler<AsyncResult<Void>> handler) {
    try {
      Vertx vertx = getVertx();
      JsonObject config = getConfig();
      LOGGER.info("STARTING MONGO CLIENT with config " + config);
      mongoClient = shared ? MongoClient.createShared(vertx, config) : MongoClient.createNonShared(vertx, config);
      if (mongoClient == null) {
        handler.handle(Future.failedFuture(new InitException("No MongoClient created")));
      } else {
        mongoClient.getCollections(resultHandler -> {
          if (resultHandler.failed()) {
            LOGGER.error("", resultHandler.cause());
            handler.handle(Future.failedFuture(resultHandler.cause()));
          } else {
            LOGGER.info(String.format("found %d collections", resultHandler.result().size()));
            handler.handle(Future.succeededFuture());
          }
        });
      }
    } catch (Exception e) {
      handler.handle(Future.failedFuture(new InitException(e)));
    }
  }

  private JsonObject getConfig() {
    JsonObject config = new JsonObject();
    config.put("connection_string", this.connectionString);
    config.put(DBNAME_PROP, this.dbName);
    return config;
  }

  private boolean startMongoExe(boolean startMongoLocal, int localPort) {
    if (startMongoLocal) {
      LOGGER.info("STARTING MONGO EXE");
      try {
        IMongodConfig config = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
            .net(new Net(localPort, Network.localhostIsIPv6())).build();
        exe = MongodStarter.getDefaultInstance().prepare(config);
        exe.start();
        return true;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#generateKey(java.lang.String)
   */
  @Override
  public void generateKey(Message<?> message) {
    String key = (String) message.body();
    if (key == null || key.hashCode() == 0) {
      message.fail(-1, "no keyname sent!");
    }
    generateKey(key, message);
  }

  private void generateKey(String key, Message<?> message) {
    JsonObject execComnand = createSequenceCommand(key);
    mongoClient.runCommand("findAndModify", execComnand, ur -> {
      if (ur.failed()) {
        LOGGER.error("", ur.cause());
        message.fail(-1, ur.cause().toString());
      } else {
        LOGGER.info(ur.result());
        JsonObject resJo = ur.result();
        JsonObject value = resJo.getJsonObject("value");
        long seq = value.getLong(key);
        message.reply(seq);
      }
    });
  }

  private JsonObject createSequenceCommand(String key) {
    JsonObject updateCommand = new JsonObject().put("$inc", new JsonObject().put(key, 1));
    return createFindAndModify(collectionName, updateCommand);
  }

  /*
   * {
   * findAndModify: <collection-name>,
   * query: <document>,
   * sort: <document>,
   * remove: <boolean>,
   * update: <document>,
   * new: <boolean>,
   * fields: <document>,
   * upsert: <boolean>,
   * bypassDocumentValidation: <boolean>,
   * writeConcern: <document>
   * }
   */
  private JsonObject createFindAndModify(String collection, JsonObject updateCommand) {
    JsonObject retOb = new JsonObject();
    retOb.put("findAndModify", collection);
    retOb.put("query", this.referenceQuery);
    retOb.put("update", updateCommand);
    retOb.put("new", true);
    return retOb;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#shutdown(io.vertx.core.Handler)
   */
  @Override
  public void shutdown(Handler<AsyncResult<Void>> handler) {
    if (exe != null) {
      exe.stop();
      exe = null;
    }
    super.shutdown(handler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.braintags.io.vertx.keygenerator.IKeyGenerator#createDefaultProperties()
   */
  @Override
  public Properties createDefaultProperties() {
    Properties props = new Properties();
    props.put(CONNECTION_STRING_PROPERTY, DEFAULT_CONNECTION);
    props.put(START_MONGO_LOCAL_PROP, "false");
    props.put(LOCAL_PORT_PROP, "27017");
    props.put(SHARED_PROP, "false");
    props.put(DBNAME_PROP, DEFAULT_DB_NAME);
    return props;
  }

}
