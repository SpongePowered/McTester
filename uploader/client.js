#!/usr/bin/env node

const path = require('path');
const fs = require('fs');
const request = require('request');

let screenshots = path.join(process.env.MCTESTER_GAMEDIR, 'screenshots', 'fail')
let formData = {};

fs.readdir(screenshots, (err, files) => {
    if (err != null) {
        throw err;
    }

    console.log("Files real: " + files);

    files.forEach(file => {
        formData[file] = fs.createReadStream(path.join(screenshots, file));
    });

    slug = process.env.TRAVIS_REPO_SLUG.split("/");

    formData.user = slug[0];
    formData.repo = slug[1];
    formData.commitSha = process.env.TRAVIS_COMMIT;


    request.post({url: process.env.MCTESTER_UPLOAD_URL, formData: formData}, function (error, response, body) {
        console.log('error:', error); // Print the error if one occurred
        console.log('statusCode:', response && response.statusCode); // Print the response status code if a response was received
        console.log('body:', body); // Print the HTML for the Google homepage.
    });
})

