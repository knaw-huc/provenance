import {ProvenanceResource} from './provenance.tsx';
import {Dispatch, SetStateAction, useEffect, useState} from 'react';

const RESOURCES_SIZE = 10;

export function ResourceLinks({curResource, sources, targets}: {
    curResource: string,
    sources: ProvenanceResource[],
    targets: ProvenanceResource[]
}) {
    const [sourcesToShow, setSourcesToShow] = useState<ProvenanceResource[]>([]);
    const [targetsToShow, setTargetsToShow] = useState<ProvenanceResource[]>([]);

    useEffect(() => {
        setSourcesToShow(sources.slice(0, RESOURCES_SIZE));
        setTargetsToShow(targets.slice(0, RESOURCES_SIZE));
    }, []);

    function loadMore(resources: ProvenanceResource[], setter: Dispatch<SetStateAction<ProvenanceResource[]>>) {
        setter(res => res.concat(resources.slice(res.length, res.length + RESOURCES_SIZE)));
    }

    return (
        <div className="resources">
            <ul>
                {sourcesToShow.map(source =>
                    <ResourceLink key={`s:${source.resource}`} curResource={curResource} provResource={source}/>)}

                {sources.length !== sourcesToShow.length &&
                    <button onClick={_ => loadMore(sources, setSourcesToShow)}>
                        Load more sources
                    </button>}
            </ul>

            <span className="resource-arrow">⮕</span>

            <ul>
                {targetsToShow.map(target =>
                    <ResourceLink key={`t:${target.resource}`} curResource={curResource} provResource={target}/>)}

                {targets.length !== targetsToShow.length &&
                    <button onClick={_ => loadMore(targets, setTargetsToShow)}>
                        Load more targets
                    </button>}
            </ul>
        </div>
    );
}

function ResourceLink({curResource, provResource}: { curResource: string, provResource: ProvenanceResource }) {
    const isCurResource = provResource.resource === curResource;

    return (
        <li className={isCurResource ? 'cur-resource' : ''}>
            {isCurResource ? provResource.resource : <a href={`#${provResource.resource}`}>
                {provResource.resource}
            </a>}
        </li>
    );
}
