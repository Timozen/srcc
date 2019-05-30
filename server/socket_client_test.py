import socket

from application_protocoll import receive_message, send_message

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

        send_message(s, messageType=1, data=4251165801351)

        messageType, data = receive_message(s)

        print(f"received messageType {messageType}")
        print(f"payload {data}")


if __name__ == "__main__":
    main()
