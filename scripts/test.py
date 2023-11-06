import websocket

def on_message(ws, message):
    print(message)

if __name__ == "__main__":
    host = "ws://localhost:8025"
    ws = websocket.WebSocketApp(host, on_message=on_message)
    ws.run_forever()
