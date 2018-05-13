const imgurUploader = require('imgur-uploader');
const GitHub = require('github-api');
const fs = require('fs');
const ImgurAPI = require('./imgurAPI');


module.exports.ImageWrapper = class ImageWrapper {
    constructor(raw, title) {
        this.raw = raw;
        this.opt = {title: title};
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

function setStatus(user, repoName, sha, message, url) {
    let gh = new GitHub({token: process.env.GITHUB_TOKEN});

    return gh.getRepo(user, repoName).updateStatus(sha, {
            state: 'failure',
            target_url: url,
            description: message,
            context: 'mctester'
        });
}

function uploadImages(images) {
    let imgur = new ImgurAPI();
    return Promise.all(images.map(image => imgur.uploadImage(image.raw, {title: image.title})));
}


module.exports.createNewStatus = function(images, user, repoName, sha) {
    if (images.length === 0) {
        return;
    }
    let imgur = new ImgurAPI();

    return imgur.uploadIntoAlbum(images, {title: 'McTester images for ' + user + "/" + repoName + "#" + sha}).then(album => {
        console.log("Made album: " + album.link);
        return setStatus(user, repoName, sha, "Some Minecraft integration tests failed", album.link)
    })
};


module.exports.uploadAndComment = function(images) {
    if (images.length === 0) {
        return;
    }

    return uploadImages(images).then(uploads => {
        let message = "Some Minecraft integration tests for this PR failed. The following screenshots were taken:\n\n------\n";

        for (let upload of uploads) {
            message += upload.title + "\n";
            message += "![" + upload.title + "](" + upload.link + ")";
        }
        return postComment(message).then(() => {
            console.log("Posted comment for images: " + uploads.map(u => u.title));
            return uploads;
        })
    });
};

//uploadAndComment([new ImageWrapper(fs.readFileSync('test1.png'), "First title"), new ImageWrapper(fs.readFileSync('test2.png'), "Second title")]).then(() => console.log("All done!"));

//result = uploadImages();
//result.then(links => console.log("Got links: " + links));
