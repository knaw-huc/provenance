import React from 'react';
import ReactDOM from 'react-dom/client';
import OldProvenance from './old/provenance.tsx';
import ProvenanceForResource from './provenance.tsx';
import {ProvenanceTemplatesProvider} from './templates.tsx';

ReactDOM.createRoot(document.getElementById('app')!).render(
    <React.StrictMode>
        <SimpleRouter/>
    </React.StrictMode>
);

function SimpleRouter() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('old')) {
        return <OldProvenance/>;
    }

    return (
        <ProvenanceTemplatesProvider>
            <ProvenanceForResource/>
        </ProvenanceTemplatesProvider>
    );
}
