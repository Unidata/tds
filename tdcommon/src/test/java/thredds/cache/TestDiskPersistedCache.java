/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.cache;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.store.storage.exceptions.StorageExceptionNotRunning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import thredds.cache.DiskPersistedCache.Builder;

public class TestDiskPersistedCache {

  @Rule
  public final TemporaryFolder cacheDir = TemporaryFolder.builder().assureDeletion().build();

  private Path cacheDir() {
    return cacheDir.getRoot().toPath();
  }

  @Test
  public void testSimpleShutdown() throws IOException {
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    assertThat(dpc.running()).isTrue();
    dpc.shutdown();
    assertThat(dpc.running()).isFalse();
    assertThrows(StorageExceptionNotRunning.class, () -> dpc.put(1, "hello"));
  }

  @Test
  public void testSimpleShutdown2() throws IOException {
    Builder<String, byte[]> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<String, byte[]> dpc = builder.build();
    assertThat(dpc.running()).isTrue();
    dpc.shutdown();
    assertThat(dpc.running()).isFalse();
  }

  @Test
  public void testPersistence() throws IOException {
    String key = "myKey";
    char[] value = key.toCharArray();
    Builder<String, char[]> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<String, char[]> dpc = builder.build();
    dpc.put(key, value);
    dpc.shutdown();
    assertThat(dpc.running()).isFalse();
    dpc = builder.build();
    assertThat(dpc.running()).isTrue();
    value = dpc.get(key);
    assertThat(value).isEqualTo(value);
    dpc.shutdown();
  }

  @Test
  public void testPersistedGetPullThrough() throws IOException {
    final int key = 1;
    final String value = "hi";
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    dpc.put(key, value);
    dpc.shutdown();
    assertThat(dpc.running()).isFalse();
    dpc = builder.build();
    // on startup, the cached value should only exist in the level 2 cache
    assertThat(dpc.running()).isTrue();
    assertThat(dpc.getL2Cache(key)).isEqualTo(value);
    assertThat(dpc.getL1Cache(key)).isNull();
    // after DiskPersistedCache.get call, value should exist in both
    assertThat(dpc.get(key)).isEqualTo(value);
    assertThat(dpc.getL2Cache(key)).isEqualTo(value);
    assertThat(dpc.getL1Cache(key)).isEqualTo(value);
    dpc.shutdown();
  }

  @Test
  public void testRemove() throws IOException {
    final int key1 = 1;
    final String value1 = "hi";
    final int key2 = 2;
    final String value2 = "bye";
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    dpc.put(key1, value1);
    dpc.put(key2, value2);
    assertThat(dpc.get(key1)).isEqualTo(value1);
    assertThat(dpc.getL2Cache(key1)).isEqualTo(value1);
    assertThat(dpc.getL1Cache(key1)).isEqualTo(value1);
    assertThat(dpc.get(key2)).isEqualTo(value2);
    assertThat(dpc.getL2Cache(key2)).isEqualTo(value2);
    assertThat(dpc.getL1Cache(key2)).isEqualTo(value2);
    // remove key1
    dpc.remove(key1);
    // key1 should not be in either cache after removal, but key2 should remain
    assertThat(dpc.get(key1)).isNull();
    assertThat(dpc.getL2Cache(key1)).isNull();
    assertThat(dpc.getL1Cache(key1)).isNull();
    assertThat(dpc.get(key2)).isEqualTo(value2);
    assertThat(dpc.getL2Cache(key2)).isEqualTo(value2);
    assertThat(dpc.getL1Cache(key2)).isEqualTo(value2);
    dpc.shutdown();
  }

  @Test
  public void testMaximumInMemory() throws IOException, InterruptedException {
    final int maxInMemoryEntities = 5;
    final int maxEntities = maxInMemoryEntities * 2;
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.maxInMemoryEntities(maxInMemoryEntities).build();
    for (int i = 0; i < maxInMemoryEntities * 2; i++) {
      dpc.put(i, String.valueOf(i));
    }

    assertThat(dpc.numL2Keys()).isEqualTo(maxEntities);
    assertThat(dpc.numL1Keys()).isEqualTo(maxInMemoryEntities);

    Thread.sleep(3000);

    dpc.cleanupL2Storage();
    assertThat(dpc.numL2Keys()).isEqualTo(maxEntities);
    assertThat(dpc.numL1Keys()).isEqualTo(maxInMemoryEntities);

    dpc.shutdown();
  }

  @Test
  public void testNamedCacheDefault() throws IOException {
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    assertThat(dpc.name()).isEqualTo(cacheDir().getFileName().toString());
    dpc.shutdown();
  }

  @Test
  public void testNamedCache() throws IOException {
    final String name = "myCache";
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.named(name).build();
    assertThat(dpc.name()).isEqualTo(name);
    dpc.shutdown();
  }

  @Test
  public void testReinitNotRunning() throws IOException {
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    dpc.shutdown();
    assertThat(dpc.running()).isFalse();
    // call to make sure this does not throw a runtime error when the cache isn't running
    dpc.reinit();
  }

  @Test
  public void testReinit() throws IOException {
    final int key = 1;
    final String value = "hi";
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    assertThat(dpc.get(key)).isNull();
    dpc.put(key, value);
    assertThat(dpc.get(key)).isEqualTo(value);
    dpc.reinit();
    assertThat(dpc.get(key)).isNull();
    dpc.shutdown();
  }

  @Test
  public void testGetOrDefault() throws IOException {
    final int key = 1;
    final String value = "hi";
    final String defaultValue = "default";
    Builder<Integer, String> builder = DiskPersistedCache.at(cacheDir());
    DiskPersistedCache<Integer, String> dpc = builder.build();
    assertThat(dpc.getOrDefault(key, defaultValue)).isEqualTo(defaultValue);
    dpc.put(key, value);
    assertThat(dpc.getOrDefault(key, defaultValue)).isEqualTo(value);
    dpc.shutdown();
  }

}
