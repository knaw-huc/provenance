import {IProvenance, ITemplate, SelectNodeFunction} from "../interfaces.ts";
import {ProvenanceOption} from "./ProvenanceOption.tsx";
import {useState} from "react";
import {ProvenanceNode} from "./ProvenanceNode.tsx";

export function Provenances({provenances, templates, selectNode}: {provenances: IProvenance[], templates: ITemplate[], selectNode: SelectNodeFunction}) {

    const [selectedProvenance, setSelectedProvenance] = useState<IProvenance>(provenances[0]);

    function onClickOption (provenance: IProvenance) {
        setSelectedProvenance(provenance);
        selectNode(provenance);
    }

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
                        templates={templates}
                        selected={provenance == selectedProvenance}
                        onClick={() => {onClickOption(provenance)}}
                    />)}
                    {/*<div className="py-8 relative">*/}
                    {/*    <div className="absolute  border-neutral-300 w-1/2  top-0 bottom-0 "></div>*/}
                    {/*    <button*/}
                    {/*        className="flex justify-start text-left flex-col gap-2 w-full p-3 rounded-sm border border-neutral-200 bg-white hover:bg-neutral-100 transition relative">*/}
                    {/*        <div className="text-sm font-bold">9 Sep 2023</div>*/}
                    {/*        <div className="text-xs">Importing, parsing, extracting and visualising information from the*/}
                    {/*            Resolutions*/}
                    {/*            of the Dutch States General*/}
                    {/*        </div>*/}
                    {/*    </button>*/}
                    {/*</div>*/}
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
