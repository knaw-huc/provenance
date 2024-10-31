import React, {useEffect, useRef, useState} from 'react';
import {Provenance, Resource} from './interfaces.ts';
import {ProvHow, ProvWhen, ProvWhere, ProvWho, ProvWhy} from './columns.tsx';
import createGraph from './graph.ts';
import './style.css';

export default function ProvenanceTrail() {
    const [resource, setResource] = useState<string | null>(null);
    const [trail, setTrail] = useState<Resource | null>(null);

    function onHashChange() {
        const resource = window.location.hash.substring(1);
        if (resource.length > 0) {
            const params = new URLSearchParams();
            params.append('resource', resource);
            window.location.search = params.toString();
        }
    }

    async function loadProvenanceTrail() {
        window.location.hash = '';

        const requestParams = new URLSearchParams(window.location.search);
        const resource = requestParams.get('resource');
        setResource(resource);

        if (resource) {
            const params = new URLSearchParams();
            params.append('resource', resource);
            params.append('direction', 'backwards');

            const result = await fetch(`/trail?${params.toString()}`);
            if (result.ok)
                setTrail(await result.json());
        }
        else {
            setTrail(null);
        }
    }

    useEffect(() => {
        onHashChange();
        loadProvenanceTrail();
        window.addEventListener('hashchange', onHashChange);
        return () => window.removeEventListener('hashchange', onHashChange);
    }, []);

    if (resource === null) {
        return <div></div>;
    }

    return (
        <div className="container">
            <h1>{resource}</h1>

            {trail && <>
                <ProvenanceTree trail={trail}/>

                <div className="trail">
                    <BaseResource resource={resource}/>
                    <ResourceProvenanceEvents resource={trail}/>
                </div>
            </>}
        </div>
    );
}

function ProvenanceTree({trail}: { trail: Resource }) {
    const container = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        createGraph(container.current!, trail);
    }, [trail]);

    return (
        <div className="tree-container" ref={container}>
            <svg className="tree"></svg>
            <svg className="legend"></svg>
            <div className="resource-info"></div>
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

function BaseResource({resource}: { resource: string }) {
    return (
        <div className="trail-container">
            <ul className="resources card">
                <li className="selected">
                    <span>{resource}</span>

                    <a href={resource} target="_blank">
                        Go to resource
                    </a>
                </li>
            </ul>
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
