
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
        const article = document.createElement('article');
        const pre = document.createElement('pre');
        pre.innerText = data.message;
        article.appendChild(pre);
        logs.insertAdjacentElement("beforeend", article);
        if (atBottom) {
            logs.scrollTop = logs.scrollHeight;
        }
    } else if (data.type === 'dot') {
        const article = document.createElement('article');
        logs.insertAdjacentElement("beforeend", article);
        Viz.instance().then(function(viz) {
            let atBottom = logs.scrollTop + logs.clientHeight >= logs.scrollHeight;
            const div = document.createElement('div');
            article.appendChild(div);
            let svg = viz.renderSVGElement(data.dot);
            if ("scale" in data) {
                let width = parseFloat(svg.getAttribute("width"));
                let height = parseFloat(svg.getAttribute("height"));
                let scale = data.scale
                width = width * scale;
                height = height * scale;
                svg.setAttribute("width", width + "pt");
                svg.setAttribute("height", height + "pt");
            }
            div.appendChild(svg);
            if (atBottom) {
                logs.scrollTop = logs.scrollHeight;
            }
        });
    } else {
        console.error("unknown type: " + data.type);
    }
}