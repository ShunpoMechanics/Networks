# Networks
Jonathan Correa
Kim Ying
Fatemah Tavassoli

Notes:
StartRemotePeers.java does nothing. In order to run the program one must transfer the java files over to the virtual machines manually.
Use this structure for the best results:
home
   +-- project
   |       +-- peer_1001
   |	        +-- file_to_be_used.dat (or whichever peers the cfg files say it should be in)
   |	   +-- peer_1002
   |	   +-- peer_1003
   |	   +-- peer_1004
   |	   +-- peer_1005
   |	   +-- peer_1006
   +-- Submission
		   +-- all of the .java files
		   +-- Common.cfg
		   +-- PeerInfo.cfg
		   
We were going to dynamically create the directory structure by creating a string using a for loop
to read PeerInfo.cfg in order to read in the peer ids with this structure: "mkdir project project/peer_1001 project/peer_1002 <etc>" 

Now, inside of the Submissions folder, compile PeerProcess.java as normal.
Then you need to manually start each Peer using "java PeerProcess <peer_id>" inserting the appropriate peer id in the order they appear in the PeerInfo.cfg
