'use strict';
var express = require('express')
var getRawBody = require('raw-body')

const handleUpload = require('./lib/handleUpload');

var app = express()
//app.use(function (req, res, next) { getRawBody(req, { limit: '10mb'}) });

// respond with "hello world" when a GET request is made to the homepage
app.post('/upload', handleUpload);
/*app.get('/hi', function (res, req) {
    req.sendStatus(500);
})*/


app.listen(3000, () => console.log('Example app listening on port 3000!'));
