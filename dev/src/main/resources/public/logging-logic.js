
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
    let logs = id("logs");
    let data = JSON.parse(msg.data);
    if (data.type === 'log') {
        let atBottom = logs.scrollTop + logs.clientHeight >= logs.scrollHeight;
        logs.insertAdjacentHTML("beforeend", data.message);
        if (atBottom) {
            logs.scrollTop = logs.scrollHeight;
        }
    } else if (data.type === 'dot') {
        const article = document.createElement('article');
        logs.insertAdjacentElement("beforeend", article);
        Viz.instance().then(function(viz) {
            let atBottom = logs.scrollTop + logs.clientHeight >= logs.scrollHeight;
            article.appendChild(viz.renderSVGElement(data.dot));
            if (atBottom) {
                logs.scrollTop = logs.scrollHeight;
            }
        });
    } else {
        console.error("unknown type: " + data.type);
        return;
    }
}