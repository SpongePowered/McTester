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

function rejectMissing(required, actual, reject) {
    if (actual == null) {
        reject("Missing fields: " + required);
        return true;
    }

    let missing = "";
    for (let field of required) {
        if (actual[field] == null) {
            missing += field + ",";
        }
    }

    if (missing !== "") {
        reject("Missing fields: " + missing.slice(0, missing.length - 1));
        return true;
    }
    return false;
}

module.exports = function(req) {
    return new Promise(function(resolve, reject) {
        if (req.method === 'POST') {

            const busboy = new Busboy({ headers: req.headers });

            const uploads = [];
            const fields = {};
            const tmpdir = os.tmpdir();

            // This callback will be invoked for each file uploaded.
            busboy.on('file', (fieldname, file, filename) => {
                // Note: os.tmpdir() points to an in-memory file system on GCF
                // Thus, any files in it must fit in the instance's memory.
                const filepath = path.join(tmpdir, filename);
                uploads.push({fieldName: fieldname, path: filepath, name: filename});
                file.pipe(fs.createWriteStream(filepath));
            });

            busboy.on('field', function(fieldname, val, fieldnameTruncated, valTruncated, encoding, mimetype) {
                fields[fieldname] = val;
            });

            // This callback will be invoked after all uploaded files are saved.
            busboy.on('finish', () => {

                if (rejectMissing(["user", "repo", "commitSha"], fields, reject)) {
                    return;
                }

                let promises = uploads.map(file => { return readFilePromise(file.path).then(data => {
                    return new ImageWrapper(data, file.fieldName);
                })});

                Promise.all(promises)
                    .then(uploads => upload.createNewStatus(uploads, fields.user, fields.repo, fields.commitSha))
                    .then(uploads => {
                        resolve("Uploaded images: " + uploads)
                    })
                    .catch(e => {
                        reject("Internal error: " + e);
                    })
            });

            req.pipe(busboy);
        }
        else {
            reject("Bad request method: " + req.method)
        }
    })

}
