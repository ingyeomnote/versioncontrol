import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api'.
    withCredentials: true // csrf 토큰을 위해 필요
});

// 요청 인터셉터
api.interceptors.request.use((config) =>{
    const token = localhost.getItem('accessToken');
    if(token){
        config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

// 응답 인터셉터
api.interceptors.response.use((response) => {
    return response;
}, async(error) => {
    const originalRequest = error.config;
    if(error.response.status == 401 && !originalRequest._retry){
        originalRequest._retry = true;
        try{
            const rs = await axios.post('/users/refresh-token', {}, {withCredentials: true});
            const { accessToken } = rs.data;
            localStorage.setItem('accessToken', accessToken);
            api.defaults.headers.common['Authorization'] = 'Bearer ' + accessToken;
            return api(originalRequest);
        } catch(_error){
            return Promise.reject(_error);
        }
    }
    return Promise.reject(error);
});

export default api;