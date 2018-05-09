const imgurUploader = require('imgur-uploader');
const GitHub = require('github-api');
const fs = require('fs');

function postComment() {
	var gh = new GitHub({token: process.env.GITHUB_TOKEN});
	return gh.getIssues('AaronBot1011', 'GHTest').createIssueComment(1, 'This is a comment!');
}

function uploadImages(images) {
    return Promise.all(images.map(image => imgurUploader(image, {title: "McTester image"}).then(data => data.link)));
}

//result = uploadImages([fs.readFileSync('test.png')]);
//result.then(links => console.log("Got links: " + links));

postComment().then(() => console.log("Posted comment!"));
