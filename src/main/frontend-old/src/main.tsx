import React from 'react';
import ReactDOM from 'react-dom/client';
import ProvenanceTrail from './root.tsx';
import {ProvenanceTemplatesProvider} from './templates.tsx';

ReactDOM.createRoot(document.getElementById('app')!).render(
    <React.StrictMode>
        <ProvenanceTemplatesProvider>
            <ProvenanceTrail/>
        </ProvenanceTemplatesProvider>
    </React.StrictMode>
);
