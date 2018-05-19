import {Album} from "./imgurAPI";

//const imgurUploader = require('imgur-uploader');
const GitHub = require('github-api');
const fs = require('fs');
const ImgurAPI = require('./imgurAPI');

interface GithubResponse {
    response: Object
}

class ImageWrapper {
    public raw: Buffer;
    public opt: { title: string
    constructor(raw: Buffer, title: string) {
        this.raw = raw;
        this.opt = {title: title};
    }

    public toString(): string {
        return `ImageWrapper(${JSON.stringify(this.opt)})`
    }

};

function postComment(message: string) {
	let gh = new GitHub({token: process.env.GITHUB_TOKEN});
	return gh.getIssues('AaronBot1011', 'GHTest').createIssueComment(1, message).catch(function(error: GithubResponse) {
	    console.error("Err: " + error.response);
    });
}

function setStatus(user: string, repoName: string, sha: string, message: string, url: string) {
    let gh = new GitHub({token: process.env.GITHUB_TOKEN});

    return gh.getRepo(user, repoName).updateStatus(sha, {
            state: 'failure',
            target_url: url,
            description: message,
            context: 'mctester'
        });
}

function uploadImages(images: ImageWrapper[]) {
    let imgur = new ImgurAPI();
    return Promise.all(images.map(image => imgur.uploadImage(image.raw, image.opt)));
}


function createNewStatus(images: ImageWrapper[], user: string, repoName: string, sha: string) {
    if (images.length === 0) {
        return;
    }
    let imgur = new ImgurAPI();

    return imgur.uploadIntoAlbum(images, {title: 'McTester images for ' + user + "/" + repoName + "#" + sha}).then((album: Album) => {
        console.log("Made album: " + album.link);
        return setStatus(user, repoName, sha, "Some Minecraft integration tests failed", album.link)
    })
};


module.exports.uploadAndComment = function(images: ImageWrapper[]) {
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

export { ImageWrapper, createNewStatus };

//uploadAndComment([new ImageWrapper(fs.readFileSync('test1.png'), "First title"), new ImageWrapper(fs.readFileSync('test2.png'), "Second title")]).then(() => console.log("All done!"));

//result = uploadImages();
//result.then(links => console.log("Got links: " + links));
