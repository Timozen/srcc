import socket

HOST_IP = "127.0.0.1"  # standard loop back address
HOST_PORT = 8000  # everything after 1023 will be ok, but not every port works


def main():
    """Main function for the socket client.

    This one is only for testing purpose to check if the server is working
    in the correct way. """

    # create a socket for connecting to another socket
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST_IP, HOST_PORT))
        print(f"Socket client connecting to IP {HOST_IP} and port {HOST_PORT}")

        s.send(bytes(2))

        size = 6
        s.send(size.to_bytes(4, byteorder = "big"))
        s.send(bytes(size))

        header = s.recv(2) # should be ready
        size = s.recv(4)
        payloadSize = int.from_bytes(size, byteorder = "big")
        payload = s.recv(payloadSize)

        print(f"received header {header} and size {size}")
        print(f"payload {payload}")
if __name__ == "__main__":
    main()
