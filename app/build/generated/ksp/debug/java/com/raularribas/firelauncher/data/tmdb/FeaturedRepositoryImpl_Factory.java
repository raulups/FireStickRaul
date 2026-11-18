package com.raularribas.firelauncher.data.tmdb;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class FeaturedRepositoryImpl_Factory implements Factory<FeaturedRepositoryImpl> {
  private final Provider<TmdbApi> apiProvider;

  public FeaturedRepositoryImpl_Factory(Provider<TmdbApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public FeaturedRepositoryImpl get() {
    return newInstance(apiProvider.get());
  }

  public static FeaturedRepositoryImpl_Factory create(javax.inject.Provider<TmdbApi> apiProvider) {
    return new FeaturedRepositoryImpl_Factory(Providers.asDaggerProvider(apiProvider));
  }

  public static FeaturedRepositoryImpl_Factory create(Provider<TmdbApi> apiProvider) {
    return new FeaturedRepositoryImpl_Factory(apiProvider);
  }

  public static FeaturedRepositoryImpl newInstance(TmdbApi api) {
    return new FeaturedRepositoryImpl(api);
  }
}
