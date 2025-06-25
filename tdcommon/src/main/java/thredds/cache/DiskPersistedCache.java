/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.cache;

import com.google.common.cache.CacheBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Formatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.serializer.collections.lazy.LazyHashMap;
import org.eclipse.serializer.concurrency.XThreads;
import org.eclipse.serializer.persistence.binary.jdk17.types.BinaryHandlersJDK17;
import org.eclipse.serializer.persistence.binary.jdk8.types.BinaryHandlersJDK8;
import org.eclipse.store.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageFoundation;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A two-tier key-value disk persisted cache
 * <p>
 * This cache is composed of a smaller in-memory LRU cache (level 1) and a disk-based storage layer for persistence
 * (level 2). The level 1 cache 1 cache is item-limited, while the eclipse storage layer is not limited. However, values
 * in the level 2 cache will be replaced with lazy references after 30 seconds. In this sense, the level 2 cache is only
 * used for persistence and loading persisted values into the level 1 cache.
 * <p>
 * When entries are added to the cache, the entry is added to both the level 1 level 2 caches. If an entry already
 * exists in either cache, it will be replaced.
 * <p>
 * When keys are requested from the cache, the level 1 cache is first checked. If the level 1 cache does not contain an
 * entry for the key, the level 2 cache is checked. If the key is not present in the level 2 cache, a null value is
 * returned. If the key exists, the value will be lazily loaded from the EclipseStorage layer, placed into the level 1
 * cache, and returned to the caller.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class DiskPersistedCache<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(DiskPersistedCache.class);

  private final EmbeddedStorageManager storageManager;
  private final com.google.common.cache.Cache<K, V> level1Cache;
  private final LazyHashMap<K, V> level2Cache;

  private DiskPersistedCache(Builder<K, V> builder) {
    this.storageManager = builder.storageManager;
    this.level2Cache = builder.level2Cache;
    this.level1Cache = builder.level1Cache;
  }

  /**
   * DiskPersistedCache Builder
   * <p>
   *
   * @param cacheDir location of the disk persisted cache
   * @param <K> cache key
   * @param <V> cache value
   * @return a DiskPersistedCache builder
   * @throws IOException if an i/o issue related to {@code cacheDir} is encountered
   */
  public static <K, V> Builder<K, V> at(Path cacheDir) throws IOException {
    return new Builder<>(cacheDir);
  }

  public static class Builder<T1, T2> {

    // When lazy loading an entity from the EclipseStore, all entities in a segment
    // are loaded into the internal EclipseStore cache. This sets the number of
    // Entities in an individual segment. The default is 1,000, which seems large
    // given how we use the storage layer to feed the L1 cache. This value can be
    // revisited as needed, and possibly exposed as a configuration in the future.
    private final int entitiesPerSegment = 10;
    private final LazyHashMap<T1, T2> level2Cache = new LazyHashMap<>(entitiesPerSegment);
    private final Path cacheDir;

    private EmbeddedStorageManager storageManager;
    private com.google.common.cache.Cache<T1, T2> level1Cache;
    private long maximumL1CacheEntities = 1_000;
    private String name;

    private Builder(Path cacheDir) throws IOException {
      // validate
      if (Files.exists(cacheDir)) {
        if (!Files.isDirectory(cacheDir)) {
          throw new IOException(String.format("cacheDir %s is not a directory", cacheDir));
        }
        if (!Files.isReadable(cacheDir)) {
          throw new IOException(String.format("cacheDir %s is not readable", cacheDir));
        }
        if (!Files.isWritable(cacheDir)) {
          throw new IOException(String.format("cacheDir %s is not writable", cacheDir));
        }
      }

      this.cacheDir = cacheDir.toAbsolutePath();
      this.name = this.cacheDir.getFileName().toString();
    }

    /**
     * Set the name of the cache
     * <p>
     *
     * @param cacheName name of the cache
     * @return builder
     */
    public Builder<T1, T2> named(String cacheName) {
      this.name = cacheName;
      return this;
    }

    /**
     * Set the maximum number of entities in the level 1 cache
     * <p>
     *
     * @param maximumMemoryCacheEntities maximum number of entities before eviction from the level 1 LRU cache
     * @return builder
     */
    public Builder<T1, T2> maxInMemoryEntities(long maximumMemoryCacheEntities) {
      this.maximumL1CacheEntities = maximumMemoryCacheEntities;
      return this;
    }

    public DiskPersistedCache<T1, T2> build() throws IOException {
      if (!Files.exists(this.cacheDir)) {
        logger.info("creating cache directory at {}", cacheDir);
        Files.createDirectories(this.cacheDir);
      }

      // basic storage config
      final Duration lazyRefTimeout = Duration.ofSeconds(30);
      final EmbeddedStorageFoundation<?> foundation = EmbeddedStorageConfiguration.Builder()
          .setStorageDirectory(cacheDir.toAbsolutePath().toString()).setEntityCacheTimeout(lazyRefTimeout)
          .createEmbeddedStorageFoundation().setDataBaseName(name).setRoot(level2Cache);

      foundation.onConnectionFoundation(BinaryHandlersJDK8::registerJDK8TypeHandlers);
      foundation.onConnectionFoundation(BinaryHandlersJDK17::registerJDK17TypeHandlers);

      storageManager = foundation.start();
      logger.info("{} cache stored at {}", storageManager.databaseName(), cacheDir);

      this.level1Cache = CacheBuilder.newBuilder().maximumSize(maximumL1CacheEntities).build();
      return new DiskPersistedCache<>(this);
    }
  }

  public V get(K key) {
    V val = level1Cache.getIfPresent(key);
    if (val == null) {
      logger.debug("{} L1 miss", storageManager.databaseName());
      val = level2Cache.get(key);
      if (val != null) {
        logger.debug("{} L2 cache hit, update L1 cache", storageManager.databaseName());
        level1Cache.put(key, val);
      } else {
        logger.debug("{} L2 cache miss", storageManager.databaseName());
      }
    } else {
      logger.debug("{} L1 cache hit", storageManager.databaseName());
    }
    if (val == null) {
      logger.debug("{} key {} not in cache", storageManager.databaseName(), key);
    }
    return val;
  }

  public V getOrDefault(K key, V defaultValue) {
    V val = get(key);
    return val == null ? defaultValue : val;
  }

  public void put(K key, V val) {
    XThreads.executeSynchronized(() -> {
      logger.debug("{} adding value to L2 cache", storageManager.databaseName());
      level2Cache.put(key, val);
      storageManager.store(level2Cache);

      logger.debug("{} updating L1 cache", storageManager.databaseName());
      level1Cache.put(key, val);
    });
  }

  public void remove(K key) {
    XThreads.executeSynchronized(() -> {
      level2Cache.remove(key);
      storageManager.store(level2Cache);
      level1Cache.invalidate(key);
    });
  }

  public long numL1Keys() {
    return level1Cache.size();
  }

  public long numL2Keys() {
    return level2Cache.size();
  }

  /**
   * remove all cached entities from both the level 1 and level 2 caches
   */
  public void reinit() {
    if (storageManager.isRunning()) {
      level2Cache.clear();
      cleanupL2Storage();
    } else {
      logger.warn("{} is not running, cannot reinit l2 cache", storageManager.databaseName());
    }
    level1Cache.invalidateAll();
  }

  public String name() {
    return storageManager.databaseName();
  }

  public boolean running() {
    return storageManager.isRunning();
  }

  public void showL1Db(Formatter f) {
    long count = 0;
    for (Map.Entry<K, V> entry : level1Cache.asMap().entrySet()) {
      f.format("%4d: '%s' == %s%n", count++, entry.getKey(), entry.getValue());
    }
  }

  public void showL2Db(Formatter f, int skip) {
    if (skip < 1) {
      skip = 1;
    }

    final int peekSkip = skip;

    AtomicLong count = new AtomicLong();
    count.set(-1);

    for (Map.Entry<K, V> entry : level2Cache.entrySet()) {
      long itemNum = count.incrementAndGet();
      if (itemNum % peekSkip == 0) {
        f.format("%4d: '%s' == %s%n", itemNum, entry.getKey(), entry.getValue());
      }
    }
  }

  public void shutdown() {
    logger.info("shutting down {} cache...", storageManager.databaseName());
    storageManager.shutdown();
    logger.info("{} cache shutdown", storageManager.databaseName());
  }

  // package private for testing, but used by reinit
  void cleanupL2Storage() {
    storageManager.issueFullCacheCheck();
    storageManager.issueFullFileCheck();
  }

  // test only methods
  V getL1Cache(K key) {
    return level1Cache.getIfPresent(key);
  }

  V getL2Cache(K key) {
    return level2Cache.get(key);
  }
}
