Setup
-----

1) Power up RPi-1, the RPi-2.
2) RPi-1 is an access point (AP). The wireless network name (SSID) is `PiFi`. Password is `iaroc2014`.
   Connect the laptop to the PiFi network
3) Open terminal window 1 (TW1) and ssh into RPi-1. 
 - username = `pi`
 - IP address = `192.168.42.1`
 - password = `raspberry`
 At the terminal prompt type: `ssh pi@192.168.42.1`
4) Start the server on RPi-1. In TW1, type:
    `sudo java -jar LaserBeamEventServer.jar`
5) Open terminal window 2 (TW2) and ssh into RPi-2. Same as Step 3, except that the IP address is `192.168.42.2`
6) Start the server on RPi-2. Same as Step 4 except that the command is typed in in TW2.
7) Open terminal window 3 (TW3) and start the Wireless Timer (WTS) application on the laptop. 
 Change to the directory where the WirelessTimingSystem.jar file is located (e.g., `cd Desktop`) and type:
    `java -jar WirelessTimingSystem.jar 1`
8) Open terminal window 4 (TW4) and start a second instance of the WTS application. In TW4, type:
    `java -jar WirelessTimingSystem.jar 2`
    
Shutdown
--------

9) Type `^c` (ctrl-c) in TW1 and TW2.
10) Type in TW2, and then in TW1:
    `sudo shutdown  -h now` 

To restart, disconnect power and repeat steps 1 through 8.