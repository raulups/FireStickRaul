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
public final class AppOrderStore_Factory implements Factory<AppOrderStore> {
  private final Provider<Context> contextProvider;

  public AppOrderStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppOrderStore get() {
    return newInstance(contextProvider.get());
  }

  public static AppOrderStore_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new AppOrderStore_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static AppOrderStore_Factory create(Provider<Context> contextProvider) {
    return new AppOrderStore_Factory(contextProvider);
  }

  public static AppOrderStore newInstance(Context context) {
    return new AppOrderStore(context);
  }
}
