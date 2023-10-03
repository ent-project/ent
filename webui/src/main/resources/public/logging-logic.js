
let id = id => document.getElementById(id);

let webSocketUrl = "ws://" + location.hostname + ":" + location.port + "/logs";
let query = new URLSearchParams(location.search);
let storyId = query.get("story");
if (storyId !== null) {
    webSocketUrl += "?story=" + storyId;
}

let ws = new WebSocket(webSocketUrl);
ws.onopen = () => {
    id("connection-status").innerText = "connected"
}
ws.onmessage = msg => updateLogs(msg);
ws.onclose = () => {
    id("connection-status").innerText = "connection closed"
}

function updateLogs(msg) {
    let logs = id("logs");
    let data = JSON.parse(msg.data);
    const article = document.createElement('article');
    if (data.jump) {
        article.className = "jump";
    }
    if (data.jump_major) {
        article.className = "jump_major"
    }
    if (data.type === 'log' || data.type === 'html') {
        let atBottom = logs.scrollTop + logs.clientHeight >= logs.scrollHeight;
        if (data.type === 'log') {
            const pre = document.createElement('pre');
            pre.innerText = data.message;
            article.appendChild(pre);
        } else if (data.type === 'html') {
            const p = document.createElement('p');
            p.innerHTML = data.html;
            article.appendChild(p);
        }
        logs.insertAdjacentElement("beforeend", article);
        if (atBottom) {
            logs.scrollTop = logs.scrollHeight;
        }
    } else if (data.type === 'dot') {
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

function findFromEnd(arr, predicate) {
    for (let i = arr.length - 1; i >= 0; i--) {
        if (predicate(arr[i], i, arr)) {
            return arr[i];
        }
    }
    return undefined;
}

document.addEventListener("keydown", function(event) {
    console.log(event.key);
    console.log(event.shiftKey);
    if (event.key.toLowerCase() !== 'n' && event.key.toLowerCase() !== 'p') {
        return;
    }
    const entries = Array.from(document.querySelectorAll(event.shiftKey ? '.jump_major' : '.jump'));
    console.log(event.key.toLowerCase())
    if (event.key.toLowerCase() === 'n') {
        console.log("coming here...")
        const nextEntry = entries.find(entry => {
            const rect = entry.getBoundingClientRect();
            return rect.top > 5;
        });
        if (nextEntry !== undefined) {
            nextEntry.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    } else if (event.key.toLowerCase() === 'p') {
        const previousEntry = findFromEnd(entries, entry => {
            const rect = entry.getBoundingClientRect();
            return rect.top < -5;
        });
        if (previousEntry !== undefined) {
            previousEntry.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
});