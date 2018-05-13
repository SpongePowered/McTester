import axios, {AxiosInstance} from 'axios';
import {ImageWrapper} from "./upload";

interface Album {
    id: string,
    link: string
}

interface Image {
    id: string
}

module.exports = class ImgurAPI {
    private instance: AxiosInstance;

    constructor() {
        this.instance = axios.create(
            {
                baseURL: 'https://api.imgur.com/3/',
                headers: {'Authorization': process.env.IMGUR_TOKEN, 'content-type': 'application/json'}
            }
        );
    }

    get<T>(url: string): Promise<T> {
        return this.request("GET", url, {});
    }

    post<T>(url: string, data: object): Promise<T> {
        return this.request("POST", url, data);
    }

    request<T>(method: string, url: string, data: object): Promise<T> {
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

    uploadImage(buf: Buffer, opt: Object): Promise<Image> {
        return this.post<Image>("image", Object.assign({image: buf.toString('base64')}, opt));
    }

    uploadImages(images: ImageWrapper[]): Promise<Image[]> {
        return Promise.all(images.map(image => this.uploadImage(image.raw, image.opt)));
    }

    uploadIntoAlbum(images: ImageWrapper[], albumOpt: Object): Promise<Album> {
        return this.uploadImages(images).then(uploads => {
            return this.createAlbum(Object.assign({deletehashes: uploads.map(upload => upload.deletehash)}, albumOpt));
        });
    }

    createAlbum(opt: Object): Promise<Album> {
        return this.post<Album>("album", opt).then(album => {
            return this.getAlbum(album.id);
        })
    }

    getAlbum(id: string): Promise<Album> {
        return this.get("album/" + id);
    }
}

export {Album, Image};
