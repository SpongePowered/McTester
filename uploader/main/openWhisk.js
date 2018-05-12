var str = require('string-to-stream');
const handleUpload = require('../lib/handleUpload.js');

global.main = function (args) {
    let decoded = new Buffer(args.__ow_body,'base64');
    let newStream = str(decoded);

    process.env.GITHUB_TOKEN = args.GITHUB_TOKEN;
    process.env.IMGUR_TOKEN = args.IMGUR_TOKEN;

    return handleUpload({
                            method: args.__ow_method.toUpperCase(),
                            headers: args.__ow_headers,
                            pipe: function(target) {
                                newStream.pipe(target);
                            }
                        })
        .then(message => {
            return {custom_success: message};
        }).catch(err => {
            return {custom_error: err};
        });
}
