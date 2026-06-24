const http = require('http');
const fs = require('fs');
const path = require('path');
const root = process.argv[2];
const port = Number(process.argv[3]);
const types = {'.html':'text/html','.css':'text/css','.png':'image/png','.md':'text/plain','.json':'application/json','.xml':'application/xml','.diff':'text/plain','.txt':'text/plain'};
http.createServer((req,res)=>{
  const url = decodeURIComponent(req.url.split('?')[0]);
  const file = path.normalize(path.join(root, url === '/' ? 'analysis/langgraph-business-understanding.html' : url));
  if (!file.startsWith(root)) { res.writeHead(403); res.end('forbidden'); return; }
  fs.readFile(file, (err,data)=>{
    if (err) { res.writeHead(404); res.end('missing'); return; }
    res.writeHead(200, {'content-type': types[path.extname(file)] || 'application/octet-stream'});
    res.end(data);
  });
}).listen(port, '127.0.0.1');
