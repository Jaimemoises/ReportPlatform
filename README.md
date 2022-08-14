A JAVA project called Report Platform was created in the IntelliJ IDE with the primary goal of simulating a report platform that receives and maintains various events. Protocol Buffers and gRPC are used in this Distributed System project to enable communication between Servers, Services, and Clients. The Java implementation of multicast DNS is used for service naming, registration, and discovery (JMDNS).

The services created were:

•	Create Report that allows the user to add new reports to the platform, as well as new events.

•	Read Reports allows the user to read the reports added to the platform.

•	Update Report the user can update, adding or removing information

•	Delete Report deletes a wrongly added report or that for some reason needs to be deleted.

•	List Reports allows the user to see all added reports and their information.


A Command Line Interface (CLI) is used to improve code visualisation. The CLI chosen is “EVANS” witch can be installed easily using “brew” in the terminal. To start EVANS after the installation the command is evans --host localhost --port 50051 --reflection repl, using show package, show service, show message and the command call you will be able to use the most part of the services and interact with the platform.

A database called MongoDB was also used, which stores the information added by the user to the platform. With the files on the computer and the project open in IntelliJ it is necessary to start docker, after that start the server, and the client.

Link for the project presentation video: https://youtu.be/LgfNKGgahno
