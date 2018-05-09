const imgurUploader = require('imgur-uploader');
const GitHub = require('github-api');
const fs = require('fs');


module.exports.ImageWrapper = class ImageWrapper {
    constructor(raw, title) {
        this.raw = raw;
        this.title = title;
    }

};

function postComment(message) {
	let gh = new GitHub({token: process.env.GITHUB_TOKEN});
	return gh.getIssues('AaronBot1011', 'GHTest').createIssueComment(1, message).catch(function(error) {
	    console.error("Err: " + error.response);
    }).then((p) => {
        return p;
    });
}

function uploadImages(images) {
    return Promise.all(images.map(image => imgurUploader(image.raw, {token: process.env.IMGUR_TOKEN, title: image.title})));
}

module.exports.uploadAndComment= function(images) {
    if (images.length === 0) {
        return;
    }

    return uploadImages(images).then(uploads => {
        let message = "Some Minecraft integration tests for this PR failed. The following screenshots were taken:\n\n------\n";

        for (let upload of uploads) {
            message += upload.title + "\n";
            message += "![" + upload.title + "](" + upload.link + ")";
        }
        let res = postComment(message);
        console.log("Posted comment for images: " + uploads.map(u => u.title));
        return res;
    });
}

//uploadAndComment([new ImageWrapper(fs.readFileSync('test1.png'), "First title"), new ImageWrapper(fs.readFileSync('test2.png'), "Second title")]).then(() => console.log("All done!"));

//result = uploadImages();
//result.then(links => console.log("Got links: " + links));
