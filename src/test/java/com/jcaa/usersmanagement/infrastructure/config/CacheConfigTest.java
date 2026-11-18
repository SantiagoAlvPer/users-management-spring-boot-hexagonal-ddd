package com.jcaa.usersmanagement.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("CacheConfig")
class CacheConfigTest {

  @Test
  @DisplayName("Returns NoOpCacheManager when cache.type is none")
  void shouldReturnNoOpCacheManagerWhenCacheTypeIsNone() {
    // Arrange
    final CacheConfig cacheConfig = new CacheConfig();
    ReflectionTestUtils.setField(cacheConfig, "cacheType", "none");
    ReflectionTestUtils.setField(cacheConfig, "cacheUserTtlSeconds", 300L);

    @SuppressWarnings("unchecked")
    final ObjectProvider<RedisConnectionFactory> provider = mock(ObjectProvider.class);

    // Act
    final CacheManager cacheManager = cacheConfig.cacheManager(provider);

    // Assert
    assertThat(cacheManager).isInstanceOf(NoOpCacheManager.class);
  }

  @Test
  @DisplayName("Returns NoOpCacheManager when cache.type is valkey but connection factory is null")
  void shouldFallbackToNoOpWhenConnectionFactoryIsNull() {
    // Arrange
    final CacheConfig cacheConfig = new CacheConfig();
    ReflectionTestUtils.setField(cacheConfig, "cacheType", "valkey");
    ReflectionTestUtils.setField(cacheConfig, "cacheUserTtlSeconds", 300L);

    @SuppressWarnings("unchecked")
    final ObjectProvider<RedisConnectionFactory> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(null);

    // Act
    final CacheManager cacheManager = cacheConfig.cacheManager(provider);

    // Assert
    assertThat(cacheManager).isInstanceOf(NoOpCacheManager.class);
  }

  @Test
  @DisplayName("Returns RedisCacheManager when cache.type is valkey and connection factory is present")
  void shouldReturnRedisCacheManagerWhenCacheTypeIsValkey() {
    // Arrange
    final CacheConfig cacheConfig = new CacheConfig();
    ReflectionTestUtils.setField(cacheConfig, "cacheType", "valkey");
    ReflectionTestUtils.setField(cacheConfig, "cacheUserTtlSeconds", 300L);

    @SuppressWarnings("unchecked")
    final ObjectProvider<RedisConnectionFactory> provider = mock(ObjectProvider.class);
    final RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
    when(provider.getIfAvailable()).thenReturn(connectionFactory);

    // Act
    final CacheManager cacheManager = cacheConfig.cacheManager(provider);

    // Assert
    assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);
  }

  @Test
  @DisplayName("ErrorHandler suppresses cache exceptions without rethrowing (fault tolerance)")
  void shouldSuppressCacheExceptionsInErrorHandler() {
    // Arrange
    final CacheConfig cacheConfig = new CacheConfig();
    final CacheErrorHandler errorHandler = cacheConfig.errorHandler();
    final Cache cache = mock(Cache.class);
    when(cache.getName()).thenReturn("user");
    final RuntimeException error = new RuntimeException("Valkey connection timeout");

    // Act & Assert
    assertDoesNotThrow(() -> errorHandler.handleCacheGetError(error, cache, "user-123"));
    assertDoesNotThrow(() -> errorHandler.handleCachePutError(error, cache, "user-123", "value"));
    assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(error, cache, "user-123"));
    assertDoesNotThrow(() -> errorHandler.handleCacheClearError(error, cache));
  }
}
