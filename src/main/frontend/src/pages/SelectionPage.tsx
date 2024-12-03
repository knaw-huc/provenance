import {Link, useLoaderData} from "react-router-dom";

interface ITarget {
    resource: string
    relation: string
}

export async function selectionLoader({params}: {params: any}) {
    const provId: number = params.provId;

    const result = await fetch(`/prov/` + provId);

    if (!result.ok) {
        throw new Error(`Failed to load provenance`);
    }

    const body = await result.json();
    return {targets: body.target} as {targets: ITarget[]}
}

export function SelectionPage() {
    const {targets} = useLoaderData() as {targets: ITarget[]};

    return <>
        <div>
            <h2>Select target</h2>
            <ul>
                {targets.map((target) => {
                    return <li><Link to={'/' + encodeURIComponent(target.resource)}>{target.resource} ({target.relation})</Link></li>
                })}
            </ul>
        </div>
    </>
}
