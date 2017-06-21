var chatIP;
var logIP;
var regIP;

function getIPs() {
    chatIP = Document.getElementById("#chatIP").value;
    logIP = Document.getElementById("#logIP").value;
    regIP = Document.getElementById("#regIP").value;
    var ips = [chatIP, logIP, regIP];

    return ips;
}