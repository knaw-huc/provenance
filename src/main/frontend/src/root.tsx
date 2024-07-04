import React, {useEffect, useState} from 'react';
import {Provenance, Resource} from './interfaces.ts';
import {ProvHow, ProvWhen, ProvWhere, ProvWho, ProvWhy} from './columns.tsx';
import './style.css';

export default function ProvenanceTrail() {
    const [resource, setResource] = useState<string | null>(null);
    const [trail, setTrail] = useState<Resource | null>(null);

    async function loadProvenanceTrail() {
        const resource = window.location.hash.substring(1);
        setResource(resource);

        const params = new URLSearchParams();
        params.append('resource', resource);
        params.append('direction', 'backwards');

        const result = await fetch(`/trail?${params.toString()}`);
        if (result.ok)
            setTrail(await result.json());
    }

    useEffect(() => {
        loadProvenanceTrail();
        window.addEventListener('hashchange', loadProvenanceTrail);
        return () => window.removeEventListener('hashchange', loadProvenanceTrail);
    }, []);

    if (resource === null) {
        return <div></div>;
    }

    return (
        <div className="container">
            <div className="trail">
                <div className="trail-container">
                    <h1>{resource}</h1>
                </div>

                {trail && <ResourceProvenanceEvents resource={trail}/>}
            </div>
        </div>
    );
}

function ResourceProvenanceEvents({resource}: { resource: Resource }) {
    const [selectedProv, setSelectedProv] = useState<Provenance | null>(null);

    useEffect(() => setSelectedProv(resource.relations[0]), [resource]);

    return (
        <>
            <div className="trail-container">
                <div className="cards">
                    {resource.relations.map(prov =>
                        <ProvenanceCard key={prov.combinedId} provenance={prov} isSelected={prov === selectedProv}
                                        onClick={() => setSelectedProv(prov)}/>)}
                </div>
            </div>

            {selectedProv && <ProvenanceResourceSources provenance={selectedProv}/>}
        </>
    );
}

function ProvenanceCard({provenance, isSelected, onClick}: {
    provenance: Provenance,
    isSelected: boolean,
    onClick?: () => void
}) {
    return (
        <div className={`card ${isSelected ? 'selected' : ''}`} onClick={onClick}>
            <dl>
                <dt>When</dt>
                <dd><ProvWhen provenance={provenance}/></dd>

                <dt>How</dt>
                <dd><ProvHow provenance={provenance}/></dd>

                <dt>Why</dt>
                <dd><ProvWhy provenance={provenance}/></dd>

                <dt>Who</dt>
                <dd><ProvWho provenance={provenance}/></dd>

                <dt>Where</dt>
                <dd><ProvWhere provenance={provenance}/></dd>
            </dl>
        </div>
    );
}

function ProvenanceResourceSources({provenance}: { provenance: Provenance }) {
    const [selectedResource, setSelectedResource] = useState<Resource | null>(null);
    const sortFunc = (a: Resource, b: Resource) => a.resource.localeCompare(b.resource);

    useEffect(() => setSelectedResource(provenance.relations[0]), [provenance]);

    return (
        <>
            <div className="trail-container">
                <ul className="resources card">
                    {provenance.relations.sort(sortFunc).map(resource =>
                        <li key={resource.resource} className={resource === selectedResource ? 'selected' : ''}>
                            <span onClick={() => setSelectedResource(resource)}>
                                {resource.resource}
                            </span>

                            <a href={resource.resource} target="_blank">
                                Go to resource
                            </a>
                        </li>)}
                </ul>
            </div>

            {selectedResource && selectedResource.relations.length > 0 &&
                <ResourceProvenanceEvents resource={selectedResource}/>}
        </>
    );
}
