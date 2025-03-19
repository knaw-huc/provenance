import {IProvenance, ITemplate} from "../interfaces.ts";
import {useState} from "react";
import {DetailType} from "../enums.ts";
import {ProvenanceDetail} from "./ProvenanceDetail.tsx";
import {formatValue} from "./formatting.ts";


export function ProvenanceNode({provenance, templates}: { provenance: IProvenance, templates: ITemplate[] }) {

    const date = new Date(provenance.data.when);

    const formatShort: Intl.DateTimeFormatOptions = {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    }

    const [expanded, setExpanded] = useState<boolean>(false)

    const expandedClasses = "group flex flex-col md:flex-row gap-6 border-b border-l-2 border-neutral-300 border-b-neutral-200/70 py-6 flex-wrap lg:flex-nowrap pl-6 md:pl-0 relative bg-neutral-50 after:content-['▲'] after:absolute after:bottom-0 after:text-neutral-300 after:text-xl after:-translate-x-2 after:translate-y-2 relative"
    const normalClasses = "group flex flex-col md:flex-row gap-6 border-b border-l-2 border-neutral-300 border-b-neutral-200/70 py-6 flex-wrap lg:flex-nowrap pl-6 md:pl-0 relative  after:content-['▲'] after:absolute after:bottom-0 after:text-neutral-300 after:text-xl after:-translate-x-2 after:translate-y-2 relative"
    const scriptLocation = provenance.data.howSoftware;

    function renderDetails () {


        return (<div
            className="border-b border-neutral-200 flex flex-col md:flex-row pl-6 md:pl-0 gap-10 py-8 bg-neutral-50 shadow-xl">
            <div className="w-1/2 md:w-1/12 flex justify-center">
                <ul className="text-xs text-neutral-500 italic">
                    {Object.keys(provenance.records).map(id =>
                        <li key={id}>#{id}</li>)}
                </ul>
            </div>
            <div className="w-full md:w-11/12 flex flex-col gap-4">
                {
                    Object.entries(provenance.data).map(([key, value]) => {
                        return <ProvenanceDetail type={key as DetailType} value={value} templates={templates} />;
                    })
                }
                <div className="flex flex-col md:flex-row gap-6">
                    <div className="w-1/2 md:w-2/12 font-bold text-sm whitespace-nowrap ">Script</div>
                    <a href={scriptLocation}
                       className="w-full md:w-9/12 after:content-['→'] after:pl-1">{scriptLocation}</a>
                </div>
            </div>
        </div>)
    }

    return (
        <>
            <div
                className={expanded ? expandedClasses : normalClasses}>
                <div className="absolute text-xs text-neutral-500 -translate-x-36 hidden group-hover:block">
                    Provenance event
                </div>
                <div className="w-1/2 md:w-1/12">
                    <div
                        className="p-1 border-neutral-300 rotate-45 -translate-x-10 md:-translate-x-4 inline-block bg-white border">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                             strokeWidth="1.5" stroke="currentColor" className="w-5 h-5 stroke-neutral-700 -rotate-45">
                            <path strokeLinecap="round" strokeLinejoin="round"
                                  d="M11.42 15.17L17.25 21A2.652 2.652 0 0021 17.25l-5.877-5.877M11.42 15.17l2.496-3.03c.317-.384.74-.626 1.208-.766M11.42 15.17l-4.655 5.653a2.548 2.548 0 11-3.586-3.586l6.837-5.63m5.108-.233c.55-.164 1.163-.188 1.743-.14a4.5 4.5 0 004.486-6.336l-3.276 3.277a3.004 3.004 0 01-2.25-2.25l3.276-3.276a4.5 4.5 0 00-6.336 4.486c.091 1.076-.071 2.264-.904 2.95l-.102.085m-1.745 1.437L5.909 7.5H4.5L2.25 3.75l1.5-1.5L7.5 4.5v1.409l4.26 4.26m-1.745 1.437l1.745-1.437m6.615 8.206L15.75 15.75M4.867 19.125h.008v.008h-.008v-.008z"
                            />
                        </svg>
                    </div>
                </div>
                <div
                    className="w-1/2 md:w-2/12 font-bold text-sm whitespace-nowrap ">{date.toLocaleDateString("en-US", formatShort)}</div>
                <div className="w-full md:w-4/12">
                    {formatValue(provenance.data.howSoftware, DetailType.HowSoftware, templates)}
                </div>
                <div className="w-1/2 md:w-2/12 flex items-center">
                    <a href={scriptLocation}
                        className="bg-neutral-100 hover:bg-neutral-200 transition text-sm rounded-full px-3 py-2 no-underline">Script &rarr;</a>
                </div>
                <div className="w-1/2 md:w-2/12 flex items-center gap-2">Info
                    <button
                        onClick={() => setExpanded(!expanded)}
                        className="bg-neutral-100 hover:bg-neutral-200 transition text-sm rounded-full px-3 py-2">
                        {expanded ? (
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                                 strokeWidth="1.5" stroke="currentColor" className="w-5 h-5 stroke-neutral-900">
                                <path strokeLinecap="round" strokeLinejoin="round" d="m4.5 15.75 7.5-7.5 7.5 7.5"
                                />
                            </svg>
                        ) : (
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                                 strokeWidth="1.5" stroke="currentColor" className="w-5 h-5 stroke-neutral-900">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5"
                                />
                            </svg>
                        )}
                    </button>
                </div>
            </div>
            {expanded ? renderDetails() : ''}
        </>
    )
}
