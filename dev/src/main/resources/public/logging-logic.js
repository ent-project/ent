
let id = id => document.getElementById(id);

let ws = new WebSocket("ws://" + location.hostname + ":" + location.port + "/logs");
ws.onopen = () => {
    id("disconnected").innerText = "connected"
}
ws.onmessage = msg => updateLogs(msg);
ws.onclose = () => {
    id("disconnected").innerText = "connection closed"
}

function updateLogs(msg) {
    console.log("updateLogs '"+msg.data+"'")

    let logs = id("logs");
    let atBottom = logs.scrollTop + logs.clientHeight >= logs.scrollHeight;
    console.log("atBottom: " + atBottom);
    let data = JSON.parse(msg.data);
    logs.insertAdjacentHTML("beforeend", data.message);
    if (atBottom) {
        logs.scrollTop = logs.scrollHeight;
    }
}