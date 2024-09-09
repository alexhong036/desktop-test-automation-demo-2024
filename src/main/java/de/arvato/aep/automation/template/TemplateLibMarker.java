package de.arvato.aep.automation.template;

// TODO Remove `template` package and replace it according to architecture guideline

import de.arvato.aep.automation.config.AepSdkConfig;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

// Without any basepackages the package of this class will be used by spring
@ConfigurationPropertiesScan // scan for configurations in this and sub-packages
@ComponentScan(
    basePackageClasses = {
        // as we need the AepSdkConfig for TestExecInfo we also need set this package,
        // otherwise spring would just scan the package of the AepSdkConfig
        TemplateLibMarker.class,
        // Here we can get the TestExecInfo-Bean
        AepSdkConfig.class
    }
) // scan for Components (like our ManagedBean annotated keyword-class) in this and sub-packages
public class TemplateLibMarker {
}
