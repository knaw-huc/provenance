import {IProvenance, ITemplate} from "../interfaces.ts";
import {DetailType} from "../enums.ts";
import {formatValue} from "./formatting.ts";
import React from "react";

type OnClickFunction = (e: React.MouseEvent) => void

export function ProvenanceOption({provenance, templates, selected, onClick}: {provenance: IProvenance, templates: ITemplate[], selected: boolean, onClick: OnClickFunction}) {

    const date = new Date(provenance.data.when);

    const formatShort: Intl.DateTimeFormatOptions = {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    }

    if (selected) {
        return <>
            <div className="py-8 relative">
                <div
                    className="absolute  border-neutral-300 w-1/2  top-0 bottom-0 border-r-2 after:content-['▲'] after:absolute after:bottom-0 after:right-0 after:text-neutral-300 after:text-xl after:translate-x-2 after:translate-y-2 "></div>
                <button
                    className="flex justify-start text-left flex-col gap-2 w-full p-3 rounded-sm border border-neutral-200 bg-neutral-100 border-l-republicOrange-400 border-l-4 hover:bg-neutral-100 transition relative">
                    <div className="text-sm font-bold">{date.toLocaleDateString("en-UK", formatShort)}</div>
                    <div className="text-xs">
                        {formatValue(provenance.data.howSoftware, DetailType.HowSoftware, templates)}
                    </div>
                </button>
            </div>
        </>
    }
    return <>
        <div onClick={onClick} className="py-8 relative">
            <div className="absolute  border-neutral-300 w-1/2  top-0 bottom-0 "></div>
            <button
                className="flex justify-start text-left flex-col gap-2 w-full p-3 rounded-sm border border-neutral-200 bg-white hover:bg-neutral-100 transition relative">
                <div className="text-sm font-bold">{date.toLocaleDateString("en-UK", formatShort)}</div>
                <div className="text-xs">
                    {formatValue(provenance.data.howSoftware, DetailType.HowSoftware, templates)}
                </div>
            </button>
        </div>
    </>
}
