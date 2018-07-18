import ErrnoException = NodeJS.ErrnoException;
import * as Busboy from 'busboy';

const upload = require('./upload');
const ImageWrapper = upload.ImageWrapper;
const fs = require('fs');
const os = require('os');
const path = require('path');

class ResponseData {
    code: number;
    message: string;

    constructor(code: number, message: string) {
        this.code = code;
        this.message = message;
    }
}

interface Upload {
    fieldName: string,
    path: string,
    name: string
}

interface ClientFields {
    [key: string]: string;
}

interface RequestLike {
    method: string;
    headers: Object;
    pipe: <T extends NodeJS.WritableStream>(dest: T) => T;
}

function readFilePromise(filePath: string) {
    return new Promise(function(resolve, reject) {
        fs.readFile(filePath, function(err: ErrnoException, data: Buffer) {
            if (err != null) {
                reject(err);
            } else {
                resolve(data);
            }
        })
    });
}

function rejectMissing(required: string[], actual: ClientFields, reject: (reason?: any) => void) {
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

function handleUpload(req: RequestLike): Promise<ResponseData> {
    return new Promise(function(resolve, reject) {
        if (req.method === 'POST') {

            const busboy = new Busboy({ headers: req.headers });

            const uploads: Upload[] = [];
            const fields : ClientFields = {};
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
                    .then(uploads => upload.createNewStatus(uploads, fields.user, fields.repo, fields.commitSha).then((githubData: Object) => {
                        resolve(new ResponseData(200, "Uploaded images: " + uploads))
                    }))
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

export { ResponseData, handleUpload };
