package com.raularribas.firelauncher.data.apps;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppsRepositoryImpl_Factory implements Factory<AppsRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<AppOrderStore> orderStoreProvider;

  public AppsRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<AppOrderStore> orderStoreProvider) {
    this.contextProvider = contextProvider;
    this.orderStoreProvider = orderStoreProvider;
  }

  @Override
  public AppsRepositoryImpl get() {
    return newInstance(contextProvider.get(), orderStoreProvider.get());
  }

  public static AppsRepositoryImpl_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<AppOrderStore> orderStoreProvider) {
    return new AppsRepositoryImpl_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(orderStoreProvider));
  }

  public static AppsRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<AppOrderStore> orderStoreProvider) {
    return new AppsRepositoryImpl_Factory(contextProvider, orderStoreProvider);
  }

  public static AppsRepositoryImpl newInstance(Context context, AppOrderStore orderStore) {
    return new AppsRepositoryImpl(context, orderStore);
  }
}
