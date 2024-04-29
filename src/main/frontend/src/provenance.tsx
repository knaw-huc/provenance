import {useEffect, useState} from 'react';
import {ProvHow, ProvInputOutput, ProvWhen, ProvWhere, ProvWho, ProvWhy} from './columns.tsx';
import {ResourceLinks} from './resources.tsx';
import './style.css';

const PAGE_SIZE = 20;
const PROVENANCE_SIZE = 10;

export interface CombinedProvenance {
    who: string;
    where: string;
    howSoftware: string;
    howInit: string;
    howDelta: string;
    whyMotivation: string;
    whyProvenanceSchema: string;
    provenance: Provenance[];
}

export interface Provenance {
    id: number;
    when: string;
    source: ProvenanceResource[];
    target: ProvenanceResource[];
}

export interface ProvenanceResource {
    resource: string;
    relation: string;
}

export default function ProvenanceForResource() {
    const [resource, setResource] = useState<string | null>(null);
    const [provenance, setProvenance] = useState<CombinedProvenance[]>([]);
    const [offset, setOffset] = useState(0);

    async function loadProvenanceOfResource() {
        const resource = window.location.hash.substring(1);
        setResource(resource);
        setProvenance([]);
        setOffset(0);
        return loadProvenance(resource, 0);
    }

    async function loadMoreProvenance() {
        return loadProvenance(resource!, offset);
    }

    async function loadProvenance(resource: string, offset: number) {
        const result = await fetch(`/prov?resource=${resource}&offset=${offset}&limit=${PAGE_SIZE}`);
        if (result.ok) {
            const prov = await result.json();
            setProvenance(provenance => offset === 0 ? prov : provenance.concat(prov));
            setOffset(offset => offset + PAGE_SIZE);
        }
    }

    useEffect(() => {
        loadProvenanceOfResource();
        window.addEventListener('hashchange', loadProvenanceOfResource);
        return () => window.removeEventListener('hashchange', loadProvenanceOfResource);
    }, []);

    if (!provenance) {
        return <div></div>;
    }

    return (
        <div className="container">
            <h1>{resource}</h1>

            <table>
                <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>When</th>
                    <th>How</th>
                    <th>Why</th>
                    <th>Who</th>
                    <th>Where</th>
                    <th>Input / output</th>
                </tr>
                </thead>
                <tbody>
                {provenance.map(prov =>
                    <ProvRowWrapped provenance={prov} resource={resource!}
                                    key={prov.provenance.map(prov => prov.id).sort().join(':')}/>)}
                </tbody>
            </table>

            {provenance.length % PAGE_SIZE === 0 && <button onClick={loadMoreProvenance}>
                Load more results
            </button>}
        </div>
    );
}

function ProvRowWrapped({provenance, resource}: { provenance: CombinedProvenance, resource: string }) {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <>
            <ProvRow provenance={provenance} resource={resource} isOpen={isOpen}
                     toggle={() => setIsOpen(isOpen => !isOpen)}/>
            {isOpen && <ProvRowCollapsed provenance={provenance} resource={resource}/>}
        </>
    )
}

function ProvRow({provenance, resource, isOpen, toggle}: {
    provenance: CombinedProvenance,
    resource: string,
    isOpen: boolean,
    toggle: () => void
}) {
    return (
        <tr className="prov-row" onClick={toggle}>
            <td>{isOpen ? '⬇' : '⮕'}</td>
            <td><ProvWhen provenance={provenance}/></td>
            <td><ProvHow provenance={provenance}/></td>
            <td><ProvWhy provenance={provenance}/></td>
            <td><ProvWho provenance={provenance}/></td>
            <td><ProvWhere provenance={provenance}/></td>
            <td><ProvInputOutput provenance={provenance} resource={resource}/></td>
        </tr>
    );
}

function ProvRowCollapsed({provenance, resource}: { provenance: CombinedProvenance, resource: string }) {
    const [recordsToShow, setRecordsToShow] = useState<Provenance[]>([]);

    useEffect(() => {
        setRecordsToShow(provenance.provenance.slice(0, PROVENANCE_SIZE));
    }, [provenance]);

    function loadMore() {
        setRecordsToShow(res => res.concat(provenance.provenance.slice(res.length, res.length + PROVENANCE_SIZE)));
    }

    return (
        <tr>
            <td colSpan={6}>
                <div className="provenance">
                    <ul>
                        {recordsToShow.map(prov =>
                            <CollapsedProv key={prov.id} provenance={prov} resource={resource}/>)}

                        {provenance.provenance.length !== recordsToShow.length && <button onClick={loadMore}>
                            Load more provenance records
                        </button>}
                    </ul>
                </div>
            </td>
        </tr>
    );
}

function CollapsedProv({provenance, resource}: { provenance: Provenance, resource: string }) {
    return (
        <li>
            <p>ID: {provenance.id}</p>
            <ResourceLinks curResource={resource} sources={provenance.source} targets={provenance.target}/>
        </li>
    );
}
