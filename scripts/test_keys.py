from time import sleep
import websocket

def on_message(ws, message):
    print(message)

if __name__ == "__main__":
    host = "ws://localhost:8025"
    # ws = websocket.WebSocketApp(host, on_message=on_message)
    # ws.run_forever()

    ws = websocket.WebSocket()
    ws.connect(host)
    for _ in range(5):
        # ws.send("mouse_move 10 0")
        ws.send("key_down 87") #w
        # ws.send("key_down 32") #space
        sleep(.1)
        ws.send("key_up 87") #w
        # ws.send("key_up 32")
        sleep(1)
