package org.iassist.spring.boot.controller;

import java.util.List;
import org.iassist.spring.boot.mqtt.MqttConfiguration;
import org.iassist.spring.boot.mqtt.MqttSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class Controller {

	@Autowired
	private MqttSubscriber mqttSubscriber;

	@GetMapping("/receivedPayload")
	public String getReceivedPayload() {
		return mqttSubscriber.getReceivedPayload();
	}

	@GetMapping("/subscribeToTopics")
	public void subscribeToTopics() {
		List<String> topics = getTopicsToSubscribe();
		mqttSubscriber.subscribeToTopics(topics);
	}

	@GetMapping("/data")
	public ResponseEntity<String> subscribeForSensorData(@RequestParam(value = "topic") String topic) {
		try {
			MqttConfiguration currentMqttConf = mqttSubscriber.getMqttConf();
			//currentMqttConf.setTopic(topic);
			//mqttSubscriber.setMqttConf(currentMqttConf);
			//mqttSubscriber.subscribeBrokerTopic();


			mqttSubscriber.getReceivedPayload();
			return ResponseEntity.ok(
					String.format("MQTT connection to brokerUrl '%s' and topic '%s' is configured successful!",
							mqttSubscriber.getMqttConf().getBrokerUrl(), "fix this"));

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No payload received for the topic '" + topic + "': " + ex);
		}

        /*if (mqttSubscriber.getReceivedPayload() != null) {
            return ResponseEntity.ok(mqttSubscriber.getReceivedPayload().toString());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No payload received for the topic: " + topic);
        }*/
	}

	@GetMapping("/data-origin")
	public ResponseEntity<String> getSensorDataOrigin(@RequestParam(value = "topic") String topic) throws InterruptedException {
		//mqttSubscriber.subscribeBrokerTopic();

		int waitTimes = 0;
		while (mqttSubscriber.getReceivedPayload() == null && waitTimes <= 5) {
			waitTimes++;
			Thread.sleep(1000);
		}

		if (mqttSubscriber.getReceivedPayload() != null) {
			return ResponseEntity.ok(mqttSubscriber.getReceivedPayload());
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No payload received for the topic: " + topic);
		}
	}

	private List<String> getTopicsToSubscribe() {
		// Implement this method to return a list of topics to subscribe to
		// For example: return List.of("topic1", "topic2", ...);
		return List.of("topic1", "topic2", "topic3");
	}
}
