import React from 'react';
import ReactDOM from 'react-dom/client';
import Provenance from './provenance.tsx';
import OldProvenance from './old/provenance.tsx';

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
    return <Provenance/>;
}
