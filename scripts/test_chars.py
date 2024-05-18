from time import sleep
import websocket

if __name__ == "__main__":
    host = "ws://localhost:8025"

    ws = websocket.WebSocket()
    ws.connect(host)
    for _ in range(5):
        ws.send(f"char {ord('H')}")
        sleep(.1)
        ws.send(f"char {ord('I')}")
        sleep(1)
