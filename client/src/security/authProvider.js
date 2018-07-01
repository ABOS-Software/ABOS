// in src/authProvider.js
import {AUTH_ERROR, AUTH_LOGIN} from 'react-admin';

export default (type, params) => {
    if (type === AUTH_LOGIN) {
        const {username, password} = params;
        const request = new Request('http://192.168.1.3:8080/api/login', {
            method: 'POST',
            body: JSON.stringify({username, password}),
            headers: new Headers({'Content-Type': 'application/json'}),
        });
        return fetch(request)
            .then(response => {
                if (response.status < 200 || response.status >= 300) {
                    throw new Error(response.statusText);
                }
                return response.json();
            })
            .then(({access_token}) => {
                localStorage.setItem('access_token', access_token);
            });
    }
    if (type === AUTH_ERROR) {
        const status = params.status;
        if (status === 401 || status === 403) {
            localStorage.removeItem('token');
            return Promise.reject();
        }
        return Promise.resolve();
    }
    return Promise.resolve();
}