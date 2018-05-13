import { Request, Response } from "express";

const handleUpload = require('./handleUpload.js');

function handleUploadExpress(req: Request, res: Response) {
    handleUpload(req).then(code => {
        res.sendStatus(code);
    }).catch(bad => {
        res.status(400).send(bad);
    });
}

export { handleUploadExpress };
