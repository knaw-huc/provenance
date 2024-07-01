import dayjs from 'dayjs';
import {Provenance} from './interfaces.ts';
import {useProvenanceTemplateFor} from './templates.tsx';

export function ProvWhen({provenance}: { provenance: Provenance }) {
    const dates = Object.values(provenance.records).sort();
    const firstDate = dayjs(dates[0]).format('D MMM YYYY');
    const lastDate = dayjs(dates[dates.length - 1]).format('D MMM YYYY');

    return (
        <>
            {firstDate === lastDate ? firstDate : `${firstDate} - ${lastDate}`}
        </>
    );
}

export function ProvHow({provenance}: { provenance: Provenance }) {
    const template = useProvenanceTemplateFor('how_software', provenance.data.howSoftware);

    return (
        <>
            {template && template.description}

            {provenance.data.howSoftware && <pre>
                <div>
                    <strong>Script:</strong> {provenance.data.howSoftware}
                </div>

                {provenance.data.howInit && <div>
                    <strong>Initialized with:</strong> {provenance.data.howInit}
                </div>}
            </pre>}

            {provenance.data.howDelta && <pre>
                {provenance.data.howDelta}
            </pre>}
        </>
    );
}

export function ProvWhy({provenance}: { provenance: Provenance }) {
    const template = useProvenanceTemplateFor('why_motivation', provenance.data.whyMotivation);

    return (
        <>
            {template ? template.description : provenance.data.whyMotivation}

            {provenance.data.whyProvenanceSchema && <pre>
                {provenance.data.whyProvenanceSchema}
            </pre>}
        </>
    );
}

export function ProvWho({provenance}: { provenance: Provenance }) {
    return (
        <>
            {provenance.data.who}
        </>
    );
}

export function ProvWhere({provenance}: { provenance: Provenance }) {
    const template = useProvenanceTemplateFor('where_location', provenance.data.where);

    return (
        <>
            {template ? template.description : provenance.data.where}
        </>
    );
}
