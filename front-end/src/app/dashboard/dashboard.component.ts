import { OnInit, Component } from '@angular/core';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit {

  payloadData: string = "";
  temperatureData: string = "";
  humidityData: string = "";
  motionData: string = "";
  contactData: string = "";

  constructor() { }

  ngOnInit(): void {
    const socket = new SockJS('http://localhost:8081/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
      stompClient.subscribe('/topic/contact', (message) => {
        this.contactData = JSON.parse(message.body).contact

      });

      stompClient.subscribe('/topic/temperature', (message) => {
        this.temperatureData = JSON.parse(message.body).temperature;
        this.humidityData = JSON.parse(message.body).humidity;
      });

      stompClient.subscribe('/topic/motion', (message) => {
        this.motionData = JSON.parse(message.body).occupancy
      });
    });

    stompClient.activate();
  }

  refresh(): void {
    window.location.reload();
  }
}
