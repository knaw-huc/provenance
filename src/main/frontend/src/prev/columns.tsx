import dayjs from 'dayjs';
import {CombinedProvenance} from './provenance.tsx';
import {useProvenanceTemplateFor} from './templates.tsx';

export function ProvWhen({provenance}: { provenance: CombinedProvenance }) {
    const firstDate = dayjs(provenance.provenance[provenance.provenance.length - 1].when).format('D MMM YYYY');
    const lastDate = dayjs(provenance.provenance[0].when).format('D MMM YYYY');

    return (
        <>
            {firstDate === lastDate ? firstDate : `${firstDate} - ${lastDate}`}
        </>
    );
}

export function ProvHow({provenance}: { provenance: CombinedProvenance }) {
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

export function ProvWhy({provenance}: { provenance: CombinedProvenance }) {
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

export function ProvWho({provenance}: { provenance: CombinedProvenance }) {
    return (
        <>
            {provenance.who}
        </>
    );
}

export function ProvWhere({provenance}: { provenance: CombinedProvenance }) {
    const template = useProvenanceTemplateFor('where_location', provenance.where);

    return (
        <>
            {template ? template.description : provenance.where}
        </>
    );
}

export function ProvInputOutput({provenance, resource}: { provenance: CombinedProvenance, resource: string }) {
    const inSources = !!provenance.provenance.find(prov =>
        prov.source.find(res => res.resource === resource));
    const inTargets = !!provenance.provenance.find(prov =>
        prov.target.find(res => res.resource === resource));

    return (
        <>
            {inSources && inTargets ? 'input and output' : (inSources ? 'input' : 'output')}
        </>
    );
}
