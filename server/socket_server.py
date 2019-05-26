import socket
import selectors
import types


HOST_IP = "127.0.0.1"  # standard loop back address
HOST_PORT = 8000  # everything after 1023 will be ok, but not every port works

# Python description of selectors
# https://docs.python.org/3/library/selectors.html


def accept(sock, sel):
    """Acception new socket connections to the server.

    Implementing the socket connection like a single socket server would do.
    sock -- is the socket used for the connection."""

    # accept the new connection
    connection, address = sock.accept()
    print(f"Accepting new connection from {address}")
    # we still shouldn't block the server with our sockets
    connection.setblocking(False)

    # create a new data object, where can handle the data for sendind and
    # receiving data
    data = types.SimpleNamespace(address = address, inb = b'', outb = b'')

    # we have to care for reading and writing events so combine them
    eventTrigger = selectors.EVENT_READ | selectors.EVENT_WRITE

    sel.register(connection, eventTrigger, data)

def handle(key, mask, sel):
    """Handling the connection with the already connected socket.

    Sending data to the connected socket or receiving data from the socket.
    key -- holds all the information about the socket and its data
    mask -- holds the events that are ready for this socket
    sel -- holds the selctor object, in case we have change some settings
    """

    sock = key.fileobj
    data = key.data

    # checking which event was triggered for our socket
    # read event
    if mask & selectors.EVENT_READ:
        #get the data, if none we close the connection
        receivedData = sock.recv(1024) # should be ready
        if receivedData:
            data.outb += receivedData
        else:
            print(f"Closing connection from {data.address}")
            sel.unregister(sock)
            sock.close()
    
    if mask & selectors.EVENT_WRITE:
        if data.outb:
            print(f"Echo back to {data.address} with {repr(data.outb)}")
            sent = sock.send(data.outb) ## should be ready to write
            # delete the data 
            data.outb = data.outb[sent:]
    

def main():
    """Main function for the socket server.

    Handle all incoming request and echo them."""

    # with selectors we can handle several socket connections
    sel = selectors.DefaultSelector()

    # create the socket for listening with the following parameters
    # AF_INET == IPV4
    multiSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # bind socket and start listening for incoming
    multiSocket.bind((HOST_IP, HOST_PORT))
    multiSocket.listen()
    print(f"Socket server listening on IP {HOST_IP} and port {HOST_PORT}")

    # our socket listener shall be not blocking, so we can handel async
    multiSocket.setblocking(False)

    # register our socket for reading events, which will be triggered by
    # sel.select(), with data we can store all the data send and received
    # over this socket
    sel.register(multiSocket, selectors.EVENT_READ, data=None)

    while True:
        # Wait for event to happen and catch it with select
        triggerEvent = sel.select()

        for key, mask in triggerEvent:
            # We didnt send or received any data, so it has to be a new
            # connection, else we are still connected
            if key.data is None:
                accept(key.fileobj, sel)
            else:
                handle(key, mask, sel)


if __name__ == "__main__":
    main()
