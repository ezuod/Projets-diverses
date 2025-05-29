import axios, { type AxiosResponse } from 'axios';
import type { ImageType } from './image';

const instance = axios.create({
  baseURL: '/',
  timeout: 15000,
});

const responseBody = (response: AxiosResponse) => response.data;

const requests = {
  get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
  post: (url: string, body: {}) => instance.post(url, body, { headers: { "Content-Type": "multipart/form-data" }, }).then(responseBody),
  put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
  delete: (url: string) => instance.delete(url).then(responseBody)
};

export const api = {
  getImageList: (): Promise<ImageType[]> => requests.get('images', {}),
  getImageDetails: (id: number): Promise<any> => requests.get(`images/${id}/details`, {}),
  getImage: (id: number): Promise<Blob> => requests.get(`images/${id}`, { responseType: "blob" }),
  createImage: (form: FormData): Promise<ImageType> => requests.post('images', form),
  deleteImage: (id: number): Promise<void> => requests.delete(`images/${id}`),
  getSimilarImages: (id: number, N: number): Promise<any> => requests.get(`images/${id}/similar?number=${N}&descriptor=histogram`, {}),
  searchImages: (query: string): Promise<ImageType[]> => requests.get(`images/search?query=${encodeURIComponent(query)}`, {})
};