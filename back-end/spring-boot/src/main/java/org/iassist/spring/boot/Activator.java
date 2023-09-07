package org.iassist.spring.boot;

import java.util.Arrays;
import java.util.Hashtable;

import java.util.List;
import org.iassist.spring.boot.api.AppService;
import org.iassist.spring.boot.mqtt.MqttSubscriber;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import org.iassist.spring.boot.api.impl.Service;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableWebSocket
public class Activator implements BundleActivator {

    private static BundleContext bundleContext;
    private ConfigurableApplicationContext appContext;
    
    private List<String> allTopics = Arrays.asList(
            "zigbee2mqtt/Tuya Temperature Sensor",
            "zigbee2mqtt/Sonoff Contact Sensor",
            "zigbee2mqtt/Sonoff Motion Sensor");

    @Autowired
    private MqttSubscriber mqttSubscriber;

    @Override
    public void start(BundleContext context) {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        // Start the Spring Boot application context
        appContext = SpringApplication.run(Activator.class);
        bundleContext = context;

        // Register OSGi services
        registerServices();

        mqttSubscriber.configureMqttClient();
        mqttSubscriber.connectMqttClient();
        mqttSubscriber.subscribeToTopics(allTopics);
    }

    @Override
    public void stop(BundleContext context) {
        bundleContext = null;
        // Close the Spring Boot application context
        SpringApplication.exit(appContext, () -> 0);
    }
    private void registerServices() {
        AppService service = new Service();
        bundleContext.registerService(AppService.class.getName(), service, new Hashtable<>());
        System.out.println("Service registered: " + service.name());
    }

    public static void main(String[] args) {
        // Only use this main method if running outside OSGi
        ConfigurableApplicationContext appContext = SpringApplication.run(Activator.class, args);
        MqttSubscriber mqttSubscriber = appContext.getBean(MqttSubscriber.class);

        mqttSubscriber.configureMqttClient();
        mqttSubscriber.connectMqttClient();

        List<String> allTopics = Arrays.asList(
                "zigbee2mqtt/Tuya Temperature Sensor",
                "zigbee2mqtt/Sonoff Contact Sensor",
                "zigbee2mqtt/Sonoff Motion Sensor");
        mqttSubscriber.subscribeToTopics(allTopics);
    }

    /*@Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }*/
}
