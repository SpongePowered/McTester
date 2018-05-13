const axios = require('axios');

module.exports = class ImgurAPI {
    constructor() {
        this.instance = axios.create(
            {
                baseURL: 'https://api.imgur.com/3/',
                headers: {'Authorization': process.env.IMGUR_TOKEN, 'content-type': 'application/json'}
            }
        );
    }

    get(url) {
        return this.request("GET", url);
    }

    post(url, data) {
        return this.request("POST", url, data);
    }

    request(method, url, data) {
        return new Promise((resolve, reject) => {
            this.instance.request(
                {
                    method: method,
                    url: url,
                    data: data
                }
            ).then(resp => {
                if (!!resp.data.success) {
                    resolve(resp.data.data)
                } else {
                    reject(resp.data)
                }
            })
        })

    }

    uploadImage(buf, opt) {
        return this.post("image", Object.assign({image: buf.toString('base64')}, opt));
    }

    uploadImages(images) {
        return Promise.all(images.map(image => this.uploadImage(image.raw, image.opt)));
    }

    uploadIntoAlbum(images, albumOpt) {
        return this.uploadImages(images).then(uploads => {
            return this.createAlbum(Object.assign({deletehashes: uploads.map(upload => upload.deletehash)}, albumOpt));
        });
    }

    createAlbum(opt) {
        return this.post("album", opt).then(album => {
            return this.getAlbum(album.id);
        })
    }

    getAlbum(id) {
        return this.get("album/" + id);
    }
}
