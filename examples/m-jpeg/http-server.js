'use strict';
const fs = require('fs');
const http = require('http');
const events = require('events');

const emitter = new events.EventEmitter();
const buffer = []
const server = http.createServer((req, res) => {
	console.log(req.url);	
	if (req.url=='/video') {
		res.writeHead(200,'Content-Type: text/html')
		res.end(fs.readFileSync('video.html'));
		return;
	}
	
	if (req.url=='/video-stream') {
		console.log("Sreaming video...");
			    
		res.writeHead(200, {
			'Cache-Control': 'no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0',
			'Pragma': 'no-cache',
			'Connection': 'close',
			'Content-Type': 'multipart/x-mixed-replace; boundary=--myboundary'
		});

		const writeFrame = () => {
		  const frame = buffer.shift();
			if (frame) {
				res.write(`--myboundary\nContent-Type: image/jpg\nContent-length: ${frame.length}\n\n`);
				res.write(frame);
			}
		};

		emitter.addListener('frame', writeFrame);
		res.addListener('close', () => {
			emitter.removeListener('frame', writeFrame);
		});
	}

});
server.listen(8000);

setInterval(() => {
	emitter.emit('frame');
}, 300);

// read frames to the buffer
setInterval(() => {
	fs.readdir('./jpeg-stream/', (err, files) => {
		files.forEach(file => {
			if (file.match('out-.+')) {                   
				console.log(file);
				try {
					const frame = fs.readFileSync('./jpeg-stream/' + file);
					buffer.push(frame);
					fs.unlinkSync('./jpeg-stream/' + file)
				} catch(e) {}
			}
		})
	});	
}, 100);
