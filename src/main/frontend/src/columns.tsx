import dayjs from 'dayjs';
import {Provenance} from './provenance.tsx';
import {useProvenanceTemplateFor} from './templates.tsx';

export function ProvWhen({provenance}: { provenance: Provenance }) {
    return (
        <>
            {dayjs(provenance.when).format('D MMM YYYY')}
        </>
    );
}

export function ProvHow({provenance}: { provenance: Provenance }) {
    const template = useProvenanceTemplateFor('how_software', provenance.howSoftware);

    return (
        <>
            {template && template.description}

            {provenance.howSoftware && <pre>
                <div>
                    <strong>Script:</strong> {provenance.howSoftware}
                </div>

                {provenance.howInit && <div>
                    <strong>Initialized with:</strong> {provenance.howInit}
                </div>}
            </pre>}

            {provenance.howDelta && <pre>
                {provenance.howDelta}
            </pre>}
        </>
    );
}

export function ProvWhy({provenance}: { provenance: Provenance }) {
    const template = useProvenanceTemplateFor('why_motivation', provenance.whyMotivation);

    return (
        <>
            {template ? template.description : provenance.whyMotivation}

            {provenance.whyProvenanceSchema && <pre>
                {provenance.whyProvenanceSchema}
            </pre>}
        </>
    );
}

export function ProvWho({provenance}: { provenance: Provenance }) {
    return (
        <>
            {provenance.who}
        </>
    );
}

export function ProvWhere({provenance}: { provenance: Provenance }) {
    const template = useProvenanceTemplateFor('where_location', provenance.where);

    return (
        <>
            {template ? template.description : provenance.where}
        </>
    );
}

export function ProvInputOutput({provenance, resource}: { provenance: Provenance, resource: string }) {
    const inSources = !!provenance.source.find(res => res.resource === resource);
    const inTargets = !!provenance.target.find(res => res.resource === resource);

    return (
        <>
            {inSources && inTargets ? 'input and output' : (inSources ? 'input' : 'output')}
        </>
    );
}
