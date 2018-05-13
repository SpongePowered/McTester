import {ResponseData, handleUpload} from "../lib/handleUpload";

var str = require('string-to-stream');

interface StringMap {
    [name: string]: string;
}

interface OpenWhiskData {
    // Builtin
    __ow_method: string,
    __ow_headers: StringMap
    __ow_path: string;
    __ow_user: string;
    __ow_body: string;

    // Custom
    GITHUB_TOKEN: string,
    IMGUR_TOKEN: string
}

interface Global {
    main: (args: OpenWhiskData) => Object;
}

declare var global: Global;

global.main = function (args: OpenWhiskData): Object {
    let decoded = new Buffer(args.__ow_body,'base64');
    let newStream = str(decoded);

    process.env.GITHUB_TOKEN = args.GITHUB_TOKEN;
    process.env.IMGUR_TOKEN = args.IMGUR_TOKEN;

    return handleUpload({
                            method: args.__ow_method.toUpperCase(),
                            headers: args.__ow_headers,
                            pipe: function<T extends NodeJS.WritableStream>(target: T): T {
                                return newStream.pipe(target);
                            }
                        })
        .then((message: ResponseData) => {
            return {custom_success: message.message};
        }).catch(err => {
            return {custom_error: err};
        });
}
