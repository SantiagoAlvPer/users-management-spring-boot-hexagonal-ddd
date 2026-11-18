package com.jcaa.usersmanagement.infrastructure.config;

import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig implements CachingConfigurer {

  private static final String PROP_CACHE_TYPE = "${cache.type:none}";
  private static final String PROP_CACHE_TTL = "${cache.user.ttl-seconds:300}";
  private static final String CACHE_TYPE_VALKEY = "valkey";
  private static final String KEY_PREFIX_SEPARATOR = ":";

  private static final String LOG_CACHE_INIT_VALKEY =
      "[CacheConfig] Inicializando cache con Valkey (TTL={}s).";
  private static final String LOG_CACHE_INIT_NOOP =
      "[CacheConfig] Cache desactivada o tipo 'none' configurado (NoOpCacheManager activo).";
  private static final String LOG_CACHE_CONN_NOT_FOUND =
      "[CacheConfig] RedisConnectionFactory no disponible. Fallback a NoOpCacheManager.";
  private static final String LOG_CACHE_GET_ERROR =
      "[CacheConfig] Error al leer de cache '{}' con clave '{}'. Fallback a BD: {}";
  private static final String LOG_CACHE_PUT_ERROR =
      "[CacheConfig] Error al escribir en cache '{}' con clave '{}': {}";
  private static final String LOG_CACHE_EVICT_ERROR =
      "[CacheConfig] Error al invalidar cache '{}' con clave '{}': {}";
  private static final String LOG_CACHE_CLEAR_ERROR =
      "[CacheConfig] Error al limpiar cache '{}': {}";

  @Value(PROP_CACHE_TYPE)
  private String cacheType;

  @Value(PROP_CACHE_TTL)
  private long cacheUserTtlSeconds;

  @Bean
  public CacheManager cacheManager(
      final ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
    if (CACHE_TYPE_VALKEY.equalsIgnoreCase(cacheType)) {
      final RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
      if (Objects.isNull(connectionFactory)) {
        log.warn(LOG_CACHE_CONN_NOT_FOUND);
        return new NoOpCacheManager();
      }

      log.info(LOG_CACHE_INIT_VALKEY, cacheUserTtlSeconds);

      final RedisCacheConfiguration redisCacheConfiguration =
          RedisCacheConfiguration.defaultCacheConfig()
              .entryTtl(Duration.ofSeconds(cacheUserTtlSeconds))
              .disableCachingNullValues()
              .computePrefixWith(cacheName -> cacheName + KEY_PREFIX_SEPARATOR)
              .serializeKeysWith(
                  RedisSerializationContext.SerializationPair.fromSerializer(
                      new StringRedisSerializer()))
              .serializeValuesWith(
                  RedisSerializationContext.SerializationPair.fromSerializer(
                      new GenericJackson2JsonRedisSerializer()));

      return RedisCacheManager.builder(connectionFactory)
          .cacheDefaults(redisCacheConfiguration)
          .build();
    }

    log.info(LOG_CACHE_INIT_NOOP);
    return new NoOpCacheManager();
  }

  @Bean
  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(
          final RuntimeException exception, final Cache cache, final Object key) {
        log.warn(LOG_CACHE_GET_ERROR, cache.getName(), key, exception.getMessage());
      }

      @Override
      public void handleCachePutError(
          final RuntimeException exception,
          final Cache cache,
          final Object key,
          final Object value) {
        log.warn(LOG_CACHE_PUT_ERROR, cache.getName(), key, exception.getMessage());
      }

      @Override
      public void handleCacheEvictError(
          final RuntimeException exception, final Cache cache, final Object key) {
        log.warn(LOG_CACHE_EVICT_ERROR, cache.getName(), key, exception.getMessage());
      }

      @Override
      public void handleCacheClearError(final RuntimeException exception, final Cache cache) {
        log.warn(LOG_CACHE_CLEAR_ERROR, cache.getName(), exception.getMessage());
      }
    };
  }
}
