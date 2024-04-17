import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/prov': 'http://127.0.0.1:8080',
            '/resource': 'http://127.0.0.1:8080',
            '/trail': 'http://127.0.0.1:8080',
        }
    }
});
