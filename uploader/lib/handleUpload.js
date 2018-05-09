const Busboy = require('busboy');
const upload = require('./upload.js');
const ImageWrapper = upload.ImageWrapper;
const fs = require('fs');
const os = require('os');
const path = require('path');

function readFilePromise(filePath) {
    return new Promise(function(resolve, reject) {
        fs.readFile(filePath, function(err, data) {
            if (err != null) {
                reject(err);
            } else {
                resolve(data);
            }
        })
    });
}

module.exports = function (req, res) {
    if (req.method === 'POST') {
        const busboy = new Busboy({ headers: req.headers });

        const uploads = [];
        const tmpdir = os.tmpdir();

        // This callback will be invoked for each file uploaded.
        busboy.on('file', (fieldname, file, filename) => {
            // Note: os.tmpdir() points to an in-memory file system on GCF
            // Thus, any files in it must fit in the instance's memory.
            const filepath = path.join(tmpdir, filename);
            uploads.push({fieldName: fieldname, path: filepath, name: filename});
            file.pipe(fs.createWriteStream(filepath));
        });

        // This callback will be invoked after all uploaded files are saved.
        busboy.on('finish', () => {
            let promises = uploads.map(file => { return readFilePromise(file.path).then(data => {
                return new ImageWrapper(data, file.fieldName);
            })});

            Promise.all(promises)
                .then(upload.uploadAndComment)
                .then(res.sendStatus(200));
        });

        req.pipe(busboy);
    }
}
