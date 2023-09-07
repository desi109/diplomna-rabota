package org.iassist.spring.boot.mqtt;

import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(MqttSubscriber.class);

    private MqttConnectOptions connOpts;
    private MqttAsyncClient mqttClient;
    private String receivedPayload;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private final MqttConfiguration mqttConf;


    @Autowired
    public MqttSubscriber(MqttConfiguration mqttConf, SimpMessagingTemplate messagingTemplate) {
        this.mqttConf = mqttConf;
        this.messagingTemplate = messagingTemplate;
    }

    public MqttAsyncClient getClient() {
        return mqttClient;
    }

    public void setClient(MqttAsyncClient client) {
        this.mqttClient = client;
    }

    public MqttConnectOptions getConnOpts() {
        return connOpts;
    }

    public void setConnOpts(MqttConnectOptions connOpts) {
        this.connOpts = connOpts;
    }

    public MqttConfiguration getMqttConf() {
        return mqttConf;
    }

    public String getReceivedPayload() {
        return receivedPayload;
    }

    public void setReceivedPayload(String receivedPayload) {
        this.receivedPayload = receivedPayload;
    }

    public void configureMqttClient() {
        try {
            LOG.info("Starting the MQTT Configuration...");
            connOpts = mqttConf.configureMqttClient();
            mqttClient = new MqttAsyncClient(mqttConf.getBrokerUrl(), mqttConf.getClientId(), new MemoryPersistence());

            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(mqttConf.getUsername());
            connOpts.setPassword(mqttConf.getPassword().toCharArray());
            connOpts.setAutomaticReconnect(true);

            LOG.info("The Mqtt protocol has been configured successfully!!!");
        }
        catch (Exception ex) {
            LOG.error("An error has happened during Mqtt configuration: " + ex);
        }
    }

    public void connectMqttClient() {
        try {
            // Connect to MQTT client
            LOG.info("Connecting to '{}' MQTT broker", mqttConf.getBrokerUrl());
            IMqttToken token = mqttClient.connect(connOpts);
            token.waitForCompletion();
            LOG.info("Connected to '{}' MQTT broker", mqttConf.getBrokerUrl());

        } catch(MqttException ex) {
            LOG.error("An error has happened: " + ex);
            LOG.error("\nMessage: " + ex.getMessage()
                    + "\nLocalized Message: " + ex.getLocalizedMessage()
                    + "\nCause: " + ex.getCause());
        }
    }

    public void subscribeToTopics(List<String> topics) {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection lost to MQTT broker: " + throwable);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    String payload = new String(mqttMessage.getPayload());
                    System.out.println("Received MQTT message from '" + topic + "': " + payload);

                    // Parse the topic to determine the sensor type
                    String sensorType = topic.split(" ")[1].toLowerCase();

                    // Broadcast the payload to WebSocket clients based on the sensor type
                    messagingTemplate.convertAndSend("/topic/" + sensorType, payload);
                    receivedPayload = payload;
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("Message delivered successfully");
                }
            });

            for (String topic : topics) {
                LOG.info("Subscribing to the topic '{}'", topic);
                mqttClient.subscribe(topic,1);  // Quality of Service level (0, 1, or 2)
                LOG.info("Successful subscribed to topic '{}'", topic);
            }

        } catch(MqttException ex) {
            LOG.error("An error has happened: " + ex);
            LOG.error("\nMessage: " + ex.getMessage()
                    + "\nLocalized Message: " + ex.getLocalizedMessage()
                    + "\nCause: " + ex.getCause());
        }
    }
}

