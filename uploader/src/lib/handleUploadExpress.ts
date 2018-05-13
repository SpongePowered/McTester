import { Request, Response } from "express";

import {handleUpload, ResponseData} from './handleUpload';

function handleUploadExpress(req: Request, res: Response) {
    handleUpload(req).then((resp: ResponseData) => {
        res.status(resp.code).send(resp.message);
    }).catch(bad => {
        res.status(400).send(bad);
    });
}

export { handleUploadExpress };
