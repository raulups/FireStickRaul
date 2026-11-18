package com.raularribas.firelauncher.presentation.home;

import com.raularribas.firelauncher.domain.repository.AppsRepository;
import com.raularribas.firelauncher.domain.repository.FeaturedRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<AppsRepository> appsRepositoryProvider;

  private final Provider<FeaturedRepository> featuredRepositoryProvider;

  public HomeViewModel_Factory(Provider<AppsRepository> appsRepositoryProvider,
      Provider<FeaturedRepository> featuredRepositoryProvider) {
    this.appsRepositoryProvider = appsRepositoryProvider;
    this.featuredRepositoryProvider = featuredRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(appsRepositoryProvider.get(), featuredRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      javax.inject.Provider<AppsRepository> appsRepositoryProvider,
      javax.inject.Provider<FeaturedRepository> featuredRepositoryProvider) {
    return new HomeViewModel_Factory(Providers.asDaggerProvider(appsRepositoryProvider), Providers.asDaggerProvider(featuredRepositoryProvider));
  }

  public static HomeViewModel_Factory create(Provider<AppsRepository> appsRepositoryProvider,
      Provider<FeaturedRepository> featuredRepositoryProvider) {
    return new HomeViewModel_Factory(appsRepositoryProvider, featuredRepositoryProvider);
  }

  public static HomeViewModel newInstance(AppsRepository appsRepository,
      FeaturedRepository featuredRepository) {
    return new HomeViewModel(appsRepository, featuredRepository);
  }
}
