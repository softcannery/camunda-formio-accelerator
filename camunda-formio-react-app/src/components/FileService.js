import NativePromise from "native-promise-only";

class FileService {
  constructor() {}

  async xhrRequest(
    method,
    url,
    name,
    query,
    data,
    options,
    progressCallback,
    abortCallback,
  ) {
    return new NativePromise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      const json = typeof data === "string";
      const fd = new FormData();

      if (typeof progressCallback === "function") {
        xhr.upload.onprogress = progressCallback;
      }

      if (typeof abortCallback === "function") {
        abortCallback(() => xhr.abort());
      }

      if (!json) {
        for (const key in data) {
          fd.append(key, data[key]);
        }
      }

      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          // Need to test if xhr.response is decoded or not.
          let respData = {};
          try {
            respData =
              typeof xhr.response === "string" ? JSON.parse(xhr.response) : {};
            respData = respData && respData.data ? respData.data : respData;
          } catch (err) {
            respData = {};
          }

          // Get the url of the file.
          let respUrl = respData.hasOwnProperty("url")
            ? respData.url
            : `${xhr.responseURL}/${name}`;

          // If they provide relative url, then prepend the url.
          if (respUrl && respUrl[0] === "/") {
            respUrl = `${url}${respUrl}`;
          }
          resolve({ url: respUrl, data: respData });
        } else {
          reject(xhr.response || "Unable to upload file");
        }
      };

      xhr.onerror = () => reject(xhr);
      xhr.onabort = () => reject(xhr);

      let requestUrl = url + (url.indexOf("?") > -1 ? "&" : "?");
      for (const key in query) {
        requestUrl += `${key}=${query[key]}&`;
      }
      if (requestUrl[requestUrl.length - 1] === "&") {
        requestUrl = requestUrl.substr(0, requestUrl.length - 1);
      }

      xhr.open(method, requestUrl);
      if (json) {
        xhr.setRequestHeader("Content-Type", "application/json");
      }

      //Overrides previous request props
      if (options) {
        const parsedOptions =
          typeof options === "string" ? JSON.parse(options) : options;
        for (const prop in parsedOptions) {
          xhr[prop] = parsedOptions[prop];
        }
      }
      xhr.send(json ? data : fd);
    });
  }

  async uploadFile(
    storage,
    file,
    fileName,
    dir,
    evt,
    url,
    options,
    progressCallback,
    abortCallback,
  ) {
    console.log(
      "FileService Upload file: ",
      storage,
      file,
      fileName,
      dir,
      url,
      options,
    );

    return this.xhrRequest(
      "POST",
      url,
      fileName,
      {},
      {
        file: file,
        name: fileName,
        dir,
      },
      options,
      progressCallback,
      abortCallback,
    ).then((response) => {
      return {
        storage: "url",
        name: fileName,
        url: response.url,
        size: file.size,
        type: file.type,
        dir: dir,
        metadata: response.data.metadata || {},
      };
    });
  }

  async deleteFile(fileInfo) {
    console.log("FileService Delete file: ", fileInfo);

    return new NativePromise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open("DELETE", fileInfo.url, true);
      xhr.setRequestHeader("Authorization", "Bearer " + this.token);
      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve("File deleted");
        } else {
          reject(xhr.response || "Unable to delete file");
        }
      };
      xhr.send(null);
    });
  }

  async downloadFile(fileInfo, options) {
    console.log("FileService Download file: ", fileInfo, options);

    return this.xhrRequest("GET", fileInfo.url, fileInfo.name, {}, {}).then(
      (response) => response.data,
    );
  }
}

export default FileService;
