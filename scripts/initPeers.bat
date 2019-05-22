::Compile
cd ..\src
for /r %%a in (.) do (javac -d ..\out %%a\*.java)
::javac -d ..\out src\client\*.java src\peer\*.java src\peer\protocols\*.java

cd ..\out

::Starting the Server
start rmiregistry

echo "Waiting 5 seconds for RMI Registry to fully initiate..."
timeout /t 5 /nobreak

::launching peers
start cmd.exe @cmd /k "echo PeerID: 1 & java peer.Peer true peer1 127.0.0.2"