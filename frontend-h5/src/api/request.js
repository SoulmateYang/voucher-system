import axios from 'axios';
import { Toast } from 'vant';

const TOKEN_KEY = 'card_voucher_token';

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
}

// Request interceptor: attach JWT token
request.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// Response interceptor: handle 401 and errors
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code === 401) {
      removeToken();
      window.location.hash = '#/login';
      Toast.fail('登录已过期，请重新登录');
      return Promise.reject(new Error('登录已过期，请重新登录'));
    }
    if (res.code !== 0) {
      Toast.fail(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    return res;
  },
  (error) => {
    const { response } = error;
    if (response) {
      const { status, data } = response;
      if (status === 401) {
        removeToken();
        window.location.hash = '#/login';
        Toast.fail('登录已过期，请重新登录');
      } else {
        const message = data?.message || '请求失败，请稍后重试';
        Toast.fail(message);
      }
    } else {
      Toast.fail('网络异常，请检查网络连接');
    }
    return Promise.reject(error);
  },
);

export default request;
