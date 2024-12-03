import {IProvenance, ITemplate, SelectNodeFunction} from "../interfaces.ts";
import {ProvenanceOption} from "./ProvenanceOption.tsx";
import {useEffect, useState} from "react";
import {ProvenanceNode} from "./ProvenanceNode.tsx";

export function Provenances({provenances, templates, selectNode}: {provenances: IProvenance[], templates: ITemplate[], selectNode: SelectNodeFunction}) {

    const [selectedProvenance, setSelectedProvenance] = useState<IProvenance>(provenances[0]);

    function onClickOption (provenance: IProvenance) {
        setSelectedProvenance(provenance);
        selectNode(provenance);
    }

    useEffect(() => {
        setSelectedProvenance(provenances[0]);
    }, [provenances])

    function renderOptions() {
        if (provenances.length <= 1) {
            return;
        }
        return <>
            <div
                className="group flex flex-col md:flex-row gap-6 border-b border-b-neutral-200/70flex-wrap lg:flex-nowrap pl-6 md:pl-0 relative  ">
                <div
                    className="absolute text-xs text-neutral-500 -translate-x-36 translate-y-20 hidden group-hover:block">Event
                    selection
                </div>
                <div className="flex gap-4">
                    {provenances.map((provenance) => <ProvenanceOption
                        provenance={provenance}
                        key={provenance.combinedId}
                        templates={templates}
                        selected={provenance == selectedProvenance}
                        onClick={() => {onClickOption(provenance)}}
                    />)}
                </div>
            </div>
        </>
    }

    return (
        <>
            {renderOptions()}
            <ProvenanceNode provenance={selectedProvenance} templates={templates} />
        </>
    )
}
