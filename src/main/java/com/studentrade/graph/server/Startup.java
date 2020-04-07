package com.studentrade.graph.server;

import com.google.inject.Inject;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.studentrade.graph.server.io.AppEntrypoint;
import com.studentrade.graph.server.io.EntryPointType;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Singleton
class Startup {

	private static final String ARCHAIUS_APP_PROPERTY = "archaius.deployment.applicationId";
	private static final DynamicStringProperty APP_NAME = DynamicPropertyFactory.getInstance()
			.getStringProperty(ARCHAIUS_APP_PROPERTY, "graph");

    @Inject(optional = true)
    private Map<EntryPointType, AppEntrypoint> entrypoints = Collections.emptyMap();

    void boot(EntryPointType entrypointType, String[] args) throws IOException {
		loadArchaiusProperties();

		Optional<AppEntrypoint> appEntrypoint = Optional.ofNullable(entrypoints.get(entrypointType));
		appEntrypoint.orElseThrow(() -> new RuntimeException("Entrypoint not defined")).boot(args);
    }

    private void loadArchaiusProperties() throws IOException {
		ConfigurationManager.loadCascadedPropertiesFromResources(APP_NAME.get());
	}
}
