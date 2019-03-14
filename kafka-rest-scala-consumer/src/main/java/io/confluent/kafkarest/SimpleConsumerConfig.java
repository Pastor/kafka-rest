/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.kafkarest;

import io.confluent.common.config.ConfigDef;
import io.confluent.common.config.ConfigDef.Importance;
import io.confluent.common.config.ConfigDef.Type;
import io.confluent.kafkarest.entities.ConsumerInstanceConfig;
import io.confluent.rest.RestConfig;
import io.confluent.rest.RestConfigException;
import kafka.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static io.confluent.common.config.ConfigDef.Range.atLeast;

public class SimpleConsumerConfig extends RestConfig {

  private static final Logger log = LoggerFactory.getLogger(SimpleConsumerConfig.class);

  public static final String ID_CONFIG = "id";
  private static final String ID_CONFIG_DOC =
      "Unique ID for this REST server instance. This is used in generating unique IDs for "
      + "consumers that do "
      + "not specify their ID. The ID is empty by default, which makes a single server setup "
      + "easier to "
      + "get up and running, but is not safe for multi-server deployments where automatic "
      + "consumer IDs "
      + "are used.";
  public static final String ID_DEFAULT = "";

  public static final String CONSUMER_MAX_THREADS_CONFIG = "consumer.threads";
  private static final String CONSUMER_MAX_THREADS_DOC =
      "The maximum number of threads to run consumer requests on."
      + " The value of -1 denotes unbounded thread creation";
  public static final String CONSUMER_MAX_THREADS_DEFAULT = "50";

  public static final String ZOOKEEPER_CONNECT_CONFIG = "zookeeper.connect";
  private static final String ZOOKEEPER_CONNECT_DOC =
      "NOTE: Only required when using v1 Consumer API's. Specifies the ZooKeeper connection "
      + "string in the form "
      + "hostname:port where host and port are the host and port of a ZooKeeper server. To allow "
      + "connecting "
      + "through other ZooKeeper nodes when that ZooKeeper machine is down you can also specify "
      + "multiple hosts "
      + "in the form hostname1:port1,hostname2:port2,hostname3:port3.\n"
      + "\n"
      + "The server may also have a ZooKeeper chroot path as part of it's ZooKeeper connection "
      + "string which puts "
      + "its data under some path in the global ZooKeeper namespace. If so the consumer should "
      + "use the same "
      + "chroot path in its connection string. For example to give a chroot path of /chroot/path "
      + "you would give "
      + "the connection string as hostname1:port1,hostname2:port2,hostname3:port3/chroot/path. ";
  public static final String ZOOKEEPER_CONNECT_DEFAULT = "";

  public static final String SCHEMA_REGISTRY_URL_CONFIG = "schema.registry.url";
  private static final String SCHEMA_REGISTRY_URL_DOC =
      "The base URL for the schema registry that should be used by the Avro serializer.";
  private static final String SCHEMA_REGISTRY_URL_DEFAULT = "http://localhost:8081";

  public static final String PROXY_FETCH_MIN_BYTES_CONFIG =
      ConsumerInstanceConfig.PROXY_FETCH_MIN_BYTES_CONFIG;
  private static final String PROXY_FETCH_MIN_BYTES_DOC =
          "Minimum bytes of records for the proxy to accumulate before"
          + "returning a response to a consumer request. "
          + "The special sentinel value of -1 disables this functionality.";
  private static final String PROXY_FETCH_MIN_BYTES_DEFAULT = "-1";
  public static final ConfigDef.Range PROXY_FETCH_MIN_BYTES_VALIDATOR =
      ConsumerInstanceConfig.PROXY_FETCH_MIN_BYTES_VALIDATOR;

  public static final String CONSUMER_ITERATOR_TIMEOUT_MS_CONFIG = "consumer.iterator.timeout.ms";
  private static final String CONSUMER_ITERATOR_TIMEOUT_MS_DOC =
      "Timeout for blocking consumer iterator operations. "
      + "This should be set to a small enough value that it is possible to effectively peek() on "
      + "the iterator.";
  public static final String CONSUMER_ITERATOR_TIMEOUT_MS_DEFAULT = "1";

  public static final String CONSUMER_ITERATOR_BACKOFF_MS_CONFIG = "consumer.iterator.backoff.ms";
  private static final String CONSUMER_ITERATOR_BACKOFF_MS_DOC =
      "Amount of time to backoff when an iterator runs "
      + "out of data. If a consumer has a dedicated worker thread, this is effectively the "
      + "maximum error for the "
      + "entire request timeout. It should be small enough to closely target the timeout, but "
      + "large enough to "
      + "avoid busy waiting.";
  public static final String CONSUMER_ITERATOR_BACKOFF_MS_DEFAULT = "50";

  public static final String CONSUMER_REQUEST_TIMEOUT_MS_CONFIG = "consumer.request.timeout.ms";
  private static final String CONSUMER_REQUEST_TIMEOUT_MS_DOC =
      "The maximum total time to wait for messages for a "
      + "request if the maximum number of messages has not yet been reached.";
  public static final String CONSUMER_REQUEST_TIMEOUT_MS_DEFAULT = "1000";

  public static final String CONSUMER_REQUEST_MAX_BYTES_CONFIG = "consumer.request.max.bytes";
  private static final String CONSUMER_REQUEST_MAX_BYTES_DOC =
      "Maximum number of bytes in unencoded message keys and values returned "
      + "by a single request. This can be used by administrators to limit the memory used "
      + "by a single consumer and to control the memory usage required to decode responses"
      + "on clients that cannot perform a streaming decode. "
      + "Note that the actual payload will be larger due to overhead from base64 encoding the "
      + "response data and from JSON encoding the entire response.";
  public static final long CONSUMER_REQUEST_MAX_BYTES_DEFAULT = 64 * 1024 * 1024;

  public static final String CONSUMER_INSTANCE_TIMEOUT_MS_CONFIG = "consumer.instance.timeout.ms";
  private static final String CONSUMER_INSTANCE_TIMEOUT_MS_DOC =
      "Amount of idle time before a consumer instance "
      + "is automatically destroyed.";
  public static final String CONSUMER_INSTANCE_TIMEOUT_MS_DEFAULT = "300000";

  public static final String SIMPLE_CONSUMER_MAX_POOL_SIZE_CONFIG = "simpleconsumer.pool.size.max";
  private static final String SIMPLE_CONSUMER_MAX_POOL_SIZE_DOC =
      "Maximum number of SimpleConsumers that can be instantiated per broker."
      + " If 0, then the pool size is not limited.";
  public static final String SIMPLE_CONSUMER_MAX_POOL_SIZE_DEFAULT = "25";

  public static final String SIMPLE_CONSUMER_POOL_TIMEOUT_MS_CONFIG =
      "simpleconsumer.pool.timeout.ms";
  private static final String SIMPLE_CONSUMER_POOL_TIMEOUT_MS_DOC =
      "Amount of time to wait for an available SimpleConsumer from the pool before failing."
      + " Use 0 for no timeout";
  public static final String SIMPLE_CONSUMER_POOL_TIMEOUT_MS_DEFAULT = "1000";

  public static final String KAFKACLIENT_ZK_SESSION_TIMEOUT_MS_CONFIG
      = "client.zk.session.timeout.ms";

  protected static final String KAFKACLIENT_ZK_SESSION_TIMEOUT_MS_DOC =
      "Zookeeper session timeout";

  private static final ConfigDef config;
  private final ConsumerConfig consumerConfig;

  static {
    config = baseKafkaRestConfigDef();
  }

  protected static ConfigDef baseKafkaRestConfigDef() {
    return baseConfigDef()
        .define(ID_CONFIG, Type.STRING, ID_DEFAULT, Importance.HIGH, ID_CONFIG_DOC)
        .define(CONSUMER_MAX_THREADS_CONFIG, Type.INT, CONSUMER_MAX_THREADS_DEFAULT,
                Importance.MEDIUM, CONSUMER_MAX_THREADS_DOC)
        .define(
            ZOOKEEPER_CONNECT_CONFIG,
            Type.STRING,
            ZOOKEEPER_CONNECT_DEFAULT,
            Importance.HIGH,
            ZOOKEEPER_CONNECT_DOC
        )
        .define(
            SCHEMA_REGISTRY_URL_CONFIG,
            Type.STRING,
            SCHEMA_REGISTRY_URL_DEFAULT,
            Importance.HIGH,
            SCHEMA_REGISTRY_URL_DOC
        )
        .define(
            PROXY_FETCH_MIN_BYTES_CONFIG,
            Type.INT,
            PROXY_FETCH_MIN_BYTES_DEFAULT,
                PROXY_FETCH_MIN_BYTES_VALIDATOR,
            Importance.LOW,
            PROXY_FETCH_MIN_BYTES_DOC
        )
        .define(
            CONSUMER_ITERATOR_TIMEOUT_MS_CONFIG,
            Type.INT,
            CONSUMER_ITERATOR_TIMEOUT_MS_DEFAULT,
            Importance.LOW,
            CONSUMER_ITERATOR_TIMEOUT_MS_DOC
        )
        .define(
            CONSUMER_ITERATOR_BACKOFF_MS_CONFIG,
            Type.INT,
            CONSUMER_ITERATOR_BACKOFF_MS_DEFAULT,
            Importance.LOW,
            CONSUMER_ITERATOR_BACKOFF_MS_DOC
        )
        .define(
            CONSUMER_REQUEST_TIMEOUT_MS_CONFIG,
            Type.INT,
            CONSUMER_REQUEST_TIMEOUT_MS_DEFAULT,
            Importance.MEDIUM,
            CONSUMER_REQUEST_TIMEOUT_MS_DOC
        )
        .define(
            CONSUMER_REQUEST_MAX_BYTES_CONFIG,
            Type.LONG,
            CONSUMER_REQUEST_MAX_BYTES_DEFAULT,
            Importance.MEDIUM,
            CONSUMER_REQUEST_MAX_BYTES_DOC
        )
        .define(
            CONSUMER_INSTANCE_TIMEOUT_MS_CONFIG,
            Type.INT,
            CONSUMER_INSTANCE_TIMEOUT_MS_DEFAULT,
            Importance.LOW,
            CONSUMER_INSTANCE_TIMEOUT_MS_DOC
        )
        .define(
            SIMPLE_CONSUMER_MAX_POOL_SIZE_CONFIG,
            Type.INT,
            SIMPLE_CONSUMER_MAX_POOL_SIZE_DEFAULT,
            Importance.MEDIUM,
            SIMPLE_CONSUMER_MAX_POOL_SIZE_DOC
        )
        .define(
            SIMPLE_CONSUMER_POOL_TIMEOUT_MS_CONFIG,
            Type.INT,
            SIMPLE_CONSUMER_POOL_TIMEOUT_MS_DEFAULT,
            Importance.LOW,
            SIMPLE_CONSUMER_POOL_TIMEOUT_MS_DOC
        )
        .define(
            KAFKACLIENT_ZK_SESSION_TIMEOUT_MS_CONFIG,
            Type.INT,
            30000,
            atLeast(0),
            Importance.LOW,
            KAFKACLIENT_ZK_SESSION_TIMEOUT_MS_DOC
        );
  }

  private Time time;
  private Properties originalProperties;

  public SimpleConsumerConfig(Properties props, Time time) throws RestConfigException {
    this(config, props, time);
  }

  public SimpleConsumerConfig(ConfigDef configDef, Properties props, Time time) {
    super(configDef, props);
    this.originalProperties = props;
    this.time = time;

    final Properties consumerProps = (Properties) originalProperties.clone();
    // ConsumerConfig is intended to be used with the HighLevelConsumer. Therefore, it requires
    // some properties
    // to be instantiated that are useless for a SimpleConsumer.
    // We use ConsumerConfig as a basis for SimpleConsumerConfig, because it contains
    // sensible defaults (buffer size, ...).
    consumerProps.setProperty("zookeeper.connect", "");
    consumerProps.setProperty("group.id", "");
    consumerConfig = new ConsumerConfig(consumerProps);
  }

  public Time getTime() {
    return time;
  }

  public Properties getOriginalProperties() {
    return originalProperties;
  }

  public int socketTimeoutMs() {
    return consumerConfig.socketTimeoutMs();
  }

  public int socketReceiveBufferBytes() {
    return consumerConfig.socketReceiveBufferBytes();
  }

  public int fetchMessageMaxBytes() {
    return consumerConfig.fetchMessageMaxBytes();
  }

  public int fetchWaitMaxMs() {
    return consumerConfig.fetchWaitMaxMs();
  }

  public int fetchMinBytes() {
    return consumerConfig.fetchMinBytes();
  }

  public static void main(String[] args) {
    System.out.print(config.toRst());
  }
}
