WirelessTimingSystem
====================

Setup
-----

1. Power up RPi-1 _first_, and _then_ RPi-2.

2. RPi-1 is an access point (AP). The wireless network name (SSID) is `PiFi`. The password is `iaroc2014`. Connect the laptop to the PiFi network

3. Open terminal window 1 (TW1) and ssh into RPi-1.
 - username = `pi`
 - IP address = `192.168.42.1`
 - password = `raspberry`
 
 At the terminal prompt type: `ssh pi@192.168.42.1`

4. Start the server on RPi-1. In TW1, type: 
    
    `sudo java -jar LaserBeamEventServer.jar`

 Observe in TW1 that the server has started and is listening on port 47047.

5. Open terminal window 2 (TW2) and ssh into RPi-2. Same as Step 3, except that the IP address is `192.168.42.2`

6. Start the server on RPi-2. Same as Step 4 except that the command is typed in in TW2.

7. Open terminal window 3 (TW3) and start the Wireless Timing System (WTS) application on the laptop.
 Change to the directory where the WirelessTimingSystem.jar file is located (e.g., `cd Desktop`) and type: 
    
    `java -jar WirelessTimingSystem.jar 1`

 Observe, in TW3, that WTS application is connected to the server on RPi-1 and, in TW1, that the server is connected to the application.

8. Open terminal window 4 (TW4) and start a second instance of the WTS application that connects to RPi-2. In TW4, type:
     
     `java -jar WirelessTimingSystem.jar 2`

 Observe, in TW4, that WTS application is connected to the server on RPi-2 and, in TW2, that the server is connected to the application.
    
Operation
---------

Two LEDs are at the start and finish line in each lane. The first LED indicates that the laser beam is received and is independent of any software. The second LED is controlled by the server on the corresponding RPi and blinks when the server is running. Fast blinking (4 Hz) indicates that both beams are LOW (i.e., broken), slower blinking (2 Hz) indicates that one beam is LOW and the other HIGH, and slow blinking (1 Hz) indicates that both beams are HIGH.

The WTS application on the laptop receives laser beam events from the corresponding server. An event is a transition from HIGH to LOW or from LOW to HIGH. 

The WTS has three states:

1. armed - zero time is displayed
2. running - time since start transition is continuously updated and displayed
3. stopped - time from start transition to stop transition is displayed

and four transitions

1. start - from armed to running; start time is recorded
2. stop - from running to stopped; 
3. resume - from stopped to running; start time is _not_ updated
4. reset - from any state to armed;

The WTS application has two modes of operation; _automatic_ and _manual_. A radio button allows the user to switch between the two modes. In both modes, the `RESET` button arms the timer.

In automatic mode, a start transition occurs when the beam at the start line transitions to LOW (i.e., is broken) and a stop transition occurs when the beam at the finish line transitions to LOW. All other beam events are ignored. A resume transition is not possible in automatic mode. 

In manual mode, a button is enabled that allows the user to `START`, `STOP`, or `RESUME` depending on state. 

A mode switch may be done at any time in any state. For example, the robot may start with the WTS in automatic mode. The clock starts running as the robot breaks the beam at the start line. The robot then bumps hard into a wall causing the beam at the finish line to become misaligned and the timer to stop. The user can then switch to manual mode and click on he `RESUME` button. While the robot is still running, assistants on the floor manage to realign the laser beam at the finish line. The user switches back to atomatic mode and the clock automatically stops when the robot breaks the beam at the finish line. In the event that the assistants do not succeed in realigning the laser beam at the finish line, the user manually stops the clock when the robot reaches the finish line. 

The displayed time is copy-able. The `Tab` key may be used to cycle through the controls and the `Space` bar may be used to select (i.e., click on) a button.

Two dots on the display indicate the state of the two laser beams (left dot corresponds to the beam at the start). Green means HIGH and red means LOW. 


Shutdown
--------

1. Type `^c` (ctrl-c) in TW1 and TW2.

2. Type in TW2 _first_, and _then_ in TW1:
    
    `sudo shutdown -h now`

To restart, disconnect power and repeat steps 1 through 8.
