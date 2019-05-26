import socket

HOST_IP = "127.0.0.1"  # standard loop back address
HOST_PORT = 8000  # everything after 1023 will be ok, but not every port works


def main():
    """Main function for the socket server.

    Handle all incoming request and echo them."""

    # create the socket for listening with the following parameters
    # AF_INET == IPV4
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST_IP, HOST_PORT))
        s.listen()
        print(f"Socket server listening on IP {HOST_IP} and port {HOST_PORT}")

        # Wait for single connection
        connection, address = s.accept()

        # we receive the connection
        with connection:
            print(f"Received connection from {address}")
            while True:
                # the data the client sends to the server
                # chunksize can be changed
                data = connection.recv(1024)
                if not data:
                    break
                #echo the data send back to the client
                connection.sendall(data)

if __name__ == "__main__":
    main()
