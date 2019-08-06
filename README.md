# SRCC Project Read-Me

## Installing the environment
Use the provided batch/shell script to install all the required packages (only python).
Than manually activate the "srcc" environment.
To use any of the script change to the according folder and run according to the launch parameters (if necessary).

## Starting the server
Run "python rest_server.py" in the console. The server will now respond to correct request (url and content).
For the test case (presentation) the server ran on a laptop hosting a local ad-hoc network.
The IP of the server was manually inserted inside the app.
Hence this might be changed depending on the used laptop.
Note: Running inside the eduroam network (even if both are connected) does not work.

## DSIDS Dataset
The dataset is not included inside this github repository for obvious reasons.
Hence it has to be manually downloaded and inserted in the main directory to assure that the scripts can find it!
Node: Inside the HR and LR image folders is a another folder "ignore".
This folder was necessary to make the Keras ImageDataGenerator able to load the pictures.

## CameraApp
The appliciation is inside the "CameraApp" folder.
This folder can be opened with the AndroidStudio program and should be automatically discovered as an Android specific project.
Opening might take a while as AndroidStudio is building and indexing the unknown user specific files.
To build the app two build versions are available (top right corner --> run button).
run (builds the current version of the app) and run-fresh (will build and clean install, remove the previous app from the phone).
