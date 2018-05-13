import { Request, Response } from "express";

import { handleUpload } from './handleUpload';

function handleUploadExpress(req: Request, res: Response) {
    handleUpload(req).then(code => {
        res.sendStatus(code);
    }).catch(bad => {
        res.status(400).send(bad);
    });
}

export { handleUploadExpress };
