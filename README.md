# TCPUsingUDP 
This project basically simulates the working of TCP tahoe.
Things taken care:
1. Congestion control
2. Flow Control 
3. Data integrity and reliability

To start the simulation:
1. Open two networks
2. Run the server on one of them by using the command: java NewServer fileName.txt 
followed by javac *.java 
Here the file name is the document that has to be shared
3. After the server is up client needs to be started by commands: java Client IPAddress port 
followed by javac *.java 
4. THe server will start sending the file once a client requests for it,
by sending a sync message simulating a handshake