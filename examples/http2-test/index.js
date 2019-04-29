const HTTPS_PORT = 3000;
const HTTP2_PORT = 3001;

/**
 * create a normal https server
 */
const https = require("https");
const fs = require("fs");
const mime = require("mime");

const serverOptions = {
  key: fs.readFileSync(__dirname + "/secret/key.pem"),
  cert: fs.readFileSync(__dirname + "/secret/cert.pem")
};

const httpsHandler = (req, res) => {
  console.log(req.url);
  // send emty response for favicon.ico
  if (req.url === "/favicon.ico") {
    res.writeHead(200);
    res.end();
    return;
  }

  const fileName = req.url === "/" ? "index.html" : __dirname + req.url;
  fs.readFile(fileName, (err, data) => {
    if (err) {
      res.writeHead(503);
      res.end("Error occurred while reading file", fileName);
      return;
    }
    res.writeHead(200, { "Content-Type": mime.getType(fileName) });
    res.end(data);
  });
};

https
  .createServer(serverOptions, httpsHandler)
  .listen(HTTPS_PORT, () =>
    console.log("https server started on port", HTTPS_PORT)
  );

/**
 * create an http2 server
 */
const http2 = require("http2");

// read and send file content in the stream
const sendFile = (stream, fileName) => {
  const fd = fs.openSync(fileName, "r");
  const stat = fs.fstatSync(fd);
  const headers = {
    "content-length": stat.size,
    "last-modified": stat.mtime.toUTCString(),
    "content-type": mime.getType(fileName)
  };
  stream.respondWithFD(fd, headers);
  stream.on("close", () => {
    console.log("closing file", fileName);
    fs.closeSync(fd);
  });
  stream.end();
};

const pushFile = (stream, path, fileName) => {
  stream.pushStream({ ":path": path }, (err, pushStream) => {
    if (err) {
      throw err;
    }
    sendFile(pushStream, fileName);
  });
};

// handle requests
const http2Handlers = (req, res) => {
  console.log(req.url);
  if (req.url === "/") {
    // push style.css
    pushFile(res.stream, "/style.css", "style.css");

    // push all files in scripts directory
    const files = fs.readdirSync(__dirname + "/scripts");
    for (let i = 0; i < files.length; i++) {
      const fileName = __dirname + "/scripts/" + files[i];
      const path = "/scripts/" + files[i];
      pushFile(res.stream, path, fileName);
    }

    // push all files in images directory
    const imageFiles = fs.readdirSync(__dirname + "/images");
    for (let i = 0; i < imageFiles.length; i++) {
      const fileName = __dirname + "/images/" + imageFiles[i];
      const path = "/images/" + imageFiles[i];
      pushFile(res.stream, path, fileName);
    }

    // lastly send index.html file
    sendFile(res.stream, "index.html");
  } else {
    // send empty response for favicon.ico
    if (req.url === "/favicon.ico") {
      res.stream.respond({ ":status": 200 });
      res.stream.end();
      return;
    }
    const fileName = __dirname + req.url;
    sendFile(res.stream, fileName);
  }
};

http2
  .createSecureServer(serverOptions, http2Handlers)
  .listen(HTTP2_PORT, () => {
    console.log("http2 server started on port", HTTP2_PORT);
  });
