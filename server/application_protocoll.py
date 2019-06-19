# -*- coding: utf-8 -*-
"""Sending and receiving data helper module.

This module is for handling the data send and received via a socket.
Its implementing the in the group agreed way of handling the send data.
All the definitions can be found inside the GitHub wiki.
"""
import socket


def send_message(sock, messageType=0, data=0):
    """This function will send a message over a existing socket connection.

    The idea is of this function is handling creating message part, so the 
    user only has to give the data and which kind of message will be send.
    The function then calculates all the according byte representations and
    sends it over the given socket.
    
    sock -- the socket for sending the data
    messageType -- in which way the data has to be encoded
    data -- the data what will be send over the socket
    """
    # check which way of data process we have to do
    # TODO put in its own function!
    if messageType == 0:
        header = bytes(2)
    elif messageType == 1:
        # make int to 2 byte representation
        header = (1).to_bytes(2, byteorder="big")
        # calculate the size of the data chunk, which has to be binary
        temp = bin(data)[2:]
        size = len(temp)//8 + 1
        
        # convert data and size to byte
        payload = data.to_bytes(size, byteorder="big")
        size = size.to_bytes(4, byteorder="big")

    #send the 3 parts of the message over the socket
    out = header + size + payload
    sock.sendall(out)


def receive_message(sock):
    """This function will handle the incoming bytes from the connected socket.

    Because we agreed on certain way of building our messages, this function
    will first received the header chunkg of the message and the size chunk.
    With this information we will than handle the data chunk which can be in 
    given size.
    
    sock -- the socket where we expect the data.
    """
    # first we read the 2 bytes which is the header
    header = sock.recv(2)
    # We decode it an int
    messageType = int.from_bytes(header, byteorder="big")

    # receive the next 4 bytes which are the size of the payload
    size = sock.recv(4)
    payloadSize = int.from_bytes(size, byteorder="big")

    bytesReceived = 0
    payload = bytes(0)
    # load all the bytes until we have gathered all
    while bytesReceived < payloadSize:
        # load only the as much as we have to or the standard amount
        payLoadChunk = sock.recv(min(payloadSize - bytesReceived, 1024))
        # check that our data is not broken because the connection died
        # while transfering
        if payLoadChunk == '':
            raise RuntimeError("Socket Connection died")
        # attach it
        payload += payLoadChunk
        bytesReceived += len(payLoadChunk)

    # handle the converting of the data!
    # TODO put in its own function
    if messageType == 0:
        return 0, 0
    elif messageType == 1:
        data = int.from_bytes(payload, byteorder="big")
        return 1, data

    return 0, 0
