// http library
var http = require("http");
 
http.createServer(function(req, res) {
    // check the value of host header
    if (req.headers.host == "company.cz") {
        res.writeHead(201, "Content-Type: text/plain");
        res.end("This is the response...");
    } else ;
        // handle enterprise.com app logic...
}).listen('127.0.0.1', 9080);
